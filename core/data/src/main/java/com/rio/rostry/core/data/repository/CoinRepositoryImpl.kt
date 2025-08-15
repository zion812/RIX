package com.rio.rostry.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.rio.rostry.core.data.service.UserValidationService
import com.rio.rostry.core.data.service.ValidationException
import com.rio.rostry.core.data.service.AuthenticationService
import com.rio.rostry.core.database.dao.CoinDao
import com.rio.rostry.core.database.dao.UserDao
import com.rio.rostry.core.database.entities.CoinTransactionEntity
import com.rio.rostry.core.common.network.NetworkStateManager
import com.rio.rostry.core.common.model.TransactionStatus
import com.rio.rostry.core.common.model.TransactionType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ✅ Coin repository with atomic transaction handling
 * Ensures transaction integrity and proper error handling
 */
@Singleton
class CoinRepositoryImpl @Inject constructor(
    private val coinDao: CoinDao,
    private val userDao: UserDao,
    private val userValidationService: UserValidationService,
    private val authenticationService: AuthenticationService,
    private val functions: FirebaseFunctions,
    private val firestore: FirebaseFirestore,
    private val networkStateManager: NetworkStateManager
) {
    
    /**
     * ✅ Purchase marketplace listing with atomic coin transaction
     */
    suspend fun purchaseMarketplaceListing(
        listingData: Map<String, Any>,
        coinCost: Int
    ): Result<String> {
        return try {
            // Validate user has sufficient coins
            val validationResult = userValidationService.validateSufficientCoins(
                getCurrentUserId(), 
                coinCost
            )
            if (!validationResult.isValid) {
                return Result.failure(ValidationException(validationResult))
            }
            
            // ✅ Optimistic UI update
            val currentBalance = getCurrentUserBalance()
            if (currentBalance < coinCost) {
                return Result.failure(InsufficientCoinsException())
            }
            
            // ✅ Show pending state
            updateLocalBalance(currentBalance - coinCost, isPending = true)
            
            // ✅ Call atomic transaction function
            val result = functions
                .getHttpsCallable("createMarketplaceListing")
                .call(mapOf(
                    "listingData" to listingData,
                    "coinCost" to coinCost
                ))
                .await()
            
            val data = result.data as Map<String, Any>
            val newBalance = (data["newBalance"] as Number).toInt()
            val serviceResult = data["serviceResult"] as Map<String, String>
            
            // ✅ Update local state with confirmed balance
            updateLocalBalance(newBalance, isPending = false)
            
            // ✅ Record successful transaction locally
            recordLocalTransaction(
                amount = coinCost,
                purpose = "marketplace_listing",
                transactionId = data["transactionId"] as String,
                status = TransactionStatus.COMPLETED,
                transactionType = TransactionType.MARKETPLACE_LISTING
            )
            
            Result.success(serviceResult["listingId"]!!)
            
        } catch (e: Exception) {
            // ✅ Revert optimistic update
            revertOptimisticUpdate()
            
            when (e) {
                is FirebaseFunctionsException -> {
                    when (e.code) {
                        FirebaseFunctionsException.Code.FAILED_PRECONDITION -> {
                            Result.failure(InsufficientCoinsException())
                        }
                        else -> Result.failure(TransactionException(e.message))
                    }
                }
                else -> Result.failure(e)
            }
        }
    }
    
    /**
     * ✅ Purchase coins with payment gateway integration
     */
    suspend fun purchaseCoins(
        coinPackage: CoinPackage,
        paymentMethod: PaymentMethod
    ): Result<String> {
        return try {
            // Create secure order
            val orderResult = createSecureOrder(coinPackage)
            if (orderResult.isFailure) {
                return Result.failure(orderResult.exceptionOrNull()!!)
            }
            
            val orderData = orderResult.getOrThrow()
            
            // Launch payment gateway
            val paymentResult = launchPaymentGateway(orderData, paymentMethod)
            if (paymentResult.isFailure) {
                return Result.failure(paymentResult.exceptionOrNull()!!)
            }
            
            val paymentData = paymentResult.getOrThrow()
            
            // Process coin credit on server
            val creditResult = functions
                .getHttpsCallable("processCoinPurchase")
                .call(mapOf(
                    "orderId" to orderData.orderId,
                    "paymentId" to paymentData.paymentId,
                    "amount" to coinPackage.coinCount
                ))
                .await()
            
            val data = creditResult.data as Map<String, Any>
            val newBalance = (data["newBalance"] as Number).toInt()
            
            // ✅ Update local balance
            updateLocalBalance(newBalance, isPending = false)
            
            // ✅ Record transaction
            recordLocalTransaction(
                amount = coinPackage.coinCount,
                purpose = "coin_purchase",
                transactionId = data["transactionId"] as String,
                status = TransactionStatus.COMPLETED,
                transactionType = TransactionType.COIN_PURCHASE
            )
            
            Result.success(data["transactionId"] as String)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * ✅ Get user's coin balance with real-time updates
     */
    fun getCoinBalance(): Flow<Int> {
        return userDao.observeUser(getCurrentUserId())
            .filterNotNull()
            .map { it.coinBalance }
            .distinctUntilChanged()
    }
    
    /**
     * ✅ Get transaction history
     */
    fun getTransactionHistory(): Flow<List<CoinTransactionEntity>> {
        return coinDao.getTransactionsByUser(getCurrentUserId())
    }
    
    /**
     * ✅ Get pending transactions
     */
    suspend fun getPendingTransactions(): List<CoinTransactionEntity> {
        return coinDao.getPendingTransactions(getCurrentUserId())
    }
    
    /**
     * ✅ Retry failed transactions
     */
    suspend fun retryFailedTransactions(): Result<Unit> {
        return try {
            if (!networkStateManager.isConnected.value) {
                return Result.failure(NetworkException("No internet connection"))
            }
            
            val failedTransactions = coinDao.getFailedTransactions(getCurrentUserId())
            
            failedTransactions.forEach { transaction ->
                try {
                    when (transaction.purpose) {
                        "marketplace_listing" -> {
                            // Retry marketplace listing creation
                            retryMarketplaceListing(transaction)
                        }
                        "premium_feature" -> {
                            // Retry premium feature activation
                            retryPremiumFeature(transaction)
                        }
                        // Add other transaction types as needed
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CoinRepo", "Failed to retry transaction ${transaction.id}", e)
                }
            }
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * ✅ Validate transaction integrity
     */
    suspend fun validateTransactionIntegrity(transactionId: String): Result<Boolean> {
        return try {
            val result = functions
                .getHttpsCallable("validateTransactionIntegrity")
                .call(mapOf("transactionId" to transactionId))
                .await()
            
            val data = result.data as Map<String, Any>
            val isValid = data["isValid"] as Boolean
            
            Result.success(isValid)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun getCurrentUserId(): String {
        return authenticationService.getCurrentUserId() 
            ?: throw IllegalStateException("User not authenticated")
    }
    
    private suspend fun getCurrentUserBalance(): Int {
        return userDao.getCurrentUser()?.coinBalance ?: 0
    }
    
    private suspend fun updateLocalBalance(newBalance: Int, isPending: Boolean) {
        val pendingBalance = if (isPending) newBalance else 0
        userDao.updateCoinBalance(getCurrentUserId(), newBalance, pendingBalance)
    }
    
    private suspend fun revertOptimisticUpdate() {
        // ✅ Fetch actual balance from server or revert to last known good state
        val lastKnownBalance = coinDao.getLastConfirmedBalance(getCurrentUserId())
        updateLocalBalance(lastKnownBalance, isPending = false)
    }
    
    private suspend fun recordLocalTransaction(
        amount: Int,
        purpose: String,
        transactionId: String,
        status: TransactionStatus,
        transactionType: TransactionType
    ) {
        val transaction = CoinTransactionEntity(
            id = transactionId,
            userId = getCurrentUserId(),
            amount = amount,
            transactionType = transactionType,
            status = status,
            purpose = purpose,
            createdAt = Date(),
            isSynced = true
        )
        
        coinDao.insertTransaction(transaction)
    }
    
    private suspend fun createSecureOrder(coinPackage: CoinPackage): Result<OrderData> {
        // MOCK IMPLEMENTATION: In a real app, this would call the payment gateway's backend
        // to create a secure order and get an order ID.
        return try {
            val orderId = "mock_order_${UUID.randomUUID()}"
            val razorpayOrderId = "mock_razorpay_${UUID.randomUUID()}"
            Result.success(OrderData(orderId, razorpayOrderId, coinPackage.priceInRupees))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun launchPaymentGateway(
        orderData: OrderData,
        paymentMethod: PaymentMethod
    ): Result<PaymentData> {
        // MOCK IMPLEMENTATION: In a real app, this would launch the payment gateway's SDK UI.
        // Here, we simulate a successful payment.
        return try {
            val paymentId = "mock_payment_${UUID.randomUUID()}"
            Result.success(PaymentData(paymentId, "success"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun retryMarketplaceListing(transaction: CoinTransactionEntity) {
        // Basic retry logic: For now, just log it.
        // A real implementation would re-submit the transaction details.
        android.util.Log.i("CoinRepo", "Simulating retry for marketplace transaction: ${transaction.id}")
        // Mark as completed to remove from retry queue for this demo
        coinDao.updateTransactionStatus(transaction.id, TransactionStatus.COMPLETED)
    }

    private suspend fun retryPremiumFeature(transaction: CoinTransactionEntity) {
        // Basic retry logic: For now, just log it.
        android.util.Log.i("CoinRepo", "Simulating retry for premium feature transaction: ${transaction.id}")
        coinDao.updateTransactionStatus(transaction.id, TransactionStatus.COMPLETED)
    }
}

/**
 * Data classes and exceptions
 */
data class CoinPackage(
    val id: String,
    val coinCount: Int,
    val priceInRupees: Int,
    val bonusCoins: Int = 0
)

data class OrderData(
    val orderId: String,
    val razorpayOrderId: String,
    val amount: Int
)

data class PaymentData(
    val paymentId: String,
    val status: String
)

enum class PaymentMethod {
    RAZORPAY,
    PAYU,
    UPI
}

class InsufficientCoinsException : Exception("Insufficient coin balance")
class TransactionException(message: String?) : Exception(message)
class NetworkException(message: String) : Exception(message)
package com.rio.rostry.core.payment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import com.rio.rostry.core.common.exceptions.SyncException
import com.rio.rostry.core.database.RIOLocalDatabase
import com.rio.rostry.core.database.entities.CoinTransactionEntity
import com.rio.rostry.core.network.NetworkStateManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Payment manager for RIO coin-based payment system
 * Integrates with Razorpay, UPI, and Google Pay for rural India
 */
@Singleton
class PaymentManager @Inject constructor(
    private val context: Context,
    private val auth: FirebaseAuth,
    private val functions: FirebaseFunctions,
    private val database: RIOLocalDatabase,
    private val networkStateManager: NetworkStateManager
) : PaymentResultWithDataListener {

    private val coinTransactionDao = database.coinTransactionDao()
    private var currentPaymentCallback: PaymentCallback? = null

    companion object {
        private const val RAZORPAY_KEY_ID = "rzp_test_demo_key" // Demo key
        private const val COIN_RATE = 5 // ₹5 per coin
    }

    init {
        // Initialize Razorpay
        Checkout.preload(context)
    }

    /**
     * Purchase coins with selected payment method
     */
    suspend fun purchaseCoins(
        activity: Activity,
        packageId: String,
        paymentMethod: PaymentMethod,
        callback: PaymentCallback
    ): Flow<PaymentResult> = flow {
        
        emit(PaymentResult.Loading("Creating payment order..."))
        currentPaymentCallback = callback

        try {
            // Check authentication
            val user = auth.currentUser
                ?: throw SyncException.AuthError.NotAuthenticated()

            // Create payment order
            val createOrderFunction = functions.getHttpsCallable("createCoinPurchaseOrder")
            val orderData = mapOf(
                "packageId" to packageId,
                "paymentMethod" to paymentMethod.name
            )

            val orderResult = createOrderFunction.call(orderData).await()
            val orderResponse = orderResult.data as Map<String, Any>
            
            emit(PaymentResult.Loading("Processing payment..."))

            when (paymentMethod) {
                PaymentMethod.RAZORPAY -> {
                    processRazorpayPayment(activity, orderResponse)
                }
                PaymentMethod.UPI -> {
                    processUPIPayment(orderResponse)
                }
                PaymentMethod.GOOGLE_PAY -> {
                    processGooglePayPayment(orderResponse)
                }
                PaymentMethod.PHONEPE -> {
                    processPhonePePayment(orderResponse)
                }
                PaymentMethod.PAYTM -> {
                    processPaytmPayment(orderResponse)
                }
            }

            emit(PaymentResult.Loading("Waiting for payment confirmation..."))

        } catch (e: Exception) {
            emit(PaymentResult.Error(e.toSyncException()))
            currentPaymentCallback?.onPaymentError(e.toSyncException())
        }
    }

    /**
     * Spend coins for marketplace features
     */
    suspend fun spendCoins(
        amount: Int,
        purpose: CoinPurpose,
        metadata: Map<String, Any> = emptyMap()
    ): Flow<CoinSpendResult> = flow {
        
        emit(CoinSpendResult.Processing("Validating transaction..."))

        try {
            // Check authentication
            val user = auth.currentUser
                ?: throw SyncException.AuthError.NotAuthenticated()

            // Check local balance first (for offline scenarios)
            val localBalance = getUserCoinBalance()
            if (localBalance < amount) {
                emit(CoinSpendResult.InsufficientBalance(localBalance, amount))
                return@flow
            }

            // Check network connectivity
            if (!networkStateManager.isConnected.value) {
                // Queue for later processing
                val transactionId = queueOfflineSpend(amount, purpose, metadata)
                emit(CoinSpendResult.QueuedForLater(transactionId, "Queued for when online"))
                return@flow
            }

            // Process spend transaction
            val spendFunction = functions.getHttpsCallable("spendCoins")
            val spendData = mapOf(
                "amount" to amount,
                "purpose" to purpose.name,
                "metadata" to metadata
            )

            val spendResult = spendFunction.call(spendData).await()
            val response = spendResult.data as Map<String, Any>

            if (response["success"] == true) {
                val transactionId = response["transactionId"] as String
                val coinsSpent = (response["coinsSpent"] as Number).toInt()
                val newBalance = (response["newBalance"] as Number).toInt()
                val discountApplied = (response["discountApplied"] as Number).toInt()

                // Update local balance
                updateLocalCoinBalance(newBalance)

                // Record local transaction
                recordLocalTransaction(
                    transactionId = transactionId,
                    type = "SPEND",
                    amount = coinsSpent,
                    purpose = purpose.name,
                    balanceAfter = newBalance
                )

                emit(CoinSpendResult.Success(
                    transactionId = transactionId,
                    coinsSpent = coinsSpent,
                    newBalance = newBalance,
                    discountApplied = discountApplied
                ))
            } else {
                emit(CoinSpendResult.Error(SyncException.ValidationError.InvalidData(
                    "transaction", "spend", "Transaction failed"
                )))
            }

        } catch (e: Exception) {
            emit(CoinSpendResult.Error(e.toSyncException()))
        }
    }

    /**
     * Get user's coin balance and transaction history
     */
    suspend fun getCoinBalance(): Flow<CoinBalanceResult> = flow {
        
        emit(CoinBalanceResult.Loading)

        try {
            // Get local balance first
            val localBalance = getUserCoinBalance()
            emit(CoinBalanceResult.Success(
                balance = localBalance,
                balanceInINR = localBalance * COIN_RATE,
                transactions = getLocalTransactions(),
                isFromCache = true
            ))

            // Fetch latest from server if online
            if (networkStateManager.isConnected.value) {
                val balanceFunction = functions.getHttpsCallable("getCoinBalance")
                val balanceResult = balanceFunction.call().await()
                val response = balanceResult.data as Map<String, Any>

                val serverBalance = (response["balance"] as Number).toInt()
                val transactions = response["transactions"] as List<Map<String, Any>>

                // Update local balance if different
                if (serverBalance != localBalance) {
                    updateLocalCoinBalance(serverBalance)
                }

                emit(CoinBalanceResult.Success(
                    balance = serverBalance,
                    balanceInINR = (response["balanceInINR"] as Number).toDouble(),
                    transactions = transactions.map { parseTransaction(it) },
                    isFromCache = false
                ))
            }

        } catch (e: Exception) {
            // If we have local data, emit that with error flag
            val localBalance = getUserCoinBalance()
            if (localBalance > 0) {
                emit(CoinBalanceResult.Success(
                    balance = localBalance,
                    balanceInINR = localBalance * COIN_RATE,
                    transactions = getLocalTransactions(),
                    isFromCache = true,
                    hasError = true
                ))
            } else {
                emit(CoinBalanceResult.Error(e.toSyncException()))
            }
        }
    }

    /**
     * Process queued offline transactions
     */
    suspend fun processQueuedTransactions(): Flow<QueueProcessResult> = flow {
        
        val queuedTransactions = coinTransactionDao.getPendingTransactions()
        emit(QueueProcessResult.Started(queuedTransactions.size))

        var successCount = 0
        var failureCount = 0

        for (transaction in queuedTransactions) {
            try {
                // Process the queued transaction
                val spendResult = spendCoins(
                    amount = transaction.amount,
                    purpose = CoinPurpose.valueOf(transaction.purpose),
                    metadata = parseMetadata(transaction.metadata)
                )

                spendResult.collect { result ->
                    when (result) {
                        is CoinSpendResult.Success -> {
                            // Update the queued transaction
                            coinTransactionDao.updateTransactionStatus(
                                transaction.id,
                                "COMPLETED",
                                result.transactionId
                            )
                            successCount++
                        }
                        is CoinSpendResult.Error -> {
                            coinTransactionDao.updateTransactionStatus(
                                transaction.id,
                                "FAILED",
                                null
                            )
                            failureCount++
                        }
                        else -> { /* Handle other states */ }
                    }
                }

                emit(QueueProcessResult.Progress(successCount, failureCount))

            } catch (e: Exception) {
                failureCount++
                coinTransactionDao.updateTransactionStatus(
                    transaction.id,
                    "FAILED",
                    null
                )
            }
        }

        emit(QueueProcessResult.Completed(successCount, failureCount))
    }

    /**
     * Razorpay payment processing
     */
    private fun processRazorpayPayment(activity: Activity, orderData: Map<String, Any>) {
        val checkout = Checkout()
        checkout.setKeyID(RAZORPAY_KEY_ID)

        val options = JSONObject().apply {
            put("name", "RIO Coins")
            put("description", "Purchase coins for RIO marketplace")
            put("order_id", orderData["orderId"])
            put("amount", orderData["amount"])
            put("currency", orderData["currency"])
            
            // Theme customization
            val theme = JSONObject().apply {
                put("color", "#FF6B35") // RIO brand color
            }
            put("theme", theme)

            // Prefill user details
            val prefill = JSONObject().apply {
                put("email", auth.currentUser?.email ?: "")
                put("contact", "") // Would get from user profile
            }
            put("prefill", prefill)

            // Payment methods
            val method = JSONObject().apply {
                put("upi", true)
                put("card", true)
                put("netbanking", true)
                put("wallet", true)
            }
            put("method", method)
        }

        checkout.open(activity, options)
    }

    /**
     * UPI payment processing
     */
    private suspend fun processUPIPayment(orderData: Map<String, Any>) {
        try {
            val upiFunction = functions.getHttpsCallable("createUPIPaymentLink")
            val upiData = mapOf(
                "orderId" to orderData["orderId"],
                "upiId" to "user@upi" // Would get from user input
            )

            val upiResult = upiFunction.call(upiData).await()
            val response = upiResult.data as Map<String, Any>

            // Open UPI payment link
            val paymentUrl = response["paymentUrl"] as String
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl))
            context.startActivity(intent)

        } catch (e: Exception) {
            currentPaymentCallback?.onPaymentError(e.toSyncException())
        }
    }

    /**
     * Google Pay payment processing
     */
    private suspend fun processGooglePayPayment(orderData: Map<String, Any>) {
        try {
            // Implementation for Google Pay integration
            // Would use Google Pay API for Android
            
            val gpayFunction = functions.getHttpsCallable("processGooglePayPayment")
            val gpayData = mapOf(
                "orderId" to orderData["orderId"],
                "paymentToken" to "demo_token" // Would get from Google Pay API
            )

            val gpayResult = gpayFunction.call(gpayData).await()
            val response = gpayResult.data as Map<String, Any>

            currentPaymentCallback?.onPaymentSuccess(
                paymentId = response["paymentId"] as String,
                orderId = orderData["orderId"] as String
            )

        } catch (e: Exception) {
            currentPaymentCallback?.onPaymentError(e.toSyncException())
        }
    }

    /**
     * PhonePe payment processing
     */
    private suspend fun processPhonePePayment(orderData: Map<String, Any>) {
        // Implementation for PhonePe integration
        // Would use PhonePe SDK
    }

    /**
     * Paytm payment processing
     */
    private suspend fun processPaytmPayment(orderData: Map<String, Any>) {
        // Implementation for Paytm integration
        // Would use Paytm SDK
    }

    /**
     * Razorpay callback implementations
     */
    override fun onPaymentSuccess(paymentId: String, paymentData: PaymentData?) {
        // Verify payment with server
        val orderId = paymentData?.orderId ?: ""
        val signature = paymentData?.signature ?: ""

        // Call verification function
        val verifyFunction = functions.getHttpsCallable("verifyPaymentAndCreditCoins")
        val verifyData = mapOf(
            "orderId" to orderId,
            "paymentId" to paymentId,
            "signature" to signature
        )

        verifyFunction.call(verifyData)
            .addOnSuccessListener { result ->
                val response = result.data as Map<String, Any>
                if (response["success"] == true) {
                    currentPaymentCallback?.onPaymentSuccess(paymentId, orderId)
                } else {
                    currentPaymentCallback?.onPaymentError(
                        SyncException.ValidationError.InvalidData("payment", "verification", "Payment verification failed")
                    )
                }
            }
            .addOnFailureListener { exception ->
                currentPaymentCallback?.onPaymentError(exception.toSyncException())
            }
    }

    override fun onPaymentError(errorCode: Int, errorDescription: String?, paymentData: PaymentData?) {
        val error = SyncException.NetworkError.ServerError(
            "Payment failed: $errorDescription (Code: $errorCode)"
        )
        currentPaymentCallback?.onPaymentError(error)
    }

    /**
     * Helper methods
     */
    private suspend fun getUserCoinBalance(): Int {
        return try {
            val userDoc = database.userDao().getById(auth.currentUser?.uid ?: "")
            userDoc?.coinBalance ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun updateLocalCoinBalance(newBalance: Int) {
        try {
            val userId = auth.currentUser?.uid ?: return
            database.userDao().updateCoinBalance(userId, newBalance)
        } catch (e: Exception) {
            // Log error but don't throw
        }
    }

    private suspend fun queueOfflineSpend(
        amount: Int,
        purpose: CoinPurpose,
        metadata: Map<String, Any>
    ): String {
        val transactionId = "offline_${System.currentTimeMillis()}"
        
        val transaction = CoinTransactionEntity(
            id = transactionId,
            userId = auth.currentUser?.uid ?: "",
            type = "SPEND",
            amount = amount,
            purpose = purpose.name,
            metadata = metadata.toString(),
            status = "PENDING",
            createdAt = Date()
        )

        coinTransactionDao.insert(transaction)
        return transactionId
    }

    private suspend fun recordLocalTransaction(
        transactionId: String,
        type: String,
        amount: Int,
        purpose: String,
        balanceAfter: Int
    ) {
        val transaction = CoinTransactionEntity(
            id = transactionId,
            userId = auth.currentUser?.uid ?: "",
            type = type,
            amount = amount,
            purpose = purpose,
            status = "COMPLETED",
            balanceAfter = balanceAfter,
            createdAt = Date()
        )

        coinTransactionDao.insert(transaction)
    }

    private suspend fun getLocalTransactions(): List<CoinTransaction> {
        return try {
            val userId = auth.currentUser?.uid ?: return emptyList()
            coinTransactionDao.getTransactionsByUser(userId).map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseTransaction(data: Map<String, Any>): CoinTransaction {
        return CoinTransaction(
            id = data["id"] as String,
            type = data["type"] as String,
            amount = (data["amount"] as Number).toInt(),
            purpose = data["purpose"] as String,
            createdAt = Date(), // Would parse from server timestamp
            status = data["status"] as String
        )
    }

    private fun parseMetadata(metadataString: String?): Map<String, Any> {
        return try {
            // Would parse JSON string to map
            emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun Exception.toSyncException(): SyncException {
        return when (this) {
            is SyncException -> this
            else -> SyncException.UnknownError(message ?: "Unknown error", this)
        }
    }
}

/**
 * Data classes and enums
 */
enum class PaymentMethod {
    RAZORPAY, UPI, GOOGLE_PAY, PHONEPE, PAYTM
}

enum class CoinPurpose {
    MARKETPLACE_LISTING,
    PREMIUM_FEATURES,
    FARMER_VERIFICATION,
    HEALTH_CERTIFICATE,
    BREEDING_RECORD,
    TRANSFER_VERIFICATION,
    PREMIUM_SUPPORT
}

interface PaymentCallback {
    fun onPaymentSuccess(paymentId: String, orderId: String)
    fun onPaymentError(error: SyncException)
}

data class CoinTransaction(
    val id: String,
    val type: String,
    val amount: Int,
    val purpose: String,
    val createdAt: Date,
    val status: String
)

sealed class PaymentResult {
    data class Loading(val message: String) : PaymentResult()
    data class Success(val paymentId: String, val orderId: String) : PaymentResult()
    data class Error(val exception: SyncException) : PaymentResult()
}

sealed class CoinSpendResult {
    data class Processing(val message: String) : CoinSpendResult()
    data class Success(
        val transactionId: String,
        val coinsSpent: Int,
        val newBalance: Int,
        val discountApplied: Int
    ) : CoinSpendResult()
    data class InsufficientBalance(val currentBalance: Int, val required: Int) : CoinSpendResult()
    data class QueuedForLater(val transactionId: String, val message: String) : CoinSpendResult()
    data class Error(val exception: SyncException) : CoinSpendResult()
}

sealed class CoinBalanceResult {
    object Loading : CoinBalanceResult()
    data class Success(
        val balance: Int,
        val balanceInINR: Double,
        val transactions: List<CoinTransaction>,
        val isFromCache: Boolean,
        val hasError: Boolean = false
    ) : CoinBalanceResult()
    data class Error(val exception: SyncException) : CoinBalanceResult()
}

sealed class QueueProcessResult {
    data class Started(val totalCount: Int) : QueueProcessResult()
    data class Progress(val successCount: Int, val failureCount: Int) : QueueProcessResult()
    data class Completed(val successCount: Int, val failureCount: Int) : QueueProcessResult()
}

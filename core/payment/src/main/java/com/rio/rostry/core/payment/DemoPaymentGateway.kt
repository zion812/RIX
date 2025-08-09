package com.rio.rostry.core.payment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.rio.rostry.core.common.exceptions.SyncException
import com.rio.rostry.core.database.RIOLocalDatabase
import com.rio.rostry.core.network.NetworkStateManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Demo Payment Gateway - Fully Functional Implementation
 * Provides realistic payment simulation for testing and demonstration
 */
@Singleton
class DemoPaymentGateway @Inject constructor(
    private val context: Context,
    private val auth: FirebaseAuth,
    private val functions: FirebaseFunctions,
    private val database: RIOLocalDatabase,
    private val networkStateManager: NetworkStateManager
) {

    companion object {
        private const val DEMO_GATEWAY_ID = "DEMO_GATEWAY"
        private const val MAX_DEMO_AMOUNT = 500000 // ₹5000 max for demo
    }

    /**
     * Create demo payment order
     */
    suspend fun createDemoOrder(
        amount: Double,
        packageId: String,
        paymentMethod: DemoPaymentMethod
    ): Flow<DemoOrderResult> = flow {
        
        emit(DemoOrderResult.Loading("Creating demo order..."))

        try {
            // Validate amount
            if (amount <= 0 || amount > MAX_DEMO_AMOUNT) {
                emit(DemoOrderResult.Error(
                    SyncException.ValidationError.InvalidData(
                        "amount", amount.toString(), "Amount must be between ₹1 and ₹5000 for demo"
                    )
                ))
                return@flow
            }

            // Check authentication
            val user = auth.currentUser
                ?: throw SyncException.AuthError.NotAuthenticated()

            // Create order via Firebase Function
            val createOrderFunction = functions.getHttpsCallable("createDemoPaymentOrder")
            val orderData = mapOf(
                "amount" to amount,
                "currency" to "INR",
                "packageId" to packageId,
                "paymentMethod" to paymentMethod.name
            )

            val orderResult = createOrderFunction.call(orderData).await()
            val response = orderResult.data as Map<String, Any>

            val demoOrder = DemoOrder(
                orderId = response["orderId"] as String,
                amount = response["amount"] as Double,
                currency = response["currency"] as String,
                status = response["status"] as String,
                paymentUrl = response["paymentUrl"] as String,
                qrCode = response["qrCode"] as String,
                expiresAt = Date((response["expiresAt"] as Map<String, Any>)["_seconds"] as Long * 1000)
            )

            emit(DemoOrderResult.Success(demoOrder))

        } catch (e: Exception) {
            emit(DemoOrderResult.Error(e.toSyncException()))
        }
    }

    /**
     * Process demo payment with realistic simulation
     */
    suspend fun processDemoPayment(
        orderId: String,
        paymentMethod: DemoPaymentMethod,
        paymentDetails: DemoPaymentDetails
    ): Flow<DemoPaymentResult> = flow {
        
        emit(DemoPaymentResult.Processing("Initializing payment..."))

        try {
            // Simulate payment method specific processing
            when (paymentMethod) {
                DemoPaymentMethod.UPI -> {
                    processUPIPayment(orderId, paymentDetails).collect { result ->
                        emit(result)
                    }
                }
                DemoPaymentMethod.GOOGLE_PAY -> {
                    processGooglePayPayment(orderId, paymentDetails).collect { result ->
                        emit(result)
                    }
                }
                DemoPaymentMethod.CARD -> {
                    processCardPayment(orderId, paymentDetails).collect { result ->
                        emit(result)
                    }
                }
                DemoPaymentMethod.NET_BANKING -> {
                    processNetBankingPayment(orderId, paymentDetails).collect { result ->
                        emit(result)
                    }
                }
                DemoPaymentMethod.WALLET -> {
                    processWalletPayment(orderId, paymentDetails).collect { result ->
                        emit(result)
                    }
                }
            }

        } catch (e: Exception) {
            emit(DemoPaymentResult.Failed(e.toSyncException()))
        }
    }

    /**
     * Process UPI payment simulation
     */
    private suspend fun processUPIPayment(
        orderId: String,
        paymentDetails: DemoPaymentDetails
    ): Flow<DemoPaymentResult> = flow {
        
        emit(DemoPaymentResult.Processing("Validating UPI ID..."))
        delay(1000) // Simulate validation delay

        // Validate UPI ID format
        val upiId = paymentDetails.upiId
        if (upiId.isNullOrEmpty() || !isValidUPIId(upiId)) {
            emit(DemoPaymentResult.Failed(
                SyncException.ValidationError.InvalidData("upiId", upiId ?: "", "Invalid UPI ID format")
            ))
            return@flow
        }

        emit(DemoPaymentResult.Processing("Connecting to UPI app..."))
        delay(1500)

        // Simulate UPI PIN entry
        emit(DemoPaymentResult.Processing("Enter UPI PIN in your app..."))
        delay(3000)

        // Process via Firebase Function
        val upiFunction = functions.getHttpsCallable("processDemoUPIPayment")
        val upiData = mapOf(
            "orderId" to orderId,
            "upiId" to upiId,
            "pin" to paymentDetails.upiPin
        )

        try {
            val upiResult = upiFunction.call(upiData).await()
            val response = upiResult.data as Map<String, Any>

            if (response["success"] == true) {
                emit(DemoPaymentResult.Success(
                    paymentId = response["paymentId"] as String,
                    bankReference = response["bankReferenceNumber"] as String,
                    message = response["message"] as String
                ))
            } else {
                emit(DemoPaymentResult.Failed(
                    SyncException.NetworkError.ServerError(response["message"] as String)
                ))
            }

        } catch (e: Exception) {
            emit(DemoPaymentResult.Failed(e.toSyncException()))
        }
    }

    /**
     * Process Google Pay payment simulation
     */
    private suspend fun processGooglePayPayment(
        orderId: String,
        paymentDetails: DemoPaymentDetails
    ): Flow<DemoPaymentResult> = flow {
        
        emit(DemoPaymentResult.Processing("Opening Google Pay..."))
        delay(800)

        emit(DemoPaymentResult.Processing("Authenticating with Google Pay..."))
        delay(1200)

        // Simulate Google Pay token generation
        val paymentToken = generateDemoPaymentToken("GPAY")

        emit(DemoPaymentResult.Processing("Processing payment..."))
        delay(1500)

        // Process via Firebase Function
        val gpayFunction = functions.getHttpsCallable("processDemoGooglePay")
        val gpayData = mapOf(
            "orderId" to orderId,
            "paymentToken" to paymentToken
        )

        try {
            val gpayResult = gpayFunction.call(gpayData).await()
            val response = gpayResult.data as Map<String, Any>

            if (response["success"] == true) {
                emit(DemoPaymentResult.Success(
                    paymentId = response["paymentId"] as String,
                    bankReference = null,
                    message = response["message"] as String
                ))
            } else {
                emit(DemoPaymentResult.Failed(
                    SyncException.NetworkError.ServerError(response["message"] as String)
                ))
            }

        } catch (e: Exception) {
            emit(DemoPaymentResult.Failed(e.toSyncException()))
        }
    }

    /**
     * Process card payment simulation
     */
    private suspend fun processCardPayment(
        orderId: String,
        paymentDetails: DemoPaymentDetails
    ): Flow<DemoPaymentResult> = flow {
        
        emit(DemoPaymentResult.Processing("Validating card details..."))
        delay(1500)

        // Validate card details
        if (!isValidCardNumber(paymentDetails.cardNumber)) {
            emit(DemoPaymentResult.Failed(
                SyncException.ValidationError.InvalidData("cardNumber", "****", "Invalid card number")
            ))
            return@flow
        }

        if (!isValidExpiryDate(paymentDetails.expiryMonth, paymentDetails.expiryYear)) {
            emit(DemoPaymentResult.Failed(
                SyncException.ValidationError.InvalidData("expiry", "**/**", "Invalid expiry date")
            ))
            return@flow
        }

        if (!isValidCVV(paymentDetails.cvv)) {
            emit(DemoPaymentResult.Failed(
                SyncException.ValidationError.InvalidData("cvv", "***", "Invalid CVV")
            ))
            return@flow
        }

        emit(DemoPaymentResult.Processing("Connecting to bank..."))
        delay(2000)

        emit(DemoPaymentResult.Processing("Authorizing payment..."))
        delay(3000)

        // Simulate 3D Secure for higher amounts
        if (paymentDetails.amount > 200000) { // ₹2000+
            emit(DemoPaymentResult.Processing("3D Secure authentication required..."))
            delay(4000)
        }

        // Process payment
        val success = simulateCardPaymentSuccess(paymentDetails)
        
        if (success) {
            val paymentId = generateDemoPaymentId("CARD")
            emit(DemoPaymentResult.Success(
                paymentId = paymentId,
                bankReference = "AUTH${Random.nextInt(100000, 999999)}",
                message = "Card payment successful"
            ))
        } else {
            val failureReason = getRandomCardFailureReason()
            emit(DemoPaymentResult.Failed(
                SyncException.NetworkError.ServerError(failureReason)
            ))
        }
    }

    /**
     * Process net banking payment simulation
     */
    private suspend fun processNetBankingPayment(
        orderId: String,
        paymentDetails: DemoPaymentDetails
    ): Flow<DemoPaymentResult> = flow {
        
        emit(DemoPaymentResult.Processing("Redirecting to bank website..."))
        delay(2000)

        emit(DemoPaymentResult.Processing("Please login to your bank account..."))
        delay(5000) // Longer delay for bank login

        emit(DemoPaymentResult.Processing("Authorizing transaction..."))
        delay(3000)

        emit(DemoPaymentResult.Processing("Processing payment..."))
        delay(2500)

        // Net banking has moderate success rate (94%)
        val success = Random.nextFloat() > 0.06f

        if (success) {
            val paymentId = generateDemoPaymentId("NETBANK")
            emit(DemoPaymentResult.Success(
                paymentId = paymentId,
                bankReference = "NB${Random.nextInt(1000000, 9999999)}",
                message = "Net banking payment successful"
            ))
        } else {
            val failureReason = getRandomNetBankingFailureReason()
            emit(DemoPaymentResult.Failed(
                SyncException.NetworkError.ServerError(failureReason)
            ))
        }
    }

    /**
     * Process wallet payment simulation
     */
    private suspend fun processWalletPayment(
        orderId: String,
        paymentDetails: DemoPaymentDetails
    ): Flow<DemoPaymentResult> = flow {
        
        emit(DemoPaymentResult.Processing("Opening wallet app..."))
        delay(1000)

        emit(DemoPaymentResult.Processing("Checking wallet balance..."))
        delay(1500)

        // Simulate insufficient balance scenario
        if (Random.nextFloat() < 0.1f) { // 10% chance
            emit(DemoPaymentResult.Failed(
                SyncException.ValidationError.InvalidData("balance", "insufficient", "Insufficient wallet balance")
            ))
            return@flow
        }

        emit(DemoPaymentResult.Processing("Authorizing payment..."))
        delay(2000)

        // Wallet payments have high success rate (96%)
        val success = Random.nextFloat() > 0.04f

        if (success) {
            val paymentId = generateDemoPaymentId("WALLET")
            emit(DemoPaymentResult.Success(
                paymentId = paymentId,
                bankReference = "WAL${Random.nextInt(100000, 999999)}",
                message = "Wallet payment successful"
            ))
        } else {
            emit(DemoPaymentResult.Failed(
                SyncException.NetworkError.ServerError("Wallet payment failed. Please try again.")
            ))
        }
    }

    /**
     * Get demo payment status
     */
    suspend fun getDemoPaymentStatus(orderId: String): DemoPaymentStatus {
        return try {
            // In a real implementation, this would query the backend
            // For demo, we'll simulate different statuses
            val statuses = listOf("PENDING", "PROCESSING", "SUCCESS", "FAILED")
            val randomStatus = statuses[Random.nextInt(statuses.size)]
            
            DemoPaymentStatus(
                orderId = orderId,
                status = randomStatus,
                lastUpdated = Date(),
                message = "Demo payment status: $randomStatus"
            )
        } catch (e: Exception) {
            DemoPaymentStatus(
                orderId = orderId,
                status = "ERROR",
                lastUpdated = Date(),
                message = "Failed to get payment status"
            )
        }
    }

    /**
     * Helper functions
     */
    private fun isValidUPIId(upiId: String): Boolean {
        val upiRegex = Regex("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$")
        return upiRegex.matches(upiId)
    }

    private fun isValidCardNumber(cardNumber: String?): Boolean {
        if (cardNumber.isNullOrEmpty()) return false
        val cleanNumber = cardNumber.replace(" ", "").replace("-", "")
        return cleanNumber.length in 13..19 && cleanNumber.all { it.isDigit() }
    }

    private fun isValidExpiryDate(month: Int?, year: Int?): Boolean {
        if (month == null || year == null) return false
        if (month !in 1..12) return false
        
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        
        return when {
            year < currentYear -> false
            year == currentYear -> month >= currentMonth
            year > currentYear + 10 -> false // Not more than 10 years in future
            else -> true
        }
    }

    private fun isValidCVV(cvv: String?): Boolean {
        if (cvv.isNullOrEmpty()) return false
        return cvv.length in 3..4 && cvv.all { it.isDigit() }
    }

    private fun simulateCardPaymentSuccess(paymentDetails: DemoPaymentDetails): Boolean {
        // Simulate different success rates based on card type and amount
        val baseSuccessRate = 0.92f // 92% base success rate
        
        // Adjust based on amount
        val amountFactor = when {
            paymentDetails.amount > 300000 -> 0.85f // Lower success for high amounts
            paymentDetails.amount > 100000 -> 0.90f
            else -> 1.0f
        }
        
        return Random.nextFloat() < (baseSuccessRate * amountFactor)
    }

    private fun generateDemoPaymentToken(prefix: String): String {
        return "${prefix}_${System.currentTimeMillis()}_${Random.nextInt(10000, 99999)}"
    }

    private fun generateDemoPaymentId(prefix: String): String {
        return "demo_${prefix.lowercase()}_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
    }

    private fun getRandomCardFailureReason(): String {
        val reasons = listOf(
            "Card declined by bank",
            "Insufficient funds",
            "Card expired",
            "Invalid CVV",
            "Transaction limit exceeded",
            "Card blocked",
            "Bank server timeout"
        )
        return reasons[Random.nextInt(reasons.size)]
    }

    private fun getRandomNetBankingFailureReason(): String {
        val reasons = listOf(
            "Bank server unavailable",
            "Session timeout",
            "Invalid login credentials",
            "Transaction limit exceeded",
            "Account temporarily blocked"
        )
        return reasons[Random.nextInt(reasons.size)]
    }

    private fun Exception.toSyncException(): SyncException {
        return when (this) {
            is SyncException -> this
            else -> SyncException.UnknownError(message ?: "Unknown error", this)
        }
    }
}

/**
 * Data classes for demo payment gateway
 */
enum class DemoPaymentMethod {
    UPI, GOOGLE_PAY, CARD, NET_BANKING, WALLET
}

data class DemoOrder(
    val orderId: String,
    val amount: Double,
    val currency: String,
    val status: String,
    val paymentUrl: String,
    val qrCode: String,
    val expiresAt: Date
)

data class DemoPaymentDetails(
    val amount: Double,
    // UPI details
    val upiId: String? = null,
    val upiPin: String? = null,
    // Card details
    val cardNumber: String? = null,
    val expiryMonth: Int? = null,
    val expiryYear: Int? = null,
    val cvv: String? = null,
    val cardHolderName: String? = null,
    // Bank details
    val bankCode: String? = null,
    // Wallet details
    val walletType: String? = null
)

data class DemoPaymentStatus(
    val orderId: String,
    val status: String,
    val lastUpdated: Date,
    val message: String
)

sealed class DemoOrderResult {
    data class Loading(val message: String) : DemoOrderResult()
    data class Success(val order: DemoOrder) : DemoOrderResult()
    data class Error(val exception: SyncException) : DemoOrderResult()
}

sealed class DemoPaymentResult {
    data class Processing(val message: String) : DemoPaymentResult()
    data class Success(
        val paymentId: String,
        val bankReference: String?,
        val message: String
    ) : DemoPaymentResult()
    data class Failed(val exception: SyncException) : DemoPaymentResult()
}

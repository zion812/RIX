package com.rio.rostry.core.payment

import android.app.Activity
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import com.rio.rostry.core.payment.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.util.Collections
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simplified Payment manager for RIO coin-based payment system
 * Phase 1.3 implementation with basic Razorpay integration
 */
@Singleton
class SimplePaymentManager @Inject constructor(
    private val context: Context,
    private val auth: FirebaseAuth,
    private val functions: FirebaseFunctions
) : PaymentResultWithDataListener {

    private var currentPaymentCallback: SimplePaymentCallback? = null
    private val processedIdempotencyKeys = Collections.synchronizedSet(mutableSetOf<String>())

    companion object {
        private val RAZORPAY_KEY_ID: String = BuildConfig.RAZORPAY_KEY_ID
        private const val COIN_RATE = 5 // ₹5 per coin
        private val DEMO_MODE: Boolean = BuildConfig.ENABLE_DEMO_GATEWAY
    }

    init {
        // Initialize Razorpay
        Checkout.preload(context)
    }

    /**
     * Purchase coins with Razorpay
     */
    suspend fun purchaseCoins(
        activity: Activity,
        coinAmount: Int,
        callback: SimplePaymentCallback
    ): Flow<SimplePaymentResult> = flow {
        // Backwards-compatible call uses a random idempotency key
        emitAll(purchaseCoins(activity, coinAmount, callback, UUID.randomUUID().toString()))
    }

    /**
     * Purchase coins with optional idempotency key (prevents duplicates)
     */
    suspend fun purchaseCoins(
        activity: Activity,
        coinAmount: Int,
        callback: SimplePaymentCallback,
        idempotencyKey: String
    ): Flow<SimplePaymentResult> = flow {

    emit(SimplePaymentResult.Loading("Creating payment order..."))
        currentPaymentCallback = callback

        try {
            // Check authentication
            val user = auth.currentUser
                ?: throw Exception("User not authenticated")

            val amountInRupees = coinAmount * COIN_RATE

            // Idempotency: if we've already processed this key successfully, reject duplicates
            if (processedIdempotencyKeys.contains(idempotencyKey)) {
                emit(SimplePaymentResult.Error("Duplicate payment detected (idempotency key used)"))
                return@flow
            }

            if (DEMO_MODE || RAZORPAY_KEY_ID.isBlank()) {
                // Demo payment flow
                emit(SimplePaymentResult.Loading("Processing demo payment..."))
                kotlinx.coroutines.delay(2000) // Simulate processing
                
                val demoPaymentId = "demo_payment_${System.currentTimeMillis()}"
                val demoOrderId = "demo_order_${System.currentTimeMillis()}"
                
                // Simulate successful payment
                emit(SimplePaymentResult.Success(demoPaymentId, demoOrderId, coinAmount))
                processedIdempotencyKeys.add(idempotencyKey)
                callback.onPaymentSuccess(demoPaymentId, demoOrderId, coinAmount)
            } else {
                // Real Razorpay payment
                processRazorpayPayment(activity, amountInRupees, coinAmount)
            }

        } catch (e: Exception) {
            emit(SimplePaymentResult.Error(e.message ?: "Payment failed"))
            callback.onPaymentError(e.message ?: "Payment failed")
        }
    }

    /**
     * Get user's coin balance (simplified)
     */
    suspend fun getCoinBalance(): Flow<SimpleCoinBalanceResult> = flow {
        emit(SimpleCoinBalanceResult.Loading)

        try {
            val user = auth.currentUser
                ?: throw Exception("User not authenticated")

            // For now, return a demo balance
            // TODO: Implement proper balance tracking
            val demoBalance = 100 // Demo balance
            
            emit(SimpleCoinBalanceResult.Success(
                balance = demoBalance,
                balanceInINR = demoBalance * COIN_RATE
            ))

        } catch (e: Exception) {
            emit(SimpleCoinBalanceResult.Error(e.message ?: "Failed to get balance"))
        }
    }

    /**
     * Razorpay payment processing
     */
    private fun processRazorpayPayment(activity: Activity, amountInRupees: Int, coinAmount: Int) {
        val checkout = Checkout()
        checkout.setKeyID(RAZORPAY_KEY_ID)

        val options = JSONObject().apply {
            put("name", "RIO Coins")
            put("description", "Purchase $coinAmount coins for RIO marketplace")
            put("amount", amountInRupees * 100) // Amount in paise
            put("currency", "INR")
            
            // Theme customization
            val theme = JSONObject().apply {
                put("color", "#FF6B35") // RIO brand color
            }
            put("theme", theme)

            // Prefill user details
            val prefill = JSONObject().apply {
                put("email", auth.currentUser?.email ?: "")
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
     * Razorpay callback implementations
     */
    override fun onPaymentSuccess(paymentId: String, paymentData: PaymentData?) {
        val orderId = paymentData?.orderId ?: ""
        currentPaymentCallback?.onPaymentSuccess(paymentId, orderId, 0) // TODO: Get coin amount
    }

    override fun onPaymentError(errorCode: Int, errorDescription: String?, paymentData: PaymentData?) {
        val errorMessage = "Payment failed: $errorDescription (Code: $errorCode)"
        currentPaymentCallback?.onPaymentError(errorMessage)
    }
}

/**
 * Simplified data classes and interfaces
 */
interface SimplePaymentCallback {
    fun onPaymentSuccess(paymentId: String, orderId: String, coinsAdded: Int)
    fun onPaymentError(error: String)
}

sealed class SimplePaymentResult {
    data class Loading(val message: String) : SimplePaymentResult()
    data class Success(val paymentId: String, val orderId: String, val coinsAdded: Int) : SimplePaymentResult()
    data class Error(val message: String) : SimplePaymentResult()
}

sealed class SimpleCoinBalanceResult {
    object Loading : SimpleCoinBalanceResult()
    data class Success(val balance: Int, val balanceInINR: Int) : SimpleCoinBalanceResult()
    data class Error(val message: String) : SimpleCoinBalanceResult()
}

/**
 * Coin packages for purchase
 */
data class CoinPackage(
    val id: String,
    val coins: Int,
    val bonusCoins: Int,
    val priceInRupees: Int
) {
    val totalCoins: Int get() = coins + bonusCoins
    val displayName: String get() = "$totalCoins coins for ₹$priceInRupees"
}

object CoinPackages {
    val SMALL = CoinPackage("small", 20, 0, 100)
    val MEDIUM = CoinPackage("medium", 100, 10, 500)
    val LARGE = CoinPackage("large", 200, 25, 1000)
    val PREMIUM = CoinPackage("premium", 500, 100, 2000)
    
    val ALL = listOf(SMALL, MEDIUM, LARGE, PREMIUM)
}

package com.rio.rostry.core.payment

import com.rio.rostry.core.common.exceptions.SyncException
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Demo Payment Gateway - Minimal Stub Implementation
 * Provides basic payment simulation for testing and demonstration
 */
@Singleton
class DemoPaymentGateway @Inject constructor() {

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
    ): DemoOrderResult {
        return DemoOrderResult.Success(
            DemoOrder(
                orderId = "demo_order_id",
                amount = amount,
                currency = "INR",
                status = "PENDING",
                paymentUrl = "https://example.com/payment",
                qrCode = "demo_qr_code",
                expiresAt = Date()
            )
        )
    }

    /**
     * Process demo payment with realistic simulation
     */
    suspend fun processDemoPayment(
        orderId: String,
        paymentMethod: DemoPaymentMethod,
        paymentDetails: DemoPaymentDetails
    ): DemoPaymentResult {
        return DemoPaymentResult.Success(
            paymentId = generateDemoPaymentId("DEMO"),
            bankReference = "demo_bank_reference",
            message = "Payment successful"
        )
    }

    /**
     * Get demo payment status
     */
    suspend fun getDemoPaymentStatus(orderId: String): DemoPaymentStatus {
        return DemoPaymentStatus(
            orderId = orderId,
            status = "PENDING",
            lastUpdated = Date(),
            message = "Demo payment status: PENDING"
        )
    }

    private fun generateDemoPaymentToken(prefix: String): String = "${prefix}_00000"
    private fun generateDemoPaymentId(prefix: String): String = "demo_${prefix}_0000"

    private fun Exception.toSyncException(): SyncException {
        return SyncException.UnknownError(message ?: "Unknown error", this)
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

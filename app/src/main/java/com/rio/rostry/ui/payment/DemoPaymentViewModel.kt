package com.rio.rostry.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.core.payment.*
import com.rio.rostry.core.common.exceptions.SyncException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Demo Payment ViewModel - Manages payment flow state and business logic
 */
@HiltViewModel
class DemoPaymentViewModel @Inject constructor(
    private val demoPaymentGateway: DemoPaymentGateway
) : ViewModel() {

    private val _uiState = MutableStateFlow<DemoPaymentUiState>(DemoPaymentUiState.Loading("Initializing..."))
    val uiState: StateFlow<DemoPaymentUiState> = _uiState.asStateFlow()

    private var currentOrder: DemoOrder? = null
    private var currentAmount: Double = 0.0
    private var currentPackageId: String = ""

    /**
     * Initialize payment with amount and package details
     */
    fun initializePayment(amount: Double, packageId: String) {
        currentAmount = amount
        currentPackageId = packageId
        
        viewModelScope.launch {
            _uiState.value = DemoPaymentUiState.Loading("Setting up payment...")
            
            try {
                // Validate amount
                if (amount <= 0) {
                    _uiState.value = DemoPaymentUiState.Error("Invalid payment amount")
                    return@launch
                }
                
                if (amount > 500000) { // ₹5000 demo limit
                    _uiState.value = DemoPaymentUiState.Error("Amount exceeds demo limit of ₹5000")
                    return@launch
                }
                
                // Move to payment method selection
                _uiState.value = DemoPaymentUiState.PaymentMethodSelection
                
            } catch (e: Exception) {
                _uiState.value = DemoPaymentUiState.Error("Failed to initialize payment: ${e.message}")
            }
        }
    }

    /**
     * Process payment with selected method and details
     */
    fun processPayment(paymentMethod: DemoPaymentMethod, paymentDetails: DemoPaymentDetails) {
        viewModelScope.launch {
            try {
                // First create the order
                _uiState.value = DemoPaymentUiState.Processing("Creating payment order...", 0.1f)
                
                demoPaymentGateway.createDemoOrder(
                    amount = currentAmount,
                    packageId = currentPackageId,
                    paymentMethod = paymentMethod
                ).collect { orderResult ->
                    when (orderResult) {
                        is DemoOrderResult.Loading -> {
                            _uiState.value = DemoPaymentUiState.Processing(orderResult.message, 0.2f)
                        }
                        
                        is DemoOrderResult.Success -> {
                            currentOrder = orderResult.order
                            processPaymentWithOrder(orderResult.order, paymentMethod, paymentDetails)
                        }
                        
                        is DemoOrderResult.Error -> {
                            _uiState.value = DemoPaymentUiState.Error(
                                "Failed to create order: ${orderResult.exception.message}"
                            )
                        }
                    }
                }
                
            } catch (e: Exception) {
                _uiState.value = DemoPaymentUiState.Error("Payment failed: ${e.message}")
            }
        }
    }

    /**
     * Process payment with created order
     */
    private suspend fun processPaymentWithOrder(
        order: DemoOrder,
        paymentMethod: DemoPaymentMethod,
        paymentDetails: DemoPaymentDetails
    ) {
        demoPaymentGateway.processDemoPayment(
            orderId = order.orderId,
            paymentMethod = paymentMethod,
            paymentDetails = paymentDetails
        ).collect { paymentResult ->
            when (paymentResult) {
                is DemoPaymentResult.Processing -> {
                    val progress = when {
                        paymentResult.message.contains("Validating") -> 0.3f
                        paymentResult.message.contains("Connecting") -> 0.5f
                        paymentResult.message.contains("Authorizing") -> 0.7f
                        paymentResult.message.contains("Processing") -> 0.9f
                        else -> 0.6f
                    }
                    _uiState.value = DemoPaymentUiState.Processing(paymentResult.message, progress)
                }
                
                is DemoPaymentResult.Success -> {
                    _uiState.value = DemoPaymentUiState.Success(
                        paymentId = paymentResult.paymentId,
                        message = paymentResult.message,
                        bankReference = paymentResult.bankReference
                    )
                }
                
                is DemoPaymentResult.Failed -> {
                    _uiState.value = DemoPaymentUiState.Error(
                        paymentResult.exception.message ?: "Payment failed"
                    )
                }
            }
        }
    }

    /**
     * Retry failed payment
     */
    fun retryPayment() {
        _uiState.value = DemoPaymentUiState.PaymentMethodSelection
    }

    /**
     * Get payment status for tracking
     */
    fun getPaymentStatus(orderId: String) {
        viewModelScope.launch {
            try {
                val status = demoPaymentGateway.getDemoPaymentStatus(orderId)
                // Handle status update if needed
            } catch (e: Exception) {
                // Handle error silently for status check
            }
        }
    }

    /**
     * Cancel current payment
     */
    fun cancelPayment() {
        _uiState.value = DemoPaymentUiState.PaymentMethodSelection
    }

    /**
     * Simulate different payment scenarios for testing
     */
    fun simulatePaymentScenario(scenario: PaymentScenario) {
        viewModelScope.launch {
            when (scenario) {
                PaymentScenario.SUCCESS -> {
                    _uiState.value = DemoPaymentUiState.Processing("Simulating success...", 0.5f)
                    kotlinx.coroutines.delay(2000)
                    _uiState.value = DemoPaymentUiState.Success(
                        paymentId = "demo_success_${System.currentTimeMillis()}",
                        message = "Payment completed successfully",
                        bankReference = "REF${System.currentTimeMillis()}"
                    )
                }
                
                PaymentScenario.FAILURE -> {
                    _uiState.value = DemoPaymentUiState.Processing("Simulating failure...", 0.5f)
                    kotlinx.coroutines.delay(2000)
                    _uiState.value = DemoPaymentUiState.Error("Simulated payment failure")
                }
                
                PaymentScenario.TIMEOUT -> {
                    _uiState.value = DemoPaymentUiState.Processing("Simulating timeout...", 0.8f)
                    kotlinx.coroutines.delay(5000)
                    _uiState.value = DemoPaymentUiState.Error("Payment timeout - please try again")
                }
                
                PaymentScenario.INSUFFICIENT_BALANCE -> {
                    _uiState.value = DemoPaymentUiState.Processing("Checking balance...", 0.3f)
                    kotlinx.coroutines.delay(1500)
                    _uiState.value = DemoPaymentUiState.Error("Insufficient balance in account")
                }
                
                PaymentScenario.NETWORK_ERROR -> {
                    _uiState.value = DemoPaymentUiState.Processing("Connecting...", 0.2f)
                    kotlinx.coroutines.delay(3000)
                    _uiState.value = DemoPaymentUiState.Error("Network error - please check your connection")
                }
            }
        }
    }

    /**
     * Get demo payment statistics for analytics
     */
    fun getDemoPaymentStats(): DemoPaymentStats {
        return DemoPaymentStats(
            totalTransactions = 1250,
            successfulTransactions = 1187,
            failedTransactions = 63,
            successRate = 94.96,
            averageProcessingTime = 3.2,
            mostUsedMethod = "UPI",
            totalAmount = 156750.0
        )
    }
}

/**
 * UI State for demo payment flow
 */
sealed class DemoPaymentUiState {
    data class Loading(val message: String) : DemoPaymentUiState()
    object PaymentMethodSelection : DemoPaymentUiState()
    data class Processing(val message: String, val progress: Float) : DemoPaymentUiState()
    data class Success(
        val paymentId: String,
        val message: String,
        val bankReference: String?
    ) : DemoPaymentUiState()
    data class Error(val error: String) : DemoPaymentUiState()
}

/**
 * Payment scenarios for testing
 */
enum class PaymentScenario {
    SUCCESS,
    FAILURE,
    TIMEOUT,
    INSUFFICIENT_BALANCE,
    NETWORK_ERROR
}

/**
 * Demo payment statistics
 */
data class DemoPaymentStats(
    val totalTransactions: Int,
    val successfulTransactions: Int,
    val failedTransactions: Int,
    val successRate: Double,
    val averageProcessingTime: Double, // in seconds
    val mostUsedMethod: String,
    val totalAmount: Double
)

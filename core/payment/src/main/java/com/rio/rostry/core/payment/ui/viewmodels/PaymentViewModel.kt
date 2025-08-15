package com.rio.rostry.core.payment.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.core.data.repository.CoinPackage
import com.rio.rostry.core.data.repository.PaymentMethod
import com.rio.rostry.core.database.entities.CoinTransactionEntity
import com.rio.rostry.core.payment.domain.usecases.GetCoinBalanceUseCase
import com.rio.rostry.core.payment.domain.usecases.GetTransactionHistoryUseCase
import com.rio.rostry.core.payment.domain.usecases.PurchaseCoinsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentUiState(
    val balance: Int = 0,
    val transactionHistory: List<CoinTransactionEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val purchaseState: PurchaseState = PurchaseState.Idle
)

sealed class PurchaseState {
    object Idle : PurchaseState()
    object Loading : PurchaseState()
    data class Success(val transactionId: String) : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val getCoinBalanceUseCase: GetCoinBalanceUseCase,
    private val getTransactionHistoryUseCase: GetTransactionHistoryUseCase,
    private val purchaseCoinsUseCase: PurchaseCoinsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState(isLoading = true))
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            combine(
                getCoinBalanceUseCase(),
                getTransactionHistoryUseCase()
            ) { balance, history ->
                PaymentUiState(balance = balance, transactionHistory = history)
            }.catch { e ->
                _uiState.value = PaymentUiState(error = e.message)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun purchaseCoins(coinPackage: CoinPackage) {
        viewModelScope.launch {
            _uiState.update { it.copy(purchaseState = PurchaseState.Loading) }
            val result = purchaseCoinsUseCase(coinPackage, PaymentMethod.RAZORPAY) // Defaulting to Razorpay
            result.onSuccess { transactionId ->
                _uiState.update { it.copy(purchaseState = PurchaseState.Success(transactionId)) }
                // Balance will update automatically via the collecting flow
            }.onFailure { error ->
                _uiState.update { it.copy(purchaseState = PurchaseState.Error(error.message ?: "Unknown error")) }
            }
        }
    }

    fun resetPurchaseState() {
        _uiState.update { it.copy(purchaseState = PurchaseState.Idle) }
    }
}

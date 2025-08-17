package com.rio.rostry.features.user.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.core.data.model.CoinTransactionType
import com.rio.rostry.core.data.repository.CoinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoinViewModel @Inject constructor(
    private val coinRepository: CoinRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<CoinUiState>(CoinUiState.Loading)
    val uiState = _uiState.asStateFlow()
    
    fun loadCoinBalance(userId: String) {
        viewModelScope.launch {
            _uiState.value = CoinUiState.Loading
            
            coinRepository.getCoinBalance(userId)
                .onSuccess { balance ->
                    _uiState.value = CoinUiState.Success(balance)
                }
                .onFailure { exception ->
                    _uiState.value = CoinUiState.Error(exception.message ?: "Failed to load coin balance")
                }
        }
    }
    
    fun loadTransactionHistory(userId: String) {
        viewModelScope.launch {
            coinRepository.getTransactionHistory(userId)
                .collect { transactions ->
                    // Update UI state with transactions
                    val currentState = _uiState.value
                    if (currentState is CoinUiState.Success) {
                        _uiState.value = currentState.copy(transactions = transactions)
                    }
                }
        }
    }
    
    fun addCoins(userId: String, amount: Int, transactionType: CoinTransactionType, relatedEntityId: String? = null) {
        viewModelScope.launch {
            _uiState.value = CoinUiState.Loading
            
            coinRepository.addCoins(userId, amount, transactionType, relatedEntityId)
                .onSuccess {
                    // Reload balance after successful transaction
                    loadCoinBalance(userId)
                }
                .onFailure { exception ->
                    _uiState.value = CoinUiState.Error(exception.message ?: "Failed to add coins")
                }
        }
    }
    
    fun deductCoins(userId: String, amount: Int, transactionType: CoinTransactionType, relatedEntityId: String? = null) {
        viewModelScope.launch {
            _uiState.value = CoinUiState.Loading
            
            coinRepository.deductCoins(userId, amount, transactionType, relatedEntityId)
                .onSuccess {
                    // Reload balance after successful transaction
                    loadCoinBalance(userId)
                }
                .onFailure { exception ->
                    _uiState.value = CoinUiState.Error(exception.message ?: "Failed to deduct coins")
                }
        }
    }
}

sealed class CoinUiState {
    object Loading : CoinUiState()
    data class Success(
        val balance: Int,
        val transactions: List<com.rio.rostry.core.data.model.CoinTransaction> = emptyList()
    ) : CoinUiState()
    data class Error(val message: String) : CoinUiState()
}
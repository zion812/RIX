package com.rio.rostry.features.fowl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.core.data.model.Transfer
import com.rio.rostry.core.data.repository.TransferRepository
import com.rio.rostry.features.fowl.ui.TransferStatusUi
import com.rio.rostry.features.fowl.ui.TransferUiState
import com.rio.rostry.features.fowl.ui.TransferUiStateModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val transferRepository: TransferRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<TransferUiState>(TransferUiState.Idle)
    val uiState = _uiState.asStateFlow()
    
    fun loadTransfer(transferId: String) {
        viewModelScope.launch {
            _uiState.value = TransferUiState.Loading
            
            transferRepository.getTransfer(transferId)
                .onSuccess { transfer ->
                    _uiState.value = transfer.toUiState()
                }
                .onFailure { exception ->
                    _uiState.value = TransferUiState.Error(exception.message ?: "Unknown error")
                }
        }
    }
    
    fun verifyTransfer(transferId: String, verificationDetails: Map<String, Any>) {
        viewModelScope.launch {
            _uiState.value = TransferUiState.Loading
            
            transferRepository.verifyTransfer(transferId, verificationDetails)
                .onSuccess { transfer ->
                    _uiState.value = TransferUiState.Verified(transfer.toUiStateModel())
                }
                .onFailure { exception ->
                    _uiState.value = TransferUiState.Error(exception.message ?: "Unknown error")
                }
        }
    }
    
    fun rejectTransfer(transferId: String) {
        viewModelScope.launch {
            _uiState.value = TransferUiState.Loading
            
            transferRepository.rejectTransfer(transferId)
                .onSuccess { transfer ->
                    _uiState.value = TransferUiState.Rejected(transfer.toUiStateModel())
                }
                .onFailure { exception ->
                    _uiState.value = TransferUiState.Error(exception.message ?: "Unknown error")
                }
        }
    }
    
    private fun Transfer.toUiState(): TransferUiState {
        return when (this.status) {
            com.rio.rostry.core.data.model.TransferStatus.PENDING -> 
                TransferUiState.Pending(this.toUiStateModel())
            com.rio.rostry.core.data.model.TransferStatus.VERIFIED -> 
                TransferUiState.Verified(this.toUiStateModel())
            com.rio.rostry.core.data.model.TransferStatus.REJECTED -> 
                TransferUiState.Rejected(this.toUiStateModel())
        }
    }
    
    private fun Transfer.toUiStateModel(): TransferUiStateModel {
        return TransferUiStateModel(
            id = this.id,
            fowlId = this.fowlId,
            giverId = this.giverId,
            receiverId = this.receiverId,
            status = when (this.status) {
                com.rio.rostry.core.data.model.TransferStatus.PENDING -> TransferStatusUi.PENDING
                com.rio.rostry.core.data.model.TransferStatus.VERIFIED -> TransferStatusUi.VERIFIED
                com.rio.rostry.core.data.model.TransferStatus.REJECTED -> TransferStatusUi.REJECTED
            },
            verificationDetails = this.verificationDetails
        )
    }
}
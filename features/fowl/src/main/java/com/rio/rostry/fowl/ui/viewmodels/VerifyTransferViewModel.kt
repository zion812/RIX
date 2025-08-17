package com.rio.rostry.fowl.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.core.data.usecase.VerifyTransferUseCase
import com.rio.rostry.core.data.usecase.TransferVerificationData
import com.rio.rostry.fowl.ui.VerifyTransferUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerifyTransferViewModel @Inject constructor(
    private val verifyTransferUseCase: VerifyTransferUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(VerifyTransferUiState())
    val uiState: StateFlow<VerifyTransferUiState> = _uiState.asStateFlow()
    
    fun loadTransferDetails(transferId: String) {
        // In a real implementation, this would load the transfer details from a repository
        // For now, we'll just update the state to show it's not loading
        _uiState.value = _uiState.value.copy(isLoading = false)
    }
    
    fun onPriceChange(price: String) {
        _uiState.value = _uiState.value.copy(actualPrice = price)
        validateForm()
    }
    
    fun onColorChange(color: String) {
        _uiState.value = _uiState.value.copy(actualColor = color)
        validateForm()
    }
    
    fun onAgeChange(age: String) {
        _uiState.value = _uiState.value.copy(actualAge = age)
        validateForm()
    }
    
    fun onWeightChange(weight: String) {
        _uiState.value = _uiState.value.copy(actualWeight = weight)
        validateForm()
    }
    
    fun onPhotoReferenceChange(photoReference: String) {
        _uiState.value = _uiState.value.copy(photoReference = photoReference)
        validateForm()
    }
    
    private fun validateForm() {
        val state = _uiState.value
        val isFormValid = state.actualPrice.isNotBlank() && 
                state.actualColor.isNotBlank() && 
                state.actualAge.isNotBlank() && 
                state.actualWeight.isNotBlank()
        _uiState.value = state.copy(isFormValid = isFormValid)
    }
    
    fun verifyTransfer(
        transferId: String,
        verifierId: String = "current_user_id", // Would be retrieved from auth state
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            val state = _uiState.value
            val verificationData = TransferVerificationData(
                price = state.actualPrice.toIntOrNull() ?: 0,
                color = state.actualColor,
                ageInWeeks = state.actualAge.toIntOrNull() ?: 0,
                weightInGrams = state.actualWeight.toIntOrNull() ?: 0,
                photoReference = state.photoReference
            )
            
            val result = verifyTransferUseCase(
                transferId = transferId,
                verifierId = verifierId,
                verificationData = verificationData
            )
            
            when (result) {
                is com.rio.rostry.core.common.model.Result.Success -> {
                    onSuccess()
                }
                is com.rio.rostry.core.common.model.Result.Error -> {
                    onError(result.exception)
                }
                else -> {}
            }
        }
    }
}
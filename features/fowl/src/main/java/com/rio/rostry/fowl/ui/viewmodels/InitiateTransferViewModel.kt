package com.rio.rostry.fowl.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.core.data.usecase.InitiateTransferUseCase
import com.rio.rostry.core.data.usecase.TransferInitiationData
import com.rio.rostry.fowl.ui.InitiateTransferUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InitiateTransferViewModel @Inject constructor(
    private val initiateTransferUseCase: InitiateTransferUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(InitiateTransferUiState())
    val uiState: StateFlow<InitiateTransferUiState> = _uiState.asStateFlow()
    
    fun loadFowlDetails(fowlId: String) {
        // In a real implementation, this would load the fowl details from a repository
        // For now, we'll just update the state to show it's not loading
        _uiState.value = _uiState.value.copy(isLoading = false)
    }
    
    fun onPriceChange(price: String) {
        _uiState.value = _uiState.value.copy(expectedPrice = price)
        validateForm()
    }
    
    fun onColorChange(color: String) {
        _uiState.value = _uiState.value.copy(expectedColor = color)
        validateForm()
    }
    
    fun onAgeChange(age: String) {
        _uiState.value = _uiState.value.copy(expectedAge = age)
        validateForm()
    }
    
    fun onWeightChange(weight: String) {
        _uiState.value = _uiState.value.copy(expectedWeight = weight)
        validateForm()
    }
    
    fun onPhotoReferenceChange(photoReference: String) {
        _uiState.value = _uiState.value.copy(photoReference = photoReference)
        validateForm()
    }
    
    private fun validateForm() {
        val state = _uiState.value
        val isFormValid = state.expectedPrice.isNotBlank() && 
                state.expectedColor.isNotBlank() && 
                state.expectedAge.isNotBlank() && 
                state.expectedWeight.isNotBlank()
        _uiState.value = state.copy(isFormValid = isFormValid)
    }
    
    fun initiateTransfer(
        fowlId: String,
        fromUserId: String = "current_user_id", // Would be retrieved from auth state
        toUserId: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            val state = _uiState.value
            val transferData = TransferInitiationData(
                price = state.expectedPrice.toIntOrNull() ?: 0,
                color = state.expectedColor,
                ageInWeeks = state.expectedAge.toIntOrNull() ?: 0,
                weightInGrams = state.expectedWeight.toIntOrNull() ?: 0,
                photoReference = state.photoReference
            )
            
            val result = initiateTransferUseCase(
                fowlId = fowlId,
                fromUserId = fromUserId,
                toUserId = toUserId,
                transferData = transferData
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
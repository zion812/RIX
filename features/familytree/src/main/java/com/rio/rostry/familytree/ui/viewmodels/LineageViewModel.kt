package com.rio.rostry.familytree.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.core.data.usecase.GetFowlLineageUseCase
import com.rio.rostry.familytree.ui.LineageUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LineageViewModel @Inject constructor(
    private val getFowlLineageUseCase: GetFowlLineageUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LineageUiState())
    val uiState: StateFlow<LineageUiState> = _uiState.asStateFlow()
    
    fun loadLineage(fowlId: String) {
        viewModelScope.launch {
            _uiState.value = LineageUiState(isLoading = true)
            
            val result = getFowlLineageUseCase(fowlId)
            
            when (result) {
                is com.rio.rostry.core.common.model.Result.Success -> {
                    _uiState.value = LineageUiState(lineageInfo = result.data)
                }
                is com.rio.rostry.core.common.model.Result.Error -> {
                    _uiState.value = LineageUiState(error = result.exception.message)
                }
                else -> {
                    _uiState.value = LineageUiState(error = "Unknown error")
                }
            }
        }
    }
}
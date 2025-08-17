package com.rio.rostry.fowl.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.core.data.usecase.CreateTimelineEventUseCase
import com.rio.rostry.core.data.usecase.TimelineCreationData
import com.rio.rostry.fowl.ui.AddTimelineEventUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTimelineEventViewModel @Inject constructor(
    private val createTimelineEventUseCase: CreateTimelineEventUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddTimelineEventUiState())
    val uiState: StateFlow<AddTimelineEventUiState> = _uiState.asStateFlow()
    
    fun setFowlId(fowlId: String) {
        // In a real implementation, this would load the fowl details from a repository
        // For now, we'll just update the state to show it's not loading
        _uiState.value = _uiState.value.copy(isLoading = false)
    }
    
    fun onEventTypeChange(eventType: String) {
        _uiState.value = _uiState.value.copy(
            selectedEventType = eventType,
            isEventTypeMenuExpanded = false
        )
        validateForm()
    }
    
    fun onTitleChange(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
        validateForm()
    }
    
    fun onDescriptionChange(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
        validateForm()
    }
    
    fun onMediaReferenceChange(mediaReference: String) {
        _uiState.value = _uiState.value.copy(mediaReference = mediaReference)
        validateForm()
    }
    
    private fun validateForm() {
        val state = _uiState.value
        val isFormValid = state.selectedEventType.isNotBlank() && 
                state.title.isNotBlank()
        _uiState.value = state.copy(isFormValid = isFormValid)
    }
    
    fun addEvent(
        currentUserID: String = "current_user_id", // Would be retrieved from auth state
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            val state = _uiState.value
            
            val timelineData = TimelineCreationData(
                fowlId = "fowl_id_placeholder", // Would be set from the fowlId parameter
                eventType = state.selectedEventType,
                title = state.title,
                description = state.description,
                mediaReferences = if (state.mediaReference.isNotBlank()) listOf(state.mediaReference) else emptyList(),
                metadata = emptyMap(),
                latitude = null,
                longitude = null,
                createdBy = currentUserID
            )
            
            val result = createTimelineEventUseCase(timelineData)
            
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
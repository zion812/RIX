package com.rio.rostry.fowl.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.core.data.repository.FowlRepositoryImpl
import com.rio.rostry.core.database.entities.FowlEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SimpleFowlViewModel @Inject constructor(
    private val fowlRepository: FowlRepositoryImpl
) : ViewModel() {

    private val _fowlList = MutableStateFlow<List<FowlEntity>>(emptyList())
    val fowlList: StateFlow<List<FowlEntity>> = _fowlList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadFowls(ownerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val fowls = fowlRepository.getFowlsByOwner(ownerId)
                _fowlList.value = fowls
            } catch (e: Exception) {
                _error.value = "Failed to load fowls: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addFowl(
        ownerId: String,
        name: String,
        breed: String,
        region: String,
        district: String
    ) {
        viewModelScope.launch {
            try {
                val newFowl = FowlEntity(
                    id = UUID.randomUUID().toString(),
                    ownerId = ownerId,
                    name = name,
                    breedPrimary = breed,
                    healthStatus = "GOOD",
                    availabilityStatus = "AVAILABLE",
                    region = region,
                    district = district,
                    createdAt = Date(),
                    updatedAt = Date()
                    // Other fields will use their default values from the data class
                )
                fowlRepository.saveFowl(newFowl).getOrThrow()
                // Refresh the list
                loadFowls(ownerId)
            } catch (e: Exception) {
                _error.value = "Failed to add fowl: ${e.message}"
            }
        }
    }

    fun deleteFowl(fowlId: String, ownerId: String) {
        viewModelScope.launch {
            try {
                fowlRepository.deleteFowl(fowlId).getOrThrow()
                // Refresh the list
                loadFowls(ownerId)
            } catch (e: Exception) {
                _error.value = "Failed to delete fowl: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    private val _selectedFowl = MutableStateFlow<FowlEntity?>(null)
    val selectedFowl: StateFlow<FowlEntity?> = _selectedFowl.asStateFlow()

    fun getFowlById(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val fowl = fowlRepository.getFowlById(id)
                _selectedFowl.value = fowl
                if (fowl == null) {
                    _error.value = "Fowl not found."
                }
            } catch (e: Exception) {
                _error.value = "Failed to load fowl details: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateFowl(updatedFowl: FowlEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                fowlRepository.updateFowl(updatedFowl).getOrThrow()
                // Also update the selected fowl state to reflect changes immediately
                _selectedFowl.value = updatedFowl
            } catch (e: Exception) {
                _error.value = "Failed to update fowl: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

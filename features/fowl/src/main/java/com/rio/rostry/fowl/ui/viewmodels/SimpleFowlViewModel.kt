package com.rio.rostry.fowl.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.core.database.RIODatabase
import com.rio.rostry.core.database.entities.FowlEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

/**
 * Simplified ViewModel for fowl management - Phase 2.1
 * Works without Hilt for Kotlin 2.0 compatibility
 */
class SimpleFowlViewModel(
    private val database: RIODatabase
) : ViewModel() {

    private val fowlDao = database.fowlDao()
    private val _fowlList = MutableStateFlow<List<FowlEntity>>(emptyList())
    val fowlList: StateFlow<List<FowlEntity>> = _fowlList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Load all fowls for the current user
     */
    fun loadFowls() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // For now, get fowls by owner (using demo user ID)
                val fowls = fowlDao.getFowlsByOwner("current_user")
                _fowlList.value = fowls
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load fowls"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Add a new fowl
     */
    fun addFowl(
        name: String,
        breed: String,
        weight: Float,
        region: String,
        district: String
    ) {
        viewModelScope.launch {
            try {
                val fowlEntity = FowlEntity(
                    id = UUID.randomUUID().toString(),
                    ownerId = "current_user", // TODO: Get from auth
                    name = name.takeIf { it.isNotBlank() } ?: "",
                    breed = breed,
                    gender = "UNKNOWN", // Default for now
                    weight = if (weight > 0) weight.toDouble() else null,
                    color = null,
                    description = null,
                    region = region,
                    district = district,
                    createdAt = java.util.Date()
                )

                fowlDao.insertFowl(fowlEntity)

                // Refresh the list
                loadFowls()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add fowl"
            }
        }
    }

    /**
     * Delete a fowl
     */
    fun deleteFowl(fowlId: String) {
        viewModelScope.launch {
            try {
                fowlDao.deleteFowlById(fowlId)

                // Refresh the list
                loadFowls()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete fowl"
            }
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }
}

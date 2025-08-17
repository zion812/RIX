package com.rio.rostry.fowl.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.core.data.model.FowlRecord
import com.rio.rostry.core.data.model.FowlRecordListItem
import com.rio.rostry.core.data.repository.FowlRecordRepository
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
    private val fowlRepository: FowlRepositoryImpl,
    private val fowlRecordRepository: FowlRecordRepository
) : ViewModel() {

    private val _fowlList = MutableStateFlow<List<FowlEntity>>(emptyList())
    val fowlList: StateFlow<List<FowlEntity>> = _fowlList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedFowl = MutableStateFlow<FowlEntity?>(null)
    val selectedFowl: StateFlow<FowlEntity?> = _selectedFowl.asStateFlow()
    
    private val _fowlRecords = MutableStateFlow<List<FowlRecord>>(emptyList())
    val fowlRecords: StateFlow<List<FowlRecord>> = _fowlRecords.asStateFlow()
    
    private val _fowlRecordListItems = MutableStateFlow<List<FowlRecordListItem>>(emptyList())
    val fowlRecordListItems: StateFlow<List<FowlRecordListItem>> = _fowlRecordListItems.asStateFlow()
    
    private var currentFowlId: String? = null
    private var currentPage = 0
    private val pageSize = 20
    private var allRecordsLoaded = false
    private var useLightweightProjection = true

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

    fun getFowlById(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val fowl = fowlRepository.getFowlById(id)
                _selectedFowl.value = fowl
                if (fowl == null) {
                    _error.value = "Fowl not found."
                } else {
                    // Load records for this fowl
                    currentFowlId = id
                    currentPage = 0
                    allRecordsLoaded = false
                    loadFowlRecords(id)
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
    
    private fun loadFowlRecords(fowlId: String) {
        viewModelScope.launch {
            try {
                if (useLightweightProjection) {
                    val result = fowlRecordRepository.getRecordListItemsByFowlIdPaged(
                        fowlId = fowlId,
                        limit = pageSize,
                        offset = currentPage * pageSize
                    )
                    
                    if (result is com.rio.rostry.core.common.model.Result.Success) {
                        val records = result.data
                        if (records.isEmpty()) {
                            allRecordsLoaded = true
                        } else {
                            // Add new records to existing list
                            val currentRecords = _fowlRecordListItems.value
                            _fowlRecordListItems.value = currentRecords + records
                        }
                    } else {
                        _error.value = "Failed to load fowl records"
                    }
                } else {
                    val result = fowlRecordRepository.getRecordsByFowlIdPaged(
                        fowlId = fowlId,
                        limit = pageSize,
                        offset = currentPage * pageSize
                    )
                    
                    if (result is com.rio.rostry.core.common.model.Result.Success) {
                        val records = result.data
                        if (records.isEmpty()) {
                            allRecordsLoaded = true
                        } else {
                            // Add new records to existing list
                            val currentRecords = _fowlRecords.value
                            _fowlRecords.value = currentRecords + records
                        }
                    } else {
                        _error.value = "Failed to load fowl records"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to load fowl records: ${e.message}"
            }
        }
    }
    
    fun loadMoreRecords() {
        if (allRecordsLoaded || currentFowlId == null) return
        
        currentPage++
        currentFowlId?.let { loadFowlRecords(it) }
    }
    
    fun addFowlRecord(record: FowlRecord) {
        viewModelScope.launch {
            try {
                val result = fowlRecordRepository.addRecord(record)
                if (result is com.rio.rostry.core.common.model.Result.Error) {
                    _error.value = "Failed to add record: ${result.exception.message}"
                } else {
                    // Reload records
                    _selectedFowl.value?.id?.let { 
                        currentFowlId = it
                        currentPage = 0
                        allRecordsLoaded = false
                        _fowlRecordListItems.value = emptyList()
                        _fowlRecords.value = emptyList()
                        loadFowlRecords(it)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to add record: ${e.message}"
            }
        }
    }
    
    fun refreshRecords() {
        _selectedFowl.value?.id?.let {
            currentFowlId = it
            currentPage = 0
            allRecordsLoaded = false
            _fowlRecordListItems.value = emptyList()
            _fowlRecords.value = emptyList()
            loadFowlRecords(it)
        }
    }
    
    fun toggleProjectionMode() {
        useLightweightProjection = !useLightweightProjection
        refreshRecords()
    }
}
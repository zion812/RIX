package com.rio.rostry.fowl.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.rio.rostry.core.common.base.BaseViewModel
import com.rio.rostry.core.common.model.*
import com.rio.rostry.fowl.domain.model.*
import com.rio.rostry.fowl.domain.usecases.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for fowl management operations
 */
@HiltViewModel
class FowlManagementViewModel @Inject constructor(
    private val getFowlsUseCase: GetFowlsUseCase,
    private val createFowlUseCase: CreateFowlUseCase,
    private val updateFowlUseCase: UpdateFowlUseCase,
    private val deleteFowlUseCase: DeleteFowlUseCase,
    private val searchFowlsUseCase: SearchFowlsUseCase,
    private val uploadFowlPhotoUseCase: UploadFowlPhotoUseCase,
    private val generateQRCodeUseCase: GenerateQRCodeUseCase,
    private val analyzeBreedUseCase: AnalyzeBreedUseCase,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    // User's fowl list
    private val _fowlList = MutableStateFlow<ListState<Fowl>>(ListState())
    val fowlList: StateFlow<ListState<Fowl>> = _fowlList.asStateFlow()

    // Selected fowl for details
    private val _selectedFowl = MutableStateFlow<Fowl?>(null)
    val selectedFowl: StateFlow<Fowl?> = _selectedFowl.asStateFlow()

    // ✅ Fowl registration form state with persistence
    private val _registrationState = MutableStateFlow(
        savedStateHandle.get<FowlRegistrationState>("fowl_registration_state") ?: FowlRegistrationState()
    )
    val registrationState: StateFlow<FowlRegistrationState> = _registrationState.asStateFlow()

    // ✅ Persist fowl draft data across configuration changes
    private val _fowlDraftData = savedStateHandle.getStateFlow(
        key = "fowl_draft_data",
        initialValue = FowlDraftData()
    )
    val fowlDraftData: StateFlow<FowlDraftData> = _fowlDraftData.asStateFlow()

    // Search state
    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    // Photo upload state
    private val _photoUploadState = MutableStateFlow(PhotoUploadState())
    val photoUploadState: StateFlow<PhotoUploadState> = _photoUploadState.asStateFlow()

    // Breed analysis state
    private val _breedAnalysisState = MutableStateFlow(BreedAnalysisState())
    val breedAnalysisState: StateFlow<BreedAnalysisState> = _breedAnalysisState.asStateFlow()

    // Pagination state
    private val _paginationState = MutableStateFlow(PaginationState())
    val paginationState: StateFlow<PaginationState> = _paginationState.asStateFlow()

    init {
        loadUserFowls()
        observeRegistrationStateChanges()
    }

    /**
     * ✅ Observe registration state changes and persist them
     */
    private fun observeRegistrationStateChanges() {
        viewModelScope.launch {
            _registrationState.collect { state ->
                savedStateHandle["fowl_registration_state"] = state
            }
        }
    }

    /**
     * ✅ Update fowl draft data with automatic persistence
     */
    fun updateFowlDraft(update: (FowlDraftData) -> FowlDraftData) {
        val currentDraft = fowlDraftData.value
        val updatedDraft = update(currentDraft)
        savedStateHandle["fowl_draft_data"] = updatedDraft
    }

    /**
     * ✅ Clear draft data after successful fowl creation
     */
    fun clearFowlDraft() {
        savedStateHandle.remove<FowlDraftData>("fowl_draft_data")
        savedStateHandle.remove<FowlRegistrationState>("fowl_registration_state")
    }

    /**
     * Load user's fowl list
     */
    fun loadUserFowls(refresh: Boolean = false) {
        executeWithTierCheck(UserTier.FARMER) {
            val currentUserId = getCurrentUserId() ?: return@executeWithTierCheck
            
            if (refresh) {
                _fowlList.value = _fowlList.value.copy(isRefreshing = true)
            } else {
                _fowlList.value = _fowlList.value.copy(isLoading = true)
            }

            executeWithResult(
                showLoading = false,
                action = { getFowlsUseCase(currentUserId) },
                onSuccess = { fowls ->
                    _fowlList.value = ListState(
                        items = fowls,
                        isLoading = false,
                        isRefreshing = false,
                        hasMore = fowls.size >= DEFAULT_PAGE_SIZE
                    )
                    logUserAction("fowls_loaded", mapOf("count" to fowls.size))
                },
                onError = { exception ->
                    _fowlList.value = _fowlList.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = exception.message
                    )
                }
            )
        }
    }

    /**
     * Search fowls with criteria
     */
    fun searchFowls(criteria: FowlSearchCriteria) {
        _searchState.value = _searchState.value.copy(isSearching = true)

        executeWithResult(
            showLoading = false,
            action = { searchFowlsUseCase(criteria, 0, DEFAULT_PAGE_SIZE) },
            onSuccess = { result ->
                _fowlList.value = ListState(
                    items = result.fowls,
                    isLoading = false,
                    hasMore = result.hasMore
                )
                _searchState.value = _searchState.value.copy(
                    isSearching = false,
                    query = criteria.query ?: ""
                )
                logUserAction("fowls_searched", mapOf(
                    "query" to (criteria.query ?: ""),
                    "breed" to (criteria.breed ?: ""),
                    "results" to result.fowls.size
                ))
            },
            onError = { exception ->
                _searchState.value = _searchState.value.copy(
                    isSearching = false
                )
                _fowlList.value = _fowlList.value.copy(
                    error = exception.message
                )
            }
        )
    }

    /**
     * Register a new fowl
     */
    fun registerFowl(fowlData: FowlRegistrationData) {
        executeWithTierCheck(UserTier.FARMER) {
            if (!validateFowlRegistration(fowlData)) return@executeWithTierCheck

            _registrationState.value = _registrationState.value.copy(isLoading = true, error = null)

            executeWithResult(
                showLoading = false,
                action = { createFowlUseCase(fowlData) },
                onSuccess = { fowl ->
                    _registrationState.value = _registrationState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                    
                    // Add to fowl list
                    val currentList = _fowlList.value.items.toMutableList()
                    currentList.add(0, fowl)
                    _fowlList.value = _fowlList.value.copy(items = currentList)
                    
                    // Generate QR code
                    generateQRCode(fowl.id)
                    
                    logUserAction("fowl_registered", mapOf(
                        "fowl_id" to fowl.id,
                        "breed" to fowl.breed.primary,
                        "gender" to fowl.gender.name
                    ))
                },
                onError = { exception ->
                    _registrationState.value = _registrationState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Registration failed"
                    )
                }
            )
        }
    }

    /**
     * Update fowl information
     */
    fun updateFowl(fowlId: String, updateData: FowlUpdateData) {
        executeWithTierCheck(UserTier.FARMER) {
            executeWithResult(
                action = { updateFowlUseCase(fowlId, updateData) },
                onSuccess = { updatedFowl ->
                    // Update in list
                    val currentList = _fowlList.value.items.toMutableList()
                    val index = currentList.indexOfFirst { it.id == fowlId }
                    if (index != -1) {
                        currentList[index] = updatedFowl
                        _fowlList.value = _fowlList.value.copy(items = currentList)
                    }
                    
                    // Update selected fowl if it's the same
                    if (_selectedFowl.value?.id == fowlId) {
                        _selectedFowl.value = updatedFowl
                    }
                    
                    logUserAction("fowl_updated", mapOf("fowl_id" to fowlId))
                }
            )
        }
    }

    /**
     * Delete a fowl
     */
    fun deleteFowl(fowlId: String) {
        executeWithTierCheck(UserTier.FARMER) {
            executeWithResult(
                action = { deleteFowlUseCase(fowlId) },
                onSuccess = {
                    // Remove from list
                    val currentList = _fowlList.value.items.toMutableList()
                    currentList.removeAll { it.id == fowlId }
                    _fowlList.value = _fowlList.value.copy(items = currentList)
                    
                    // Clear selected fowl if it's the same
                    if (_selectedFowl.value?.id == fowlId) {
                        _selectedFowl.value = null
                    }
                    
                    logUserAction("fowl_deleted", mapOf("fowl_id" to fowlId))
                }
            )
        }
    }

    /**
     * Upload fowl photo
     */
    fun uploadFowlPhoto(fowlId: String, imageData: ByteArray, photoType: PhotoType) {
        _photoUploadState.value = _photoUploadState.value.copy(
            isUploading = true,
            progress = 0f,
            error = null
        )

        executeWithResult(
            showLoading = false,
            action = { uploadFowlPhotoUseCase(fowlId, imageData, photoType) },
            onSuccess = { photoUrl ->
                _photoUploadState.value = _photoUploadState.value.copy(
                    isUploading = false,
                    isSuccess = true,
                    uploadedUrl = photoUrl
                )
                
                // Refresh fowl data to get updated photo info
                refreshFowlData(fowlId)
                
                logUserAction("fowl_photo_uploaded", mapOf(
                    "fowl_id" to fowlId,
                    "photo_type" to photoType.name
                ))
            },
            onError = { exception ->
                _photoUploadState.value = _photoUploadState.value.copy(
                    isUploading = false,
                    error = exception.message ?: "Upload failed"
                )
            }
        )
    }

    /**
     * Analyze breed from photo using AI
     */
    fun analyzeBreedFromPhoto(imageData: ByteArray) {
        _breedAnalysisState.value = _breedAnalysisState.value.copy(
            isAnalyzing = true,
            error = null
        )

        executeWithResult(
            showLoading = false,
            action = { analyzeBreedUseCase(imageData) },
            onSuccess = { analysis ->
                _breedAnalysisState.value = _breedAnalysisState.value.copy(
                    isAnalyzing = false,
                    analysis = analysis
                )
                
                logUserAction("breed_analyzed", mapOf(
                    "detected_breed" to (analysis.detectedBreed ?: "unknown"),
                    "confidence" to analysis.confidence
                ))
            },
            onError = { exception ->
                _breedAnalysisState.value = _breedAnalysisState.value.copy(
                    isAnalyzing = false,
                    error = exception.message ?: "Analysis failed"
                )
            }
        )
    }

    /**
     * Generate QR code for fowl
     */
    fun generateQRCode(fowlId: String) {
        executeWithResult(
            showLoading = false,
            action = { generateQRCodeUseCase(fowlId) },
            onSuccess = { qrCodeUrl ->
                // Update fowl with QR code URL
                val currentList = _fowlList.value.items.toMutableList()
                val index = currentList.indexOfFirst { it.id == fowlId }
                if (index != -1) {
                    val updatedFowl = currentList[index].copy(
                        documentation = currentList[index].documentation.copy(qrCode = qrCodeUrl)
                    )
                    currentList[index] = updatedFowl
                    _fowlList.value = _fowlList.value.copy(items = currentList)
                }
                
                logUserAction("qr_code_generated", mapOf("fowl_id" to fowlId))
            }
        )
    }

    /**
     * Select a fowl for detailed view
     */
    fun selectFowl(fowl: Fowl) {
        _selectedFowl.value = fowl
        logUserAction("fowl_selected", mapOf("fowl_id" to fowl.id))
    }

    /**
     * Clear selected fowl
     */
    fun clearSelectedFowl() {
        _selectedFowl.value = null
    }

    /**
     * Load more fowls for pagination
     */
    fun loadMoreFowls() {
        if (_paginationState.value.isLoadingMore || !_paginationState.value.hasMore) return

        _paginationState.value = _paginationState.value.copy(isLoadingMore = true)

        val currentUserId = getCurrentUserId() ?: return
        val nextPage = _paginationState.value.currentPage + 1

        executeWithResult(
            showLoading = false,
            action = { getFowlsUseCase(currentUserId, nextPage, DEFAULT_PAGE_SIZE) },
            onSuccess = { newFowls ->
                val currentList = _fowlList.value.items.toMutableList()
                currentList.addAll(newFowls)
                
                _fowlList.value = _fowlList.value.copy(items = currentList)
                _paginationState.value = _paginationState.value.copy(
                    currentPage = nextPage,
                    isLoadingMore = false,
                    hasMore = newFowls.size >= DEFAULT_PAGE_SIZE
                )
            },
            onError = {
                _paginationState.value = _paginationState.value.copy(isLoadingMore = false)
            }
        )
    }

    /**
     * Refresh fowl data
     */
    private fun refreshFowlData(fowlId: String) {
        // Implementation to refresh specific fowl data
        loadUserFowls(refresh = true)
    }

    /**
     * Validate fowl registration data
     */
    private fun validateFowlRegistration(fowlData: FowlRegistrationData): Boolean {
        val errors = mutableListOf<String>()

        if (fowlData.breed.primary.isBlank()) {
            errors.add("Breed is required")
        }

        if (fowlData.physicalTraits.weight <= 0) {
            errors.add("Weight must be greater than 0")
        }

        if (fowlData.physicalTraits.height <= 0) {
            errors.add("Height must be greater than 0")
        }

        if (fowlData.origin.region.isBlank()) {
            errors.add("Region is required")
        }

        if (fowlData.origin.district.isBlank()) {
            errors.add("District is required")
        }

        if (errors.isNotEmpty()) {
            _registrationState.value = _registrationState.value.copy(
                error = errors.joinToString(", ")
            )
            return false
        }

        return true
    }

    /**
     * Update registration form state
     */
    fun updateRegistrationForm(fowlData: FowlRegistrationData) {
        _registrationState.value = _registrationState.value.copy(
            fowlData = fowlData,
            error = null
        )
    }

    /**
     * Clear registration state
     */
    fun clearRegistrationState() {
        _registrationState.value = FowlRegistrationState()
    }

    /**
     * Clear photo upload state
     */
    fun clearPhotoUploadState() {
        _photoUploadState.value = PhotoUploadState()
    }

    /**
     * Clear breed analysis state
     */
    fun clearBreedAnalysisState() {
        _breedAnalysisState.value = BreedAnalysisState()
    }

    override fun refreshData() {
        loadUserFowls(refresh = true)
    }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
    }
}

/**
 * Fowl registration state
 */
data class FowlRegistrationState(
    val fowlData: FowlRegistrationData? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

/**
 * Photo upload state
 */
data class PhotoUploadState(
    val isUploading: Boolean = false,
    val progress: Float = 0f,
    val isSuccess: Boolean = false,
    val uploadedUrl: String? = null,
    val error: String? = null
)

/**
 * Breed analysis state
 */
data class BreedAnalysisState(
    val isAnalyzing: Boolean = false,
    val analysis: AIAnalysis? = null,
    val error: String? = null
)

/**
 * Fowl registration data
 */
data class FowlRegistrationData(
    val name: String? = null,
    val breed: BreedInfo,
    val gender: FowlGender,
    val physicalTraits: PhysicalTraits,
    val birthInfo: BirthInfo,
    val lineage: LineageInfo = LineageInfo(),
    val origin: OriginInfo,
    val documentation: Documentation = Documentation(),
    val tags: List<String> = emptyList()
)

/**
 * Fowl update data
 */
data class FowlUpdateData(
    val name: String? = null,
    val physicalTraits: PhysicalTraits? = null,
    val status: FowlStatus? = null,
    val performance: PerformanceMetrics? = null,
    val tags: List<String>? = null,
    val notes: String? = null
)

package com.rio.rostry.ui.familytree

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.core.database.entities.RoosterEntity
import com.rio.rostry.core.database.entities.BreedingRecordEntity
import com.rio.rostry.core.database.entities.TransferEntity
import com.rio.rostry.domain.repository.RoosterRepository
import com.rio.rostry.domain.repository.BreedingRepository
import com.rio.rostry.domain.repository.TransferRepository
import com.rio.rostry.ui.familytree.model.FowlNode
import com.rio.rostry.ui.familytree.model.TreeConnection
import com.rio.rostry.ui.familytree.model.TreeStatistics
import com.rio.rostry.ui.familytree.model.TreeSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the family tree screen
 * Manages tree data, user interactions, and UI state
 */
@HiltViewModel
class FamilyTreeViewModel @Inject constructor(
    private val roosterRepository: RoosterRepository,
    private val breedingRepository: BreedingRepository,
    private val transferRepository: TransferRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FamilyTreeUiState())
    val uiState: StateFlow<FamilyTreeUiState> = _uiState.asStateFlow()

    private var currentRoosterId: String? = null
    private var allFamilyData: List<RoosterEntity> = emptyList()

    /**
     * Load family tree data for a specific rooster
     */
    fun loadFamilyTree(roosterId: String) {
        currentRoosterId = roosterId
        
        viewModelScope.launch {
            _uiState.update { it.copy(loadingState = LoadingState.Loading) }
            
            try {
                // Load root rooster
                val rootRooster = roosterRepository.getRoosterById(roosterId)
                if (rootRooster == null) {
                    _uiState.update { 
                        it.copy(
                            loadingState = LoadingState.Error,
                            errorMessage = "Rooster not found"
                        )
                    }
                    return@launch
                }

                // Load complete family tree data
                val familyData = loadCompleteFamilyData(roosterId)
                allFamilyData = familyData

                // Calculate tree statistics
                val statistics = calculateTreeStatistics(familyData)

                _uiState.update {
                    it.copy(
                        loadingState = LoadingState.Success,
                        rootFowl = rootRooster,
                        familyData = familyData,
                        treeStatistics = statistics,
                        totalGenerations = statistics.totalGenerations,
                        errorMessage = null
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loadingState = LoadingState.Error,
                        errorMessage = e.message ?: "Failed to load family tree"
                    )
                }
            }
        }
    }

    private suspend fun loadCompleteFamilyData(roosterId: String): List<RoosterEntity> {
        val visited = mutableSetOf<String>()
        val familyMembers = mutableListOf<RoosterEntity>()
        
        // Load ancestors and descendants recursively
        loadFamilyRecursive(roosterId, visited, familyMembers, maxDepth = 10)
        
        return familyMembers
    }

    private suspend fun loadFamilyRecursive(
        roosterId: String,
        visited: MutableSet<String>,
        familyMembers: MutableList<RoosterEntity>,
        maxDepth: Int
    ) {
        if (roosterId in visited || maxDepth <= 0) return
        
        visited.add(roosterId)
        
        val rooster = roosterRepository.getRoosterById(roosterId)
        if (rooster != null) {
            familyMembers.add(rooster)
            
            // Load parents
            rooster.fatherId?.let { fatherId ->
                loadFamilyRecursive(fatherId, visited, familyMembers, maxDepth - 1)
            }
            rooster.motherId?.let { motherId ->
                loadFamilyRecursive(motherId, visited, familyMembers, maxDepth - 1)
            }
            
            // Load children
            val children = roosterRepository.getChildrenOf(roosterId)
            children.forEach { child ->
                loadFamilyRecursive(child.id, visited, familyMembers, maxDepth - 1)
            }
        }
    }

    private fun calculateTreeStatistics(familyData: List<RoosterEntity>): TreeStatistics {
        val generations = mutableMapOf<Int, Int>()
        var maxGeneration = 0
        var verifiedCount = 0
        var breedingAgeCount = 0
        
        familyData.forEach { fowl ->
            val generation = calculateGeneration(fowl, familyData)
            generations[generation] = generations.getOrDefault(generation, 0) + 1
            maxGeneration = maxOf(maxGeneration, generation)
            
            if (fowl.lineageVerified) verifiedCount++
            
            // Calculate age and check breeding eligibility
            val ageMonths = calculateAgeInMonths(fowl)
            if (ageMonths in 6..60) breedingAgeCount++
        }
        
        return TreeStatistics(
            totalFowl = familyData.size,
            totalGenerations = maxGeneration + 1,
            verifiedLineages = verifiedCount,
            breedingAgeFowl = breedingAgeCount,
            generationDistribution = generations
        )
    }

    private fun calculateGeneration(fowl: RoosterEntity, allFowl: List<RoosterEntity>): Int {
        // Simple generation calculation - can be enhanced
        var generation = 0
        var current = fowl
        val visited = mutableSetOf<String>()
        
        while (current.fatherId != null || current.motherId != null) {
            if (current.id in visited) break
            visited.add(current.id)
            
            val parent = allFowl.find { it.id == current.fatherId || it.id == current.motherId }
            if (parent != null) {
                current = parent
                generation++
                if (generation > 10) break
            } else {
                break
            }
        }
        
        return generation
    }

    private fun calculateAgeInMonths(fowl: RoosterEntity): Int {
        return fowl.birthDate?.let { birth ->
            val now = java.util.Calendar.getInstance()
            val birthCal = java.util.Calendar.getInstance().apply { time = birth }
            
            val years = now.get(java.util.Calendar.YEAR) - birthCal.get(java.util.Calendar.YEAR)
            val months = now.get(java.util.Calendar.MONTH) - birthCal.get(java.util.Calendar.MONTH)
            
            years * 12 + months
        } ?: 0
    }

    /**
     * Select a fowl in the tree
     */
    fun selectFowl(fowl: RoosterEntity) {
        _uiState.update { it.copy(selectedFowl = fowl) }
    }

    /**
     * Show context menu for a fowl
     */
    fun showFowlContextMenu(fowl: RoosterEntity) {
        _uiState.update { 
            it.copy(
                selectedFowl = fowl,
                showFowlContextMenu = true
            )
        }
    }

    /**
     * Hide fowl context menu
     */
    fun hideFowlContextMenu() {
        _uiState.update { it.copy(showFowlContextMenu = false) }
    }

    /**
     * Show connection details
     */
    fun showConnectionDetails(connection: TreeConnection) {
        // Implementation for showing connection details
    }

    /**
     * Zoom in on the tree
     */
    fun zoomIn() {
        val currentZoom = _uiState.value.targetZoom
        val newZoom = (currentZoom * 1.2f).coerceAtMost(3.0f)
        _uiState.update { it.copy(targetZoom = newZoom) }
    }

    /**
     * Zoom out on the tree
     */
    fun zoomOut() {
        val currentZoom = _uiState.value.targetZoom
        val newZoom = (currentZoom / 1.2f).coerceAtLeast(0.1f)
        _uiState.update { it.copy(targetZoom = newZoom) }
    }

    /**
     * Center the tree view
     */
    fun centerTree() {
        _uiState.update { it.copy(shouldCenterTree = true) }
    }

    /**
     * Called when tree has been centered
     */
    fun onTreeCentered() {
        _uiState.update { it.copy(shouldCenterTree = false) }
    }

    /**
     * Filter by generation
     */
    fun filterByGeneration(generation: Int?) {
        _uiState.update { 
            it.copy(
                selectedGeneration = generation,
                familyData = if (generation == null) {
                    allFamilyData
                } else {
                    allFamilyData.filter { fowl ->
                        calculateGeneration(fowl, allFamilyData) == generation
                    }
                }
            )
        }
    }

    /**
     * Show add fowl dialog
     */
    fun showAddFowlDialog() {
        _uiState.update { it.copy(showAddFowlDialog = true) }
    }

    /**
     * Hide add fowl dialog
     */
    fun hideAddFowlDialog() {
        _uiState.update { it.copy(showAddFowlDialog = false) }
    }

    /**
     * Add fowl to tree
     */
    fun addFowlToTree(parentId: String?, fowlData: RoosterEntity) {
        viewModelScope.launch {
            try {
                // Add fowl to repository
                roosterRepository.insertRooster(fowlData)
                
                // Reload family tree
                currentRoosterId?.let { loadFamilyTree(it) }
                
                hideAddFowlDialog()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to add fowl: ${e.message}")
                }
            }
        }
    }

    /**
     * Show tree settings
     */
    fun showTreeSettings() {
        _uiState.update { it.copy(showTreeSettings = true) }
    }

    /**
     * Hide tree settings
     */
    fun hideTreeSettings() {
        _uiState.update { it.copy(showTreeSettings = false) }
    }

    /**
     * Update tree settings
     */
    fun updateTreeSettings(settings: TreeSettings) {
        _uiState.update { it.copy(treeSettings = settings) }
    }

    /**
     * Show/hide statistics
     */
    fun toggleStatistics() {
        _uiState.update { it.copy(showStatistics = !it.showStatistics) }
    }

    /**
     * Hide statistics
     */
    fun hideStatistics() {
        _uiState.update { it.copy(showStatistics = false) }
    }

    /**
     * Focus on fowl lineage
     */
    fun focusOnFowlLineage(fowl: RoosterEntity) {
        // Implementation for focusing on specific fowl lineage
        selectFowl(fowl)
        centerTree()
    }

    /**
     * Request lineage verification
     */
    fun requestLineageVerification(fowl: RoosterEntity) {
        viewModelScope.launch {
            try {
                // Implementation for requesting lineage verification
                // This would integrate with the verification system
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to request verification: ${e.message}")
                }
            }
        }
    }

    /**
     * Export family tree
     */
    fun exportFamilyTree() {
        viewModelScope.launch {
            try {
                // Implementation for exporting family tree
                // This could generate PDF, image, or data export
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to export tree: ${e.message}")
                }
            }
        }
    }

    /**
     * Print family tree
     */
    fun printFamilyTree() {
        viewModelScope.launch {
            try {
                // Implementation for printing family tree
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to print tree: ${e.message}")
                }
            }
        }
    }
}

/**
 * UI state for the family tree screen
 */
data class FamilyTreeUiState(
    val loadingState: LoadingState = LoadingState.Loading,
    val rootFowl: RoosterEntity? = null,
    val familyData: List<RoosterEntity> = emptyList(),
    val selectedFowl: RoosterEntity? = null,
    val selectedGeneration: Int? = null,
    val totalGenerations: Int = 0,
    val targetZoom: Float = 1.0f,
    val shouldCenterTree: Boolean = false,
    val showFowlContextMenu: Boolean = false,
    val showAddFowlDialog: Boolean = false,
    val showTreeSettings: Boolean = false,
    val showStatistics: Boolean = false,
    val treeStatistics: TreeStatistics = TreeStatistics(),
    val treeSettings: TreeSettings = TreeSettings(),
    val errorMessage: String? = null
)

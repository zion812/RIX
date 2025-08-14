package com.rio.rostry.features.familytree.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.core.common.model.Result
import com.rio.rostry.features.familytree.domain.model.FowlNode
import com.rio.rostry.features.familytree.domain.repository.FamilyTreeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FamilyTreeUiState(
    val nodes: List<FowlNode> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showVerifiedOnly: Boolean = false,
    val generationDepth: Int = 3
)

@HiltViewModel
class FamilyTreeViewModel @Inject constructor(
    private val repository: FamilyTreeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FamilyTreeUiState())
    val uiState: StateFlow<FamilyTreeUiState> = _uiState.asStateFlow()

    init {
        loadFamilyTree()
    }

    fun loadFamilyTree() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                repository.getFamilyTree(
                    verifiedOnly = _uiState.value.showVerifiedOnly,
                    maxDepth = _uiState.value.generationDepth
                ).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _uiState.update { 
                                it.copy(
                                    nodes = result.data,
                                    isLoading = false,
                                    error = null
                                )
                            }
                        }
                        is Result.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = result.exception.message
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun toggleVerifiedFilter() {
        _uiState.update { 
            it.copy(showVerifiedOnly = !it.showVerifiedOnly)
        }
        loadFamilyTree()
    }

    fun setGenerationDepth(depth: Int) {
        _uiState.update { 
            it.copy(generationDepth = depth)
        }
        loadFamilyTree()
    }
}

package com.rio.rostry.familytree.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rio.rostry.familytree.ui.viewmodels.LineageViewModel

/**
 * Screen for displaying fowl lineage information
 */
@Composable
fun LineageScreen(
    fowlId: String,
    onFowlSelected: (fowlId: String) -> Unit,
    viewModel: LineageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(fowlId) {
        viewModel.loadLineage(fowlId)
    }
    
    LineageContent(
        uiState = uiState,
        onFowlSelected = onFowlSelected,
        onRetry = { viewModel.loadLineage(fowlId) }
    )
}

@Composable
fun LineageContent(
    uiState: LineageUiState,
    onFowlSelected: (fowlId: String) -> Unit,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }
            uiState.error != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Error loading lineage information",
                        style = MaterialTheme.typography.h6
                    )
                    Text(
                        text = uiState.error,
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }
            uiState.lineageInfo != null -> {
                LineageVisualization(
                    lineageInfo = uiState.lineageInfo!!,
                    onFowlSelected = onFowlSelected,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Text("No lineage information available")
            }
        }
    }
}

data class LineageUiState(
    val isLoading: Boolean = false,
    val lineageInfo: com.rio.rostry.core.data.model.LineageInfo? = null,
    val error: String? = null
)
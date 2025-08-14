package com.rio.rostry.features.familytree.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rio.rostry.features.familytree.ui.components.FamilyTreeGraph
import com.rio.rostry.features.familytree.ui.viewmodel.FamilyTreeViewModel

@Composable
fun FamilyTreeScreen(
    viewModel: FamilyTreeViewModel = hiltViewModel(),
    onNavigateToFowlDetails: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Family Tree") },
                actions = {
                    // Verification status filter
                    FilterChip(
                        selected = uiState.showVerifiedOnly,
                        onClick = { viewModel.toggleVerifiedFilter() },
                        label = { Text("Verified Only") }
                    )
                    // Generation depth selector
                    DropdownMenu(
                        expanded = false,
                        onDismissRequest = { },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        (1..5).forEach { depth ->
                            DropdownMenuItem(
                                text = { Text("Gen $depth") },
                                onClick = { viewModel.setGenerationDepth(depth) }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                    )
                }
                uiState.nodes.isEmpty() -> {
                    Text(
                        text = "No family tree data available",
                        modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                    )
                }
                else -> {
                    FamilyTreeGraph(
                        nodes = uiState.nodes,
                        modifier = Modifier.fillMaxSize(),
                        onNodeClick = { node ->
                            onNavigateToFowlDetails(node.id)
                        }
                    )
                }
            }
        }
    }
}

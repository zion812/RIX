package com.rio.rostry.ui.familytree

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rio.rostry.core.database.entities.RoosterEntity
import com.rio.rostry.ui.components.LoadingIndicator
import com.rio.rostry.ui.components.ErrorMessage

/**
 * Family tree screen showing interactive fowl lineage visualization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyTreeScreen(
    roosterId: String,
    onNavigateBack: () -> Unit,
    onNavigateToFowlDetail: (String) -> Unit,
    onNavigateToBreeding: (String) -> Unit,
    onNavigateToTransfer: (String) -> Unit,
    viewModel: FamilyTreeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Initialize with rooster ID
    LaunchedEffect(roosterId) {
        viewModel.loadFamilyTree(roosterId)
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top app bar
        TopAppBar(
            title = { 
                Text(
                    text = uiState.rootFowl?.name?.let { "Family Tree - $it" } ?: "Family Tree"
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                // Zoom controls
                IconButton(
                    onClick = { viewModel.zoomIn() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom In"
                    )
                }
                
                IconButton(
                    onClick = { viewModel.zoomOut() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomOut,
                        contentDescription = "Zoom Out"
                    )
                }
                
                IconButton(
                    onClick = { viewModel.centerTree() }
                ) {
                    Icon(
                        imageVector = Icons.Default.CenterFocusStrong,
                        contentDescription = "Center Tree"
                    )
                }
                
                // More options
                var showMenu by remember { mutableStateOf(false) }
                
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Export Tree") },
                        onClick = {
                            showMenu = false
                            viewModel.exportFamilyTree()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Share, contentDescription = null)
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("Print Tree") },
                        onClick = {
                            showMenu = false
                            viewModel.printFamilyTree()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Print, contentDescription = null)
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("Tree Settings") },
                        onClick = {
                            showMenu = false
                            viewModel.showTreeSettings()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Settings, contentDescription = null)
                        }
                    )
                }
            }
        )
        
        // Main content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (uiState.loadingState) {
                LoadingState.Loading -> {
                    LoadingIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        message = "Loading family tree..."
                    )
                }
                
                LoadingState.Error -> {
                    ErrorMessage(
                        message = uiState.errorMessage ?: "Failed to load family tree",
                        onRetry = { viewModel.loadFamilyTree(roosterId) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                LoadingState.Success -> {
                    if (uiState.rootFowl != null && uiState.familyData.isNotEmpty()) {
                        // Family tree view
                        AndroidView(
                            factory = { context ->
                                FowlFamilyTreeView(context).apply {
                                    // Set up click listeners
                                    onFowlClickListener = { fowl ->
                                        viewModel.selectFowl(fowl)
                                    }
                                    
                                    onFowlLongClickListener = { fowl ->
                                        viewModel.showFowlContextMenu(fowl)
                                    }
                                    
                                    onConnectionClickListener = { connection ->
                                        viewModel.showConnectionDetails(connection)
                                    }
                                }
                            },
                            update = { view ->
                                uiState.rootFowl?.let { root ->
                                    view.setRootFowl(root, uiState.familyData)
                                }
                                
                                // Update zoom if changed
                                if (uiState.targetZoom != view.getCurrentZoom()) {
                                    view.setZoom(uiState.targetZoom, true)
                                }
                                
                                // Center tree if requested
                                if (uiState.shouldCenterTree) {
                                    view.centerTree()
                                    viewModel.onTreeCentered()
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Floating action button for adding new fowl
                        FloatingActionButton(
                            onClick = { viewModel.showAddFowlDialog() },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Fowl"
                            )
                        }
                        
                        // Tree statistics overlay
                        if (uiState.showStatistics) {
                            TreeStatisticsOverlay(
                                statistics = uiState.treeStatistics,
                                onDismiss = { viewModel.hideStatistics() },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp)
                            )
                        }
                    } else {
                        // Empty state
                        EmptyFamilyTreeView(
                            onAddFirstFowl = { viewModel.showAddFowlDialog() },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
        
        // Bottom controls
        if (uiState.loadingState == LoadingState.Success) {
            BottomTreeControls(
                selectedFowl = uiState.selectedFowl,
                onBreedingClick = { fowl ->
                    onNavigateToBreeding(fowl.id)
                },
                onTransferClick = { fowl ->
                    onNavigateToTransfer(fowl.id)
                },
                onDetailsClick = { fowl ->
                    onNavigateToFowlDetail(fowl.id)
                },
                onGenerationFilter = { generation ->
                    viewModel.filterByGeneration(generation)
                },
                currentGeneration = uiState.selectedGeneration,
                totalGenerations = uiState.totalGenerations
            )
        }
    }
    
    // Dialogs and bottom sheets
    if (uiState.showFowlContextMenu) {
        uiState.selectedFowl?.let { fowl ->
            FowlContextMenuBottomSheet(
                fowl = fowl,
                onDismiss = { viewModel.hideFowlContextMenu() },
                onEdit = { onNavigateToFowlDetail(fowl.id) },
                onBreeding = { onNavigateToBreeding(fowl.id) },
                onTransfer = { onNavigateToTransfer(fowl.id) },
                onViewLineage = { viewModel.focusOnFowlLineage(fowl) },
                onVerifyLineage = { viewModel.requestLineageVerification(fowl) }
            )
        }
    }
    
    if (uiState.showAddFowlDialog) {
        AddFowlToTreeDialog(
            parentFowl = uiState.selectedFowl,
            onDismiss = { viewModel.hideAddFowlDialog() },
            onConfirm = { parentId, fowlData ->
                viewModel.addFowlToTree(parentId, fowlData)
            }
        )
    }
    
    if (uiState.showTreeSettings) {
        TreeSettingsBottomSheet(
            settings = uiState.treeSettings,
            onDismiss = { viewModel.hideTreeSettings() },
            onSettingsChange = { newSettings ->
                viewModel.updateTreeSettings(newSettings)
            }
        )
    }
}

@Composable
fun BottomTreeControls(
    selectedFowl: RoosterEntity?,
    onBreedingClick: (RoosterEntity) -> Unit,
    onTransferClick: (RoosterEntity) -> Unit,
    onDetailsClick: (RoosterEntity) -> Unit,
    onGenerationFilter: (Int?) -> Unit,
    currentGeneration: Int?,
    totalGenerations: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Generation filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Generation Filter:",
                    style = MaterialTheme.typography.labelMedium
                )
                
                Row {
                    FilterChip(
                        onClick = { onGenerationFilter(null) },
                        label = { Text("All") },
                        selected = currentGeneration == null
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    repeat(totalGenerations.coerceAtMost(5)) { generation ->
                        FilterChip(
                            onClick = { onGenerationFilter(generation) },
                            label = { Text("G$generation") },
                            selected = currentGeneration == generation
                        )
                        
                        if (generation < totalGenerations - 1) {
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                }
            }
            
            // Selected fowl actions
            selectedFowl?.let { fowl ->
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Selected: ${fowl.name}",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onDetailsClick(fowl) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Details")
                    }
                    
                    OutlinedButton(
                        onClick = { onBreedingClick(fowl) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Breeding")
                    }
                    
                    OutlinedButton(
                        onClick = { onTransferClick(fowl) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Transfer")
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyFamilyTreeView(
    onAddFirstFowl: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.AccountTree,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Family Tree Data",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Start building your fowl family tree by adding the first member",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onAddFirstFowl
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add First Fowl")
        }
    }
}

enum class LoadingState {
    Loading, Success, Error
}

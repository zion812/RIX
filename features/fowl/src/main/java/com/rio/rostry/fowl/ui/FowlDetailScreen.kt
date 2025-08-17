package com.rio.rostry.fowl.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rio.rostry.core.database.entities.FowlEntity
import com.rio.rostry.fowl.ui.viewmodels.SimpleFowlViewModel
import com.rio.rostry.core.navigation.RIONavigation
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FowlDetailScreen(
    fowlId: String,
    navController: NavController,
    viewModel: SimpleFowlViewModel = hiltViewModel()
) {
    val fowl by viewModel.selectedFowl.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val records by viewModel.fowlRecords.collectAsState()
    val recordListItems by viewModel.fowlRecordListItems.collectAsState()

    LaunchedEffect(fowlId) {
        viewModel.getFowlById(fowlId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(fowl?.name ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        navController.navigate(RIONavigation.Screen.FowlAddRecord.createRoute(fowlId))
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Record")
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
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                error != null -> {
                    Text(
                        text = "Error: $error",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                fowl != null -> {
                    FowlDetailContent(fowl!!, records, recordListItems, viewModel, navController)
                }
            }
        }
    }
}

@Composable
fun FowlDetailContent(
    fowl: FowlEntity, 
    records: List<com.rio.rostry.core.data.model.FowlRecord>,
    recordListItems: List<com.rio.rostry.core.data.model.FowlRecordListItem>,
    viewModel: SimpleFowlViewModel,
    navController: NavController
) {
    val listState = rememberLazyListState()
    
    // Load more when scrolled to the end
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= if (recordListItems.isNotEmpty()) recordListItems.size else records.size - 5) {
                    viewModel.loadMoreRecords()
                }
            }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DetailCard(title = "Primary Information") {
                DetailItem("Name", fowl.name ?: "N/A")
                DetailItem("Primary Breed", fowl.breedPrimary)
                DetailItem("Secondary Breed", fowl.breedSecondary ?: "N/A")
                DetailItem("Generation", fowl.generation.toString())
            }
        }

        item {
            DetailCard(title = "Status & Health") {
                DetailItem("Availability", fowl.availabilityStatus)
                DetailItem("Health Status", fowl.healthStatus)
            }
        }

        item {
            DetailCard(title = "Breeding Information") {
                DetailItem("Inbreeding Coefficient", fowl.inbreedingCoefficient?.toString() ?: "N/A")
                DetailItem("Total Offspring", fowl.totalOffspring.toString())
                DetailItem("Siblings", fowl.siblings.joinToString().ifEmpty { "N/A" })
            }
        }

        item {
            DetailCard(title = "Performance") {
                DetailItem("Fighting Wins", fowl.fightingWins.toString())
                DetailItem("Fighting Losses", fowl.fightingLosses.toString())
                DetailItem("Show Wins", fowl.showWins.toString())
            }
        }
        
        item {
            // Quick add actions
            QuickAddActions(fowl, navController)
        }
        
        item {
            // Timeline section
            DetailCard(title = "Timeline") {
                if (recordListItems.isEmpty() && records.isEmpty()) {
                    Text(
                        text = "No records yet. Add your first record to start tracking this fowl's history.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    // Display the records in a list
                    Column {
                        if (recordListItems.isNotEmpty()) {
                            // Use lightweight projection
                            recordListItems.forEach { record ->
                                TimelineListItem(record)
                            }
                        } else {
                            // Use full records
                            records.forEach { record ->
                                TimelineItem(record)
                            }
                        }
                        
                        // Show loading indicator when fetching more records
                        // In a real implementation, we would have a separate state for this
                    }
                }
            }
        }

        item {
            DetailCard(title = "Notes") {
                Text(
                    text = fowl.notes ?: "No notes available.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun QuickAddActions(fowl: FowlEntity, navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Quick add vaccination
                IconButton(
                    onClick = { 
                        // TODO: Navigate to add vaccination screen with prefilled data
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.Vaccines,
                            contentDescription = "Add Vaccination",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Vaccination",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // Quick add weekly update
                IconButton(
                    onClick = { 
                        // TODO: Navigate to add weekly update screen with prefilled data
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.Update,
                            contentDescription = "Add Weekly Update",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Weekly Update",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // General add record
                IconButton(
                    onClick = { 
                        navController.navigate(RIONavigation.Screen.FowlAddRecord.createRoute(fowl.id))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Add Record",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Other",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineListItem(record: com.rio.rostry.core.data.model.FowlRecordListItem) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = record.recordType.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = dateFormat.format(record.recordDate),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        record.description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        if (record.proofCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${record.proofCount} proof document${if (record.proofCount > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Show pending proof indicator if needed
                // In this lightweight version, we don't have proofUrls, so we can't check upload status
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Proofs attached",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Attached",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineItem(record: com.rio.rostry.core.data.model.FowlRecord) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = record.recordType.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = dateFormat.format(record.recordDate),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        record.description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (record.proofCount > 0) {
                Text(
                    text = "${record.proofCount} proof document${if (record.proofCount > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Show pending proof indicator if needed
            if (record.proofCount > 0 && record.proofUrls.isEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = "Pending proof upload",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pending upload",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else if (record.proofCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Proof uploaded",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Uploaded",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun DetailCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(0.6f)
        )
    }
}
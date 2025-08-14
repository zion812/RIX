package com.rio.rostry.ui.fieldtesting

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rio.rostry.core.fieldtesting.FieldTestingManager
import com.rio.rostry.core.fieldtesting.FieldTestingSummary
import kotlinx.coroutines.launch

/**
 * Field Testing Dashboard for real-time monitoring
 * Accessible during field testing deployment
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldTestingDashboard(
    navController: NavController,
    fieldTestingManager: FieldTestingManager
) {
    val scope = rememberCoroutineScope()
    var summary by remember { mutableStateOf<FieldTestingSummary?>(null) }
    var featureStats by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportData by remember { mutableStateOf("") }
    
    // Load field testing data
    LaunchedEffect(Unit) {
        summary = fieldTestingManager.getFieldTestingSummary()
        featureStats = fieldTestingManager.getFeatureUsageStats()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Field Testing Dashboard") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        scope.launch {
                            exportData = fieldTestingManager.exportFieldTestingData()
                            showExportDialog = true
                        }
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Export Data")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                summary?.let { 
                    FieldTestingStatusCard(it)
                }
            }
            
            item {
                FeatureUsageCard(featureStats)
            }
            
            item {
                QuickActionsCard(
                    onRefreshData = {
                        scope.launch {
                            summary = fieldTestingManager.getFieldTestingSummary()
                            featureStats = fieldTestingManager.getFeatureUsageStats()
                        }
                    },
                    onClearData = {
                        scope.launch {
                            fieldTestingManager.clearFieldTestingData()
                            summary = fieldTestingManager.getFieldTestingSummary()
                            featureStats = fieldTestingManager.getFeatureUsageStats()
                        }
                    },
                    onTestAction = {
                        fieldTestingManager.trackUserAction("dashboard_test_action", "manual_test")
                        scope.launch {
                            featureStats = fieldTestingManager.getFeatureUsageStats()
                        }
                    }
                )
            }
            
            item {
                DeploymentInfoCard()
            }
        }
    }
    
    if (showExportDialog) {
        ExportDataDialog(
            exportData = exportData,
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
private fun FieldTestingStatusCard(summary: FieldTestingSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (summary.isActive) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (summary.isActive) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (summary.isActive) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Field Testing Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (summary.isActive) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusMetric(
                    label = "Status",
                    value = if (summary.isActive) "ACTIVE" else "INACTIVE",
                    color = if (summary.isActive) Color.Green else Color.Red
                )
                
                StatusMetric(
                    label = "Session",
                    value = "${summary.sessionDurationMinutes}m",
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                StatusMetric(
                    label = "Actions",
                    value = summary.totalFeatureUsage.toString(),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                StatusMetric(
                    label = "Errors",
                    value = summary.totalErrors.toString(),
                    color = if (summary.totalErrors > 0) Color.Red else Color.Green
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Participant ID: ${summary.participantId}",
                style = MaterialTheme.typography.bodySmall,
                color = if (summary.isActive) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun StatusMetric(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FeatureUsageCard(featureStats: Map<String, Int>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Feature Usage Analytics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (featureStats.isEmpty()) {
                Text(
                    text = "No feature usage data yet. Start using the app to see analytics.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                featureStats.entries.sortedByDescending { it.value }.forEach { (feature, count) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = feature.replace("_", " ").replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = "$count times",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    if (feature != featureStats.keys.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsCard(
    onRefreshData: () -> Unit,
    onClearData: () -> Unit,
    onTestAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRefreshData,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Refresh", fontSize = 12.sp)
                }
                
                OutlinedButton(
                    onClick = onTestAction,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test", fontSize = 12.sp)
                }
                
                OutlinedButton(
                    onClick = onClearData,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun DeploymentInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Deployment Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Phase: Technical Validation",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Target: Rural Karnataka Farmers",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Focus: Core functionality and stability",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Duration: 2 weeks",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ExportDataDialog(
    exportData: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Field Testing Data Export") },
        text = {
            LazyColumn(
                modifier = Modifier.height(400.dp)
            ) {
                item {
                    Text(
                        text = exportData,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

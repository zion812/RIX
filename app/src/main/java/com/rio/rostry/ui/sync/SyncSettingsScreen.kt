package com.rio.rostry.ui.sync

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rostry.core.database.di.DatabaseProvider
import com.rio.rostry.sync.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Sync Settings Screen - Phase 3.1
 * Allows users to configure sync preferences and view sync status
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncSettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Sync manager setup
    val database = remember { DatabaseProvider.getDatabase(context) }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    val syncManager = remember {
        SimpleSyncManager(context, database, firestore, auth)
    }
    
    // State
    val syncStatus by syncManager.syncStatus.collectAsState()
    val lastSyncTime by syncManager.lastSyncTime.collectAsState()
    var syncPreferences by remember { mutableStateOf(SyncPreferences()) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var syncResult by remember { mutableStateOf<SyncResult?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Sync Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
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
                SyncStatusCard(
                    syncStatus = syncStatus,
                    lastSyncTime = lastSyncTime,
                    onManualSync = {
                        showSyncDialog = true
                        scope.launch {
                            val result = syncManager.performManualSync()
                            syncResult = result
                        }
                    }
                )
            }
            
            item {
                SyncPreferencesCard(
                    preferences = syncPreferences,
                    onPreferencesChanged = { syncPreferences = it }
                )
            }
            
            item {
                SyncInfoCard()
            }
            
            item {
                SyncActionsCard(
                    onInitializeSync = {
                        syncManager.initialize()
                    },
                    onCancelSync = {
                        syncManager.cancelSync()
                    }
                )
            }
        }
    }
    
    // Sync Dialog
    if (showSyncDialog) {
        SyncProgressDialog(
            syncStatus = syncStatus,
            syncResult = syncResult,
            onDismiss = { 
                showSyncDialog = false
                syncResult = null
            }
        )
    }
}

@Composable
private fun SyncStatusCard(
    syncStatus: SyncStatus,
    lastSyncTime: Long?,
    onManualSync: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (syncStatus) {
                is SyncStatus.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
                is SyncStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
                SyncStatus.SYNCING -> MaterialTheme.colorScheme.secondaryContainer
                SyncStatus.IDLE -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sync Status",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val statusText = when (syncStatus) {
                        SyncStatus.IDLE -> "Ready to sync"
                        SyncStatus.SYNCING -> "Syncing data..."
                        SyncStatus.SUCCESS -> "Last sync successful"
                        is SyncStatus.ERROR -> "Sync failed: ${syncStatus.message}"
                    }
                    
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    lastSyncTime?.let { timestamp ->
                        val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
                        Text(
                            text = "Last sync: ${formatter.format(Date(timestamp))}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (syncStatus == SyncStatus.SYNCING) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    IconButton(
                        onClick = onManualSync,
                        enabled = syncStatus != SyncStatus.SYNCING
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = "Manual Sync")
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncPreferencesCard(
    preferences: SyncPreferences,
    onPreferencesChanged: (SyncPreferences) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Sync Preferences",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Auto Sync Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto Sync",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Automatically sync data in background",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = preferences.autoSyncEnabled,
                    onCheckedChange = { 
                        onPreferencesChanged(preferences.copy(autoSyncEnabled = it))
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // WiFi Only Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "WiFi Only",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Sync only when connected to WiFi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = preferences.syncOnWifiOnly,
                    onCheckedChange = { 
                        onPreferencesChanged(preferences.copy(syncOnWifiOnly = it))
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Charging Only Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sync When Charging",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Only sync when device is charging",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = preferences.syncOnlyWhenCharging,
                    onCheckedChange = { 
                        onPreferencesChanged(preferences.copy(syncOnlyWhenCharging = it))
                    }
                )
            }
        }
    }
}

@Composable
private fun SyncInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "ℹ️ About Sync",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• Sync keeps your data updated across devices\n" +
                      "• Works offline - changes sync when connected\n" +
                      "• Automatic conflict resolution for data safety\n" +
                      "• Rural-optimized for low bandwidth connections\n" +
                      "• Your data is encrypted and secure",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SyncActionsCard(
    onInitializeSync: () -> Unit,
    onCancelSync: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Sync Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onInitializeSync,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Initialize")
                }
                
                OutlinedButton(
                    onClick = onCancelSync,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun SyncProgressDialog(
    syncStatus: SyncStatus,
    syncResult: SyncResult?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sync Progress") },
        text = {
            Column {
                when (syncStatus) {
                    SyncStatus.SYNCING -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Syncing your data...")
                        }
                    }
                    SyncStatus.SUCCESS -> {
                        syncResult?.let { result ->
                            when (result) {
                                is SyncResult.Success -> {
                                    Text("✅ Sync completed successfully!\n\n" +
                                         "Users synced: ${result.usersSynced}\n" +
                                         "Fowls synced: ${result.fowlsSynced}")
                                }
                                is SyncResult.Error -> {
                                    Text("❌ Sync failed: ${result.message}")
                                }
                            }
                        }
                    }
                    is SyncStatus.ERROR -> {
                        Text("❌ Sync failed: ${syncStatus.message}")
                    }
                    SyncStatus.IDLE -> {
                        Text("Ready to sync")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

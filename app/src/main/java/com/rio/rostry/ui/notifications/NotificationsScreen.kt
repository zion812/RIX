package com.rio.rostry.ui.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rio.rostry.notifications.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Notifications Screen - Phase 3.3
 * Displays notifications and allows configuration of notification settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Notification manager setup
    val notificationManager = remember { SimpleNotificationManager(context) }
    
    // State
    val notifications by notificationManager.notifications.collectAsState()
    val settings by notificationManager.notificationSettings.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
    var fcmToken by remember { mutableStateOf<String?>(null) }
    
    // Initialize FCM
    LaunchedEffect(Unit) {
        fcmToken = notificationManager.initialize()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Notifications",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(
                        onClick = { 
                            notificationManager.clearAllNotifications()
                        }
                    ) {
                        Icon(Icons.Default.ClearAll, contentDescription = "Clear All")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    // Send test notification
                    notificationManager.sendLocalNotification(
                        title = "Test Notification",
                        body = "This is a test notification from RIO app",
                        type = NotificationType.GENERAL
                    )
                },
                icon = { Icon(Icons.Default.NotificationAdd, contentDescription = null) },
                text = { Text("Test") }
            )
        }
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            EmptyNotificationsView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    NotificationStatsCard(
                        totalNotifications = notifications.size,
                        unreadCount = notifications.count { !it.isRead },
                        fcmToken = fcmToken
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                items(notifications) { notification ->
                    NotificationCard(
                        notification = notification,
                        onMarkAsRead = { notificationManager.markAsRead(notification.id) }
                    )
                }
            }
        }
    }
    
    // Settings Dialog
    if (showSettings) {
        NotificationSettingsDialog(
            settings = settings,
            onSettingsChanged = { notificationManager.updateSettings(it) },
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
private fun EmptyNotificationsView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No notifications yet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "You'll see notifications about fowl health, marketplace updates, and sync status here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NotificationStatsCard(
    totalNotifications: Int,
    unreadCount: Int,
    fcmToken: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Notification Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = totalNotifications.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column {
                    Text(
                        text = "Unread",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = unreadCount.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (unreadCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Column {
                    Text(
                        text = "FCM Status",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (fcmToken != null) "Connected" else "Disconnected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (fcmToken != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationItem,
    onMarkAsRead: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            getIconForNotificationType(notification.type),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = getColorForNotificationType(notification.type)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = notification.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                    Text(
                        text = formatter.format(Date(notification.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (!notification.isRead) {
                    IconButton(
                        onClick = onMarkAsRead,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.MarkEmailRead,
                            contentDescription = "Mark as read",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun getIconForNotificationType(type: NotificationType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        NotificationType.FOWL_HEALTH -> Icons.Default.HealthAndSafety
        NotificationType.FOWL_BREEDING -> Icons.Default.Favorite
        NotificationType.MARKETPLACE -> Icons.Default.Store
        NotificationType.SYNC -> Icons.Default.Sync
        else -> Icons.Default.Notifications
    }
}

@Composable
private fun getColorForNotificationType(type: NotificationType): androidx.compose.ui.graphics.Color {
    return when (type) {
        NotificationType.FOWL_HEALTH -> MaterialTheme.colorScheme.error
        NotificationType.FOWL_BREEDING -> MaterialTheme.colorScheme.tertiary
        NotificationType.MARKETPLACE -> MaterialTheme.colorScheme.primary
        NotificationType.SYNC -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
private fun NotificationSettingsDialog(
    settings: NotificationSettings,
    onSettingsChanged: (NotificationSettings) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notification Settings") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    SettingToggle(
                        title = "General Notifications",
                        description = "App updates and general information",
                        checked = settings.generalEnabled,
                        onCheckedChange = {
                            onSettingsChanged(settings.copy(generalEnabled = it))
                        }
                    )
                }

                item {
                    SettingToggle(
                        title = "Fowl Health Alerts",
                        description = "Health monitoring and medical reminders",
                        checked = settings.fowlHealthEnabled,
                        onCheckedChange = {
                            onSettingsChanged(settings.copy(fowlHealthEnabled = it))
                        }
                    )
                }

                item {
                    SettingToggle(
                        title = "Breeding Updates",
                        description = "Breeding schedules and genetic insights",
                        checked = settings.fowlBreedingEnabled,
                        onCheckedChange = {
                            onSettingsChanged(settings.copy(fowlBreedingEnabled = it))
                        }
                    )
                }

                item {
                    SettingToggle(
                        title = "Marketplace Notifications",
                        description = "New listings and transaction updates",
                        checked = settings.marketplaceEnabled,
                        onCheckedChange = {
                            onSettingsChanged(settings.copy(marketplaceEnabled = it))
                        }
                    )
                }

                item {
                    SettingToggle(
                        title = "Sync Status",
                        description = "Data synchronization updates",
                        checked = settings.syncEnabled,
                        onCheckedChange = {
                            onSettingsChanged(settings.copy(syncEnabled = it))
                        }
                    )
                }

                item {
                    HorizontalDivider()
                }

                item {
                    SettingToggle(
                        title = "Sound",
                        description = "Play notification sounds",
                        checked = settings.soundEnabled,
                        onCheckedChange = {
                            onSettingsChanged(settings.copy(soundEnabled = it))
                        }
                    )
                }

                item {
                    SettingToggle(
                        title = "Vibration",
                        description = "Vibrate for notifications",
                        checked = settings.vibrationEnabled,
                        onCheckedChange = {
                            onSettingsChanged(settings.copy(vibrationEnabled = it))
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun SettingToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

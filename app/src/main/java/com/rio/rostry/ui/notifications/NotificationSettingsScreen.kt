package com.rio.rostry.ui.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rio.rostry.core.database.entities.NotificationPreferenceEntity

/**
 * Notification settings screen for managing user preferences
 * Supports granular control over notification categories and behavior
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBackClick: () -> Unit,
    viewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top app bar
        TopAppBar(
            title = { Text("Notification Settings") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        preferences?.let { prefs ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Notification Categories Section
                item {
                    NotificationCategorySection(
                        preferences = prefs,
                        onPreferenceChange = { updatedPrefs ->
                            viewModel.updatePreferences(updatedPrefs)
                        }
                    )
                }

                // Sound & Vibration Section
                item {
                    SoundVibrationSection(
                        preferences = prefs,
                        onPreferenceChange = { updatedPrefs ->
                            viewModel.updatePreferences(updatedPrefs)
                        }
                    )
                }

                // Quiet Hours Section
                item {
                    QuietHoursSection(
                        preferences = prefs,
                        onPreferenceChange = { updatedPrefs ->
                            viewModel.updatePreferences(updatedPrefs)
                        }
                    )
                }

                // Advanced Settings Section
                item {
                    AdvancedSettingsSection(
                        onTestNotification = {
                            viewModel.sendTestNotification()
                        },
                        onClearHistory = {
                            viewModel.clearNotificationHistory()
                        }
                    )
                }
            }
        } ?: run {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun NotificationCategorySection(
    preferences: NotificationPreferenceEntity,
    onPreferenceChange: (NotificationPreferenceEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Notification Categories",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Marketplace notifications
            NotificationToggleItem(
                title = "Marketplace",
                subtitle = "New listings, price alerts, and purchase updates",
                icon = Icons.Default.Store,
                checked = preferences.marketplaceNotifications,
                onCheckedChange = { enabled ->
                    onPreferenceChange(preferences.copy(marketplaceNotifications = enabled))
                }
            )

            // Transfer notifications
            NotificationToggleItem(
                title = "Transfers & Verification",
                subtitle = "Rooster transfers and verification status updates",
                icon = Icons.Default.SwapHoriz,
                checked = preferences.transferNotifications,
                onCheckedChange = { enabled ->
                    onPreferenceChange(preferences.copy(transferNotifications = enabled))
                }
            )

            // Communication notifications
            NotificationToggleItem(
                title = "Messages & Chat",
                subtitle = "New messages and community updates",
                icon = Icons.Default.Chat,
                checked = preferences.communicationNotifications,
                onCheckedChange = { enabled ->
                    onPreferenceChange(preferences.copy(communicationNotifications = enabled))
                }
            )

            // Breeding notifications
            NotificationToggleItem(
                title = "Breeding & Health",
                subtitle = "Breeding reminders and health alerts",
                icon = Icons.Default.Pets,
                checked = preferences.breedingNotifications,
                onCheckedChange = { enabled ->
                    onPreferenceChange(preferences.copy(breedingNotifications = enabled))
                }
            )

            // Payment notifications
            NotificationToggleItem(
                title = "Payments & Coins",
                subtitle = "Payment confirmations and coin transactions",
                icon = Icons.Default.Payment,
                checked = preferences.paymentNotifications,
                onCheckedChange = { enabled ->
                    onPreferenceChange(preferences.copy(paymentNotifications = enabled))
                }
            )

            // System notifications
            NotificationToggleItem(
                title = "System Updates",
                subtitle = "App updates and important announcements",
                icon = Icons.Default.Settings,
                checked = preferences.systemNotifications,
                onCheckedChange = { enabled ->
                    onPreferenceChange(preferences.copy(systemNotifications = enabled))
                }
            )
        }
    }
}

@Composable
fun SoundVibrationSection(
    preferences: NotificationPreferenceEntity,
    onPreferenceChange: (NotificationPreferenceEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Sound & Vibration",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Sound toggle
            NotificationToggleItem(
                title = "Sound",
                subtitle = "Play notification sounds",
                icon = Icons.Default.VolumeUp,
                checked = preferences.soundEnabled,
                onCheckedChange = { enabled ->
                    onPreferenceChange(preferences.copy(soundEnabled = enabled))
                }
            )

            // Vibration toggle
            NotificationToggleItem(
                title = "Vibration",
                subtitle = "Vibrate for notifications",
                icon = Icons.Default.Vibration,
                checked = preferences.vibrationEnabled,
                onCheckedChange = { enabled ->
                    onPreferenceChange(preferences.copy(vibrationEnabled = enabled))
                }
            )
        }
    }
}

@Composable
fun QuietHoursSection(
    preferences: NotificationPreferenceEntity,
    onPreferenceChange: (NotificationPreferenceEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Quiet Hours",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Quiet hours toggle
            NotificationToggleItem(
                title = "Enable Quiet Hours",
                subtitle = "Silence notifications during specified hours",
                icon = Icons.Default.DoNotDisturb,
                checked = preferences.quietHoursEnabled,
                onCheckedChange = { enabled ->
                    onPreferenceChange(preferences.copy(quietHoursEnabled = enabled))
                }
            )

            if (preferences.quietHoursEnabled) {
                Spacer(modifier = Modifier.height(16.dp))

                // Time range selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "From",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = preferences.quietHoursStart,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )

                    Column {
                        Text(
                            text = "To",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = preferences.quietHoursEnd,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        // TODO: Open time picker dialog
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change Quiet Hours")
                }
            }
        }
    }
}

@Composable
fun AdvancedSettingsSection(
    onTestNotification: () -> Unit,
    onClearHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Advanced",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Test notification button
            OutlinedButton(
                onClick = onTestNotification,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationImportant,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send Test Notification")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Clear history button
            OutlinedButton(
                onClick = onClearHistory,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ClearAll,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear Notification History")
            }
        }
    }
}

@Composable
fun NotificationToggleItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = checked,
                onClick = { onCheckedChange(!checked) },
                role = Role.Switch
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
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

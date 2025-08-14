package com.rio.rostry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rio.rostry.auth.FirebaseAuthManager
import com.rio.rostry.core.database.di.DatabaseProvider
import com.rio.rostry.ui.auth.AuthScreen
import com.rio.rostry.ui.test.FirebaseTestScreen
import com.rio.rostry.ui.test.DatabaseTestScreen
import com.rio.rostry.ui.test.RepositoryTestScreen
import com.rio.rostry.ui.fowl.SimpleFowlManagementScreen
import com.rio.rostry.ui.auth.AuthenticationScreen
import com.rio.rostry.ui.profile.UserProfileScreen
import com.rio.rostry.auth.FirebaseAuthService
import com.rio.rostry.ui.theme.ROSTRYTheme
import com.rio.rostry.ui.MainActivityViewModel
import com.rio.rostry.navigation.ROSTRYNavigation
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.rio.rostry.core.common.compliance.ConsentManager
import com.rio.rostry.ui.compliance.ConsentDialog

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ROSTRYTheme {
                ROSTRYApp()
                // Stop startup trace once first frame is composed
                LaunchedEffect(Unit) {
                    try {
                        FirebasePerformance.getInstance()
                            .newTrace("app_startup_first_frame").apply {
                                start(); stop()
                            }
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }
}

@Composable
fun ROSTRYApp(
    viewModel: MainActivityViewModel = hiltViewModel()
) {
    // Use the new Navigation Compose system
    ROSTRYNavigation(authManager = viewModel.authManager)

    // Minimal consent flow
    val context = androidx.compose.ui.platform.LocalContext.current
    val consentManager = remember { ConsentManager(context) }
    var showConsent by remember { mutableStateOf(!consentManager.isConsentAccepted()) }

    if (showConsent) {
        ConsentDialog(
            onAccept = { retentionAccepted ->
                consentManager.setConsentAccepted(true)
                consentManager.setDataRetentionAccepted(retentionAccepted)
                showConsent = false
            },
            onDecline = {
                // Keep dialog open or handle restricted mode
            }
        )
    }
}

@Composable
fun Phase5MainContent(
    modifier: Modifier = Modifier,
    onShowFirebaseTest: () -> Unit = {},
    onShowDatabaseTest: () -> Unit = {},
    onShowRepositoryTest: () -> Unit = {},
    onShowFowlManagement: () -> Unit = {},
    onShowAuthentication: () -> Unit = {},
    onShowUserProfile: () -> Unit = {},
    currentUser: com.google.firebase.auth.FirebaseUser? = null,
    onLogout: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Rocket,
            contentDescription = "ROSTRY Logo",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ROSTRY Platform",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Phase 6 - Production Ready",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Authentication Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (currentUser != null)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = if (currentUser != null) "✅ Authenticated" else "❌ Not Authenticated",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (currentUser != null) {
                    Text(
                        text = "User: ${currentUser.displayName ?: currentUser.email}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    TextButton(
                        onClick = onLogout,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text("Logout", fontSize = 12.sp)
                    }
                } else {
                    Text(
                        text = "Login to access full features",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Welcome to ROSTRY!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "This is Phase 6 of the ROSTRY platform - Production Ready! " +
                            "Complete integration with Firebase Auth, real-time sync, and full repository layer.",
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "✅ Firebase Integration\n" +
                            "✅ Basic UI Framework\n" +
                            "✅ Authentication System\n" +
                            "✅ Database Layer (Phase 2)\n" +
                            "✅ Repository Layer (Phase 3)\n" +
                            "✅ Feature Modules (Phase 4)\n" +
                            "✅ Full Integration (Phase 5)\n" +
                            "✅ Production Ready (Phase 6)\n" +
                            "🚀 Ready for Rural Farmers!",
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Test buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onShowFirebaseTest,
                modifier = Modifier.weight(1f)
            ) {
                Text("Firebase")
            }

            Button(
                onClick = onShowDatabaseTest,
                modifier = Modifier.weight(1f)
            ) {
                Text("Database")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onShowRepositoryTest,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Test Repository Layer")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onShowFowlManagement,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("Fowl Management")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onShowAuthentication,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Text("Authentication")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onShowUserProfile,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text("User Profile")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Use the buttons to test connectivity, manage fowls, authenticate, and view profile",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ROSTRYAppPreview() {
    ROSTRYTheme {
        ROSTRYApp()
    }
}
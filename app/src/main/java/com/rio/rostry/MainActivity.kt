package com.rio.rostry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import com.rio.rostry.auth.FirebaseAuthManager
import com.rio.rostry.ui.auth.AuthScreen
import com.rio.rostry.ui.test.FirebaseTestScreen
import com.rio.rostry.ui.theme.ROSTRYTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ROSTRYTheme {
                RIOApp()
            }
        }
    }
}

@Composable
fun RIOApp() {
    val authManager = FirebaseAuthManager.getInstance()
    val currentUser by authManager.currentUser.collectAsState()
    val userClaims by authManager.userClaims.collectAsState()
    var showTestScreen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showTestScreen = !showTestScreen }
            ) {
                Text(if (showTestScreen) "App" else "Test")
            }
        }
    ) { innerPadding ->
        if (showTestScreen) {
            // Show Firebase test screen
            FirebaseTestScreen()
        } else if (currentUser == null) {
            // Show authentication screen
            AuthScreen(
                authManager = authManager,
                onAuthSuccess = {
                    // Navigation will be handled by the AuthScreen's LaunchedEffect
                }
            )
        } else {
            // Show main app content based on user tier
            MainContent(
                userClaims = userClaims,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun MainContent(
    userClaims: com.rio.rostry.auth.UserClaims?,
    modifier: Modifier = Modifier
) {
    userClaims?.let { claims ->
        when (claims.tier) {
            com.rio.rostry.auth.UserTier.GENERAL -> {
                GeneralUserDashboard(claims = claims, modifier = modifier)
            }
            com.rio.rostry.auth.UserTier.FARMER -> {
                FarmerDashboard(claims = claims, modifier = modifier)
            }
            com.rio.rostry.auth.UserTier.ENTHUSIAST -> {
                EnthusiastDashboard(claims = claims, modifier = modifier)
            }
        }
    } ?: run {
        Text(
            text = "Loading user data...",
            modifier = modifier
        )
    }
}

@Composable
fun GeneralUserDashboard(
    claims: com.rio.rostry.auth.UserClaims,
    modifier: Modifier = Modifier
) {
    Text(
        text = "Welcome to RIO Marketplace!\n\n" +
                "As a General User, you can:\n" +
                "• Browse fowl listings\n" +
                "• View marketplace\n" +
                "• Manage your basic profile\n\n" +
                "Upgrade to Farmer tier to create listings and manage breeding records!",
        modifier = modifier
    )
}

@Composable
fun FarmerDashboard(
    claims: com.rio.rostry.auth.UserClaims,
    modifier: Modifier = Modifier
) {
    Text(
        text = "Welcome to RIO Farmer Dashboard!\n\n" +
                "As a Farmer, you can:\n" +
                "• Create and manage fowl listings\n" +
                "• Access breeding records\n" +
                "• View analytics\n" +
                "• Sell in marketplace\n\n" +
                "Upgrade to Enthusiast tier for premium features!",
        modifier = modifier
    )
}

@Composable
fun EnthusiastDashboard(
    claims: com.rio.rostry.auth.UserClaims,
    modifier: Modifier = Modifier
) {
    Text(
        text = "Welcome to RIO Enthusiast Dashboard!\n\n" +
                "As a High-Level Enthusiast, you have access to:\n" +
                "• All Farmer features\n" +
                "• Advanced analytics dashboard\n" +
                "• Premium features\n" +
                "• Verified transfer capabilities\n" +
                "• Priority support\n\n" +
                "You have the highest tier access!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun RIOAppPreview() {
    ROSTRYTheme {
        RIOApp()
    }
}
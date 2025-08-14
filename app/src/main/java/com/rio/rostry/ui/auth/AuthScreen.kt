package com.rio.rostry.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rio.rostry.auth.FirebaseAuthManager
import com.rio.rostry.auth.UserTier
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onAuthSuccess: () -> Unit = {}
) {
    val authManager = viewModel.authManager
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val isLoading by authManager.isLoading.collectAsState()
    val currentUser by authManager.currentUser.collectAsState()
    val userClaims by authManager.userClaims.collectAsState()

    // Navigate to main screen if user is authenticated
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onAuthSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "RIO Rooster Community",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    errorMessage = null
                    val result = if (isSignUp) {
                        authManager.createUserWithEmailAndPassword(email, password)
                    } else {
                        authManager.signInWithEmailAndPassword(email, password)
                    }

                    result.onFailure { exception ->
                        errorMessage = exception.message
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(if (isSignUp) "Sign Up" else "Sign In")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { isSignUp = !isSignUp },
            enabled = !isLoading
        ) {
            Text(
                if (isSignUp) "Already have an account? Sign In"
                else "Don't have an account? Sign Up"
            )
        }

        errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Show user info if authenticated
        currentUser?.let { user ->
            Spacer(modifier = Modifier.height(24.dp))
            UserInfoCard(
                user = user,
                userClaims = userClaims,
                authManager = authManager
            )
        }
    }
}

@Composable
fun UserInfoCard(
    user: com.google.firebase.auth.FirebaseUser,
    userClaims: com.rio.rostry.auth.UserClaims?,
    authManager: FirebaseAuthManager
) {
    val scope = rememberCoroutineScope()
    var showUpgradeDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Welcome, ${user.email}!",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            userClaims?.let { claims ->
                Text("Tier: ${claims.tier.displayName}")
                Text("Email Verified: ${claims.verificationStatus.emailVerified}")

                Spacer(modifier = Modifier.height(16.dp))

                Text("Permissions:", style = MaterialTheme.typography.titleSmall)
                Text("• Create Listings: ${claims.permissions.canCreateListings}")
                Text("• Access Analytics: ${claims.permissions.canAccessAnalytics}")
                Text("• Premium Features: ${claims.permissions.canAccessPremiumFeatures}")

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    if (claims.tier != UserTier.ENTHUSIAST) {
                        Button(
                            onClick = { showUpgradeDialog = true }
                        ) {
                            Text("Request Upgrade")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    OutlinedButton(
                        onClick = { authManager.signOut() }
                    ) {
                        Text("Sign Out")
                    }
                }
            }
        }
    }

    if (showUpgradeDialog) {
        TierUpgradeDialog(
            currentTier = userClaims?.tier ?: UserTier.GENERAL,
            onDismiss = { showUpgradeDialog = false },
            onUpgradeRequest = { targetTier ->
                scope.launch {
                    authManager.requestTierUpgrade(targetTier)
                    showUpgradeDialog = false
                }
            }
        )
    }
}

@Composable
fun TierUpgradeDialog(
    currentTier: UserTier,
    onDismiss: () -> Unit,
    onUpgradeRequest: (UserTier) -> Unit
) {
    val availableTiers = when (currentTier) {
        UserTier.GENERAL -> listOf(UserTier.FARMER, UserTier.ENTHUSIAST)
        UserTier.FARMER -> listOf(UserTier.ENTHUSIAST)
        UserTier.ENTHUSIAST -> emptyList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Tier Upgrade") },
        text = {
            Column {
                Text("Current tier: ${currentTier.displayName}")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Select target tier:")

                availableTiers.forEach { tier ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onUpgradeRequest(tier) }
                        ) {
                            Text(tier.displayName)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
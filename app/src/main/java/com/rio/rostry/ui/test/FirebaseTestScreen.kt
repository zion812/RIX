package com.rio.rostry.ui.test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseTestScreen() {
    var testResults by remember { mutableStateOf<List<TestResult>>(emptyList()) }
    var isRunning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Firebase Integration Test",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = {
                scope.launch {
                    isRunning = true
                    testResults = runFirebaseTests()
                    isRunning = false
                }
            },
            enabled = !isRunning,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isRunning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Run Firebase Tests")
        }

        Spacer(modifier = Modifier.height(24.dp))

        testResults.forEach { result ->
            TestResultCard(result = result)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun TestResultCard(result: TestResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.success)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.testName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (result.success) "✅ PASS" else "❌ FAIL",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            if (result.details.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = result.details,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (result.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Error: ${result.error}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

data class TestResult(
    val testName: String,
    val success: Boolean,
    val details: String = "",
    val error: String? = null
)

suspend fun runFirebaseTests(): List<TestResult> {
    val results = mutableListOf<TestResult>()

    // Test 1: Firebase App Initialization
    try {
        val app = FirebaseApp.getInstance()
        val projectId = app.options.projectId
        val appId = app.options.applicationId

        results.add(
            TestResult(
                testName = "Firebase App Initialization",
                success = true,
                details = "Project ID: $projectId\nApp ID: $appId"
            )
        )
    } catch (e: Exception) {
        results.add(
            TestResult(
                testName = "Firebase App Initialization",
                success = false,
                error = e.message
            )
        )
    }

    // Test 2: Firebase Auth Connection
    try {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        results.add(
            TestResult(
                testName = "Firebase Auth Connection",
                success = true,
                details = if (currentUser != null)
                    "User signed in: ${currentUser.email}"
                else
                    "No user currently signed in"
            )
        )
    } catch (e: Exception) {
        results.add(
            TestResult(
                testName = "Firebase Auth Connection",
                success = false,
                error = e.message
            )
        )
    }

    // Test 3: Firestore Connection
    try {
        val firestore = FirebaseFirestore.getInstance()

        // Try to read from a test collection
        val testDoc = firestore.collection("test").document("connection").get().await()

        results.add(
            TestResult(
                testName = "Firestore Connection",
                success = true,
                details = "Successfully connected to Firestore\nTest document exists: ${testDoc.exists()}"
            )
        )
    } catch (e: Exception) {
        results.add(
            TestResult(
                testName = "Firestore Connection",
                success = false,
                error = e.message
            )
        )
    }

    // Test 4: Custom Claims Structure
    try {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user != null) {
            val tokenResult = user.getIdToken(false).await()
            val claims = tokenResult.claims

            results.add(
                TestResult(
                    testName = "Custom Claims",
                    success = true,
                    details = "Claims available: ${claims.keys.joinToString(", ")}"
                )
            )
        } else {
            results.add(
                TestResult(
                    testName = "Custom Claims",
                    success = true,
                    details = "No user signed in - cannot test claims"
                )
            )
        }
    } catch (e: Exception) {
        results.add(
            TestResult(
                testName = "Custom Claims",
                success = false,
                error = e.message
            )
        )
    }

    return results
}
package com.rio.rostry.ui.test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rio.rostry.core.database.di.DatabaseProvider
import com.rio.rostry.core.database.entities.UserEntity
import com.rio.rostry.core.database.entities.FowlEntity
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseTestScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { DatabaseProvider.getDatabase(context) }
    
    var users by remember { mutableStateOf<List<UserEntity>>(emptyList()) }
    var fowls by remember { mutableStateOf<List<FowlEntity>>(emptyList()) }
    var testResult by remember { mutableStateOf("Ready to test database...") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Database Test - Phase 2") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            // Test database operations
                            testResult = "Testing database operations..."
                            
                            // Create test user
                            val testUser = UserEntity(
                                id = "test_user_${System.currentTimeMillis()}",
                                email = "test@rio.com",
                                displayName = "Test User",
                                phoneNumber = "+1234567890",
                                tier = "general",
                                region = "test_region",
                                district = "test_district",
                                createdAt = Date()
                            )
                            
                            database.userDao().insertUser(testUser)
                            testResult = "✅ User inserted successfully!"
                            
                            // Create test fowl
                            val testFowl = FowlEntity(
                                id = "test_fowl_${System.currentTimeMillis()}",
                                ownerId = testUser.id,
                                name = "Test Rooster",
                                breed = "Rhode Island Red",
                                gender = "MALE",
                                birthDate = Date(),
                                color = "Red",
                                weight = 3.5,
                                status = "ACTIVE",
                                description = "A beautiful test rooster",
                                isForSale = true,
                                price = 150.0,
                                region = "test_region",
                                district = "test_district",
                                createdAt = Date()
                            )
                            
                            database.fowlDao().insertFowl(testFowl)
                            testResult = "✅ User and Fowl inserted successfully!"
                            
                            // Fetch all users and fowls
                            users = database.userDao().getUsersByLocation("test_region", "test_district")
                            fowls = database.fowlDao().getFowlsByLocation("test_region", "test_district")
                            
                            testResult = "✅ Database test completed! Found ${users.size} users and ${fowls.size} fowls"
                            
                        } catch (e: Exception) {
                            testResult = "❌ Database test failed: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Test Database")
            }
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
                                Icons.Default.Storage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Database Status",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Testing database...")
                            }
                        } else {
                            Text(testResult)
                        }
                    }
                }
            }
            
            if (users.isNotEmpty()) {
                item {
                    Text(
                        text = "Users (${users.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(users) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = user.displayName,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Email: ${user.email}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Tier: ${user.tier} | Region: ${user.region}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            if (fowls.isNotEmpty()) {
                item {
                    Text(
                        text = "Fowls (${fowls.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(fowls) { fowl ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = fowl.name,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Breed: ${fowl.breed} | Gender: ${fowl.gender}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (fowl.isForSale && fowl.price != null) {
                                Text(
                                    text = "For Sale: ₹${fowl.price}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

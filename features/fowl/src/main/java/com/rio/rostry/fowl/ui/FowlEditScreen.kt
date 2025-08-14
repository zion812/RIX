package com.rio.rostry.fowl.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rio.rostry.fowl.ui.viewmodels.SimpleFowlViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FowlEditScreen(
    fowlId: String,
    navController: NavController,
    viewModel: SimpleFowlViewModel = hiltViewModel()
) {
    val fowl by viewModel.selectedFowl.collectAsState()

    // Fetch the fowl details when the screen is first composed
    LaunchedEffect(fowlId) {
        viewModel.getFowlById(fowlId)
    }

    var name by remember(fowl) { mutableStateOf(fowl?.name ?: "") }
    var breed by remember(fowl) { mutableStateOf(fowl?.breedPrimary ?: "") }
    var notes by remember(fowl) { mutableStateOf(fowl?.notes ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit ${fowl?.name ?: "Fowl"}") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    fowl?.let {
                        val updatedFowl = it.copy(
                            name = name,
                            breedPrimary = breed,
                            notes = notes,
                            updatedAt = java.util.Date()
                        )
                        viewModel.updateFowl(updatedFowl)
                        navController.navigateUp()
                    }
                }
            ) {
                Icon(Icons.Default.Done, contentDescription = "Save Changes")
            }
        }
    ) { paddingValues ->
        if (fowl != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Edit Fowl Details", style = MaterialTheme.typography.headlineMedium)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Primary Breed") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5
                )
            }
        } else {
            // Show a loading or error state
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

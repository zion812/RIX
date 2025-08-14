package com.rio.rostry.fowl.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rio.rostry.core.database.entities.FowlEntity
import com.rio.rostry.fowl.ui.viewmodels.SimpleFowlViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FowlDetailScreen(
    fowlId: String,
    navController: NavController,
    viewModel: SimpleFowlViewModel = hiltViewModel()
) {
    val fowl by viewModel.selectedFowl.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(fowlId) {
        viewModel.getFowlById(fowlId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(fowl?.name ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                error != null -> {
                    Text(
                        text = "Error: $error",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                fowl != null -> {
                    FowlDetailContent(fowl!!)
                }
            }
        }
    }
}

@Composable
fun FowlDetailContent(fowl: FowlEntity) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DetailCard(title = "Primary Information") {
            DetailItem("Name", fowl.name ?: "N/A")
            DetailItem("Primary Breed", fowl.breedPrimary)
            DetailItem("Secondary Breed", fowl.breedSecondary ?: "N/A")
            DetailItem("Generation", fowl.generation.toString())
        }

        DetailCard(title = "Status & Health") {
            DetailItem("Availability", fowl.availabilityStatus)
            DetailItem("Health Status", fowl.healthStatus)
        }

        DetailCard(title = "Breeding Information") {
            DetailItem("Inbreeding Coefficient", fowl.inbreedingCoefficient?.toString() ?: "N/A")
            DetailItem("Total Offspring", fowl.totalOffspring.toString())
            DetailItem("Siblings", fowl.siblings.joinToString().ifEmpty { "N/A" })
        }

        DetailCard(title = "Performance") {
            DetailItem("Fighting Wins", fowl.fightingWins.toString())
            DetailItem("Fighting Losses", fowl.fightingLosses.toString())
            DetailItem("Show Wins", fowl.showWins.toString())
        }

        DetailCard(title = "Notes") {
            Text(
                text = fowl.notes ?: "No notes available.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun DetailCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(0.6f)
        )
    }
}

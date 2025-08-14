package com.rio.rostry.marketplace.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rio.rostry.marketplace.domain.model.ListingCreateRequest
import com.rio.rostry.marketplace.domain.model.ListingDetails
import com.rio.rostry.marketplace.domain.model.ListingPricing
import com.rio.rostry.marketplace.domain.model.ListingType
import com.rio.rostry.marketplace.ui.viewmodels.MarketplaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(
    navController: NavController,
    viewModel: MarketplaceViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    val creationState by viewModel.listingCreationState.collectAsState()

    LaunchedEffect(creationState.isSuccess) {
        if (creationState.isSuccess) {
            // Navigate back after successful creation
            navController.popBackStack()
            viewModel.clearListingCreationState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Listing") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Listing Title") },
                modifier = Modifier.fillMaxWidth(),
                isError = creationState.error?.contains("Title") == true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5,
                isError = creationState.error?.contains("Description") == true
            )

            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it },
                label = { Text("Breed") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price in Coins") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = creationState.error?.contains("Price") == true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val priceInCoins = price.toIntOrNull() ?: 0
                    val request = ListingCreateRequest(
                        details = ListingDetails(
                            title = title,
                            description = description,
                            breed = breed,
                            category = "Fowl" // Default category for now
                        ),
                        pricing = ListingPricing(
                            basePrice = priceInCoins
                        ),
                        listingType = ListingType.FIXED_PRICE, // Default type
                        autoPublish = true
                    )
                    viewModel.createListing(request)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !creationState.isLoading
            ) {
                if (creationState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Create Listing")
                }
            }

            creationState.error?.let {
                Text(
                    text = "Error: $it",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

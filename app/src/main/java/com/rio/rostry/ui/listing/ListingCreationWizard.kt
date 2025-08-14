package com.rio.rostry.ui.listing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rio.rostry.ui.listing.models.*
import kotlinx.coroutines.launch

/**
 * Comprehensive Listing Creation Wizard for Field Testing
 * Supports KYC verification, media upload, and traceability metadata
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingCreationWizard(
    navController: NavController,
    onListingCreated: (ListingCreationData) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val scope = rememberCoroutineScope()
    
    var listingData by remember { mutableStateOf(ListingCreationData()) }
    var currentStepValid by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Listing") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            WizardBottomBar(
                currentStep = pagerState.currentPage,
                totalSteps = 5,
                isStepValid = currentStepValid,
                onPrevious = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                onNext = {
                    scope.launch {
                        if (pagerState.currentPage < 4) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        } else {
                            onListingCreated(listingData)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = { (pagerState.currentPage + 1) / 5f },
                modifier = Modifier.fillMaxWidth()
            )
            
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> BasicInfoStep(
                        data = listingData,
                        onDataChange = { listingData = it },
                        onValidationChange = { currentStepValid = it }
                    )
                    1 -> MediaUploadStep(
                        data = listingData,
                        onDataChange = { listingData = it },
                        onValidationChange = { currentStepValid = it }
                    )
                    2 -> TraceabilityStep(
                        data = listingData,
                        onDataChange = { listingData = it },
                        onValidationChange = { currentStepValid = it }
                    )
                    3 -> PricingAndAvailabilityStep(
                        data = listingData,
                        onDataChange = { listingData = it },
                        onValidationChange = { currentStepValid = it }
                    )
                    4 -> ReviewAndSubmitStep(
                        data = listingData,
                        onDataChange = { listingData = it },
                        onValidationChange = { currentStepValid = it }
                    )
                }
            }
        }
    }
}

/**
 * Wizard bottom navigation bar
 */
@Composable
private fun WizardBottomBar(
    currentStep: Int,
    totalSteps: Int,
    isStepValid: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 0) {
                OutlinedButton(onClick = onPrevious) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Previous")
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
            
            // Step indicator
            Text(
                text = "Step ${currentStep + 1} of $totalSteps",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Button(
                onClick = onNext,
                enabled = isStepValid
            ) {
                Text(if (currentStep == totalSteps - 1) "Create Listing" else "Next")
                if (currentStep < totalSteps - 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

/**
 * Step 1: Basic Information
 */
@Composable
private fun BasicInfoStep(
    data: ListingCreationData,
    onDataChange: (ListingCreationData) -> Unit,
    onValidationChange: (Boolean) -> Unit
) {
    LaunchedEffect(data.basicInfo) {
        val isValid = data.basicInfo.name.isNotBlank() && 
                     data.basicInfo.breed.isNotBlank() &&
                     data.basicInfo.gender.isNotBlank()
        onValidationChange(isValid)
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Basic Information",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tell us about your rooster - this information helps buyers find exactly what they're looking for",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tip: Accurate information builds trust with buyers and leads to better sales",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        item {
            OutlinedTextField(
                value = data.basicInfo.name,
                onValueChange = { 
                    onDataChange(data.copy(
                        basicInfo = data.basicInfo.copy(name = it)
                    ))
                },
                label = { Text("Rooster Name *") },
                placeholder = { Text("e.g., Champion, Raja, etc.") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Pets, contentDescription = null) }
            )
        }
        
        item {
            BreedSelectionField(
                selectedBreed = data.basicInfo.breed,
                onBreedSelected = { breed ->
                    onDataChange(data.copy(
                        basicInfo = data.basicInfo.copy(breed = breed)
                    ))
                }
            )
        }
        
        item {
            GenderSelectionField(
                selectedGender = data.basicInfo.gender,
                onGenderSelected = { gender ->
                    onDataChange(data.copy(
                        basicInfo = data.basicInfo.copy(gender = gender)
                    ))
                }
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = data.basicInfo.age,
                    onValueChange = { 
                        onDataChange(data.copy(
                            basicInfo = data.basicInfo.copy(age = it)
                        ))
                    },
                    label = { Text("Age (months)") },
                    placeholder = { Text("e.g., 12") },
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = data.basicInfo.weight,
                    onValueChange = { 
                        onDataChange(data.copy(
                            basicInfo = data.basicInfo.copy(weight = it)
                        ))
                    },
                    label = { Text("Weight (kg)") },
                    placeholder = { Text("e.g., 3.5") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            OutlinedTextField(
                value = data.basicInfo.description,
                onValueChange = { 
                    onDataChange(data.copy(
                        basicInfo = data.basicInfo.copy(description = it)
                    ))
                },
                label = { Text("Description") },
                placeholder = { Text("Describe your rooster's characteristics, temperament, etc.") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
    }
}

// Placeholder composables for the wizard components
// These will be implemented in separate files

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BreedSelectionField(
    selectedBreed: String,
    onBreedSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedBreed,
            onValueChange = { },
            readOnly = true,
            label = { Text("Breed *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            leadingIcon = { Icon(Icons.Default.Pets, contentDescription = null) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            BreedOptions.COMMON_BREEDS.forEach { breed ->
                DropdownMenuItem(
                    text = { Text(breed) },
                    onClick = {
                        onBreedSelected(breed)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenderSelectionField(
    selectedGender: String,
    onGenderSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedGender,
            onValueChange = { },
            readOnly = true,
            label = { Text("Gender *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            leadingIcon = { Icon(Icons.Default.Wc, contentDescription = null) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            BreedOptions.GENDER_OPTIONS.forEach { gender ->
                DropdownMenuItem(
                    text = { Text(gender) },
                    onClick = {
                        onGenderSelected(gender)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Placeholder implementations for other steps
@Composable
private fun MediaUploadStep(
    data: ListingCreationData,
    onDataChange: (ListingCreationData) -> Unit,
    onValidationChange: (Boolean) -> Unit
) {
    LaunchedEffect(Unit) {
        onValidationChange(true) // For now, make this step always valid
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.PhotoCamera,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Media Upload",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Photo and video upload functionality will be implemented here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TraceabilityStep(
    data: ListingCreationData,
    onDataChange: (ListingCreationData) -> Unit,
    onValidationChange: (Boolean) -> Unit
) {
    LaunchedEffect(Unit) {
        onValidationChange(true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.AccountTree,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Traceability & Health Records",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Lineage and health record functionality will be implemented here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PricingAndAvailabilityStep(
    data: ListingCreationData,
    onDataChange: (ListingCreationData) -> Unit,
    onValidationChange: (Boolean) -> Unit
) {
    LaunchedEffect(data.pricingInfo) {
        val isValid = data.pricingInfo.basePrice.isNotBlank() &&
                     data.pricingInfo.basePrice.toDoubleOrNull() != null &&
                     data.pricingInfo.basePrice.toDouble() > 0
        onValidationChange(isValid)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Pricing & Availability",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            OutlinedTextField(
                value = data.pricingInfo.basePrice,
                onValueChange = {
                    onDataChange(data.copy(
                        pricingInfo = data.pricingInfo.copy(basePrice = it)
                    ))
                },
                label = { Text("Price (₹) *") },
                placeholder = { Text("e.g., 5000") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) }
            )
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = data.pricingInfo.isNegotiable,
                    onCheckedChange = {
                        onDataChange(data.copy(
                            pricingInfo = data.pricingInfo.copy(isNegotiable = it)
                        ))
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Price is negotiable")
            }
        }
    }
}

@Composable
private fun ReviewAndSubmitStep(
    data: ListingCreationData,
    onDataChange: (ListingCreationData) -> Unit,
    onValidationChange: (Boolean) -> Unit
) {
    LaunchedEffect(Unit) {
        onValidationChange(true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Review & Submit",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Review functionality will be implemented here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

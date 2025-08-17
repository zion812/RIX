package com.rio.rostry.marketplace.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rio.rostry.core.data.model.MarketplaceFilter

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MarketplaceFilterSheet(
    filter: MarketplaceFilter,
    availableBreeds: List<String>,
    availableLocations: List<String>,
    onFilterChange: (MarketplaceFilter) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    ModalBottomSheetLayout(
        sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Expanded),
        sheetContent = {
            FilterContent(
                filter = filter,
                availableBreeds = availableBreeds,
                availableLocations = availableLocations,
                onFilterChange = onFilterChange,
                onDismiss = onDismiss,
                onApply = onApply
            )
        },
        onDismissRequest = onDismiss,
        scrimColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
    ) {
        // Empty content as the sheet is always shown when this composable is used
    }
}

@Composable
fun FilterContent(
    filter: MarketplaceFilter,
    availableBreeds: List<String>,
    availableLocations: List<String>,
    onFilterChange: (MarketplaceFilter) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.h6
            )
            
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search query
        OutlinedTextField(
            value = filter.query ?: "",
            onValueChange = { onFilterChange(filter.copy(query = if (it.isEmpty()) null else it)) },
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Price range
        Text("Price Range", style = MaterialTheme.typography.subtitle1)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = filter.minPrice?.toString() ?: "",
                onValueChange = { 
                    val value = it.toDoubleOrNull()
                    onFilterChange(filter.copy(minPrice = value))
                },
                label = { Text("Min Price") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            OutlinedTextField(
                value = filter.maxPrice?.toString() ?: "",
                onValueChange = { 
                    val value = it.toDoubleOrNull()
                    onFilterChange(filter.copy(maxPrice = value))
                },
                label = { Text("Max Price") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Breed filter
        Text("Breed", style = MaterialTheme.typography.subtitle1)
        ExposedDropdownMenuBox(
            expanded = false, // Simplified for this example
            onExpandedChange = { }
        ) {
            OutlinedTextField(
                value = filter.breed ?: "",
                onValueChange = { onFilterChange(filter.copy(breed = if (it.isEmpty()) null else it)) },
                label = { Text("Breed") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Gender filter
        Text("Gender", style = MaterialTheme.typography.subtitle1)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(
                selected = filter.gender == null,
                onClick = { onFilterChange(filter.copy(gender = null)) },
                label = { Text("Any") }
            )
            
            FilterChip(
                selected = filter.gender == "MALE",
                onClick = { onFilterChange(filter.copy(gender = "MALE")) },
                label = { Text("Male") }
            )
            
            FilterChip(
                selected = filter.gender == "FEMALE",
                onClick = { onFilterChange(filter.copy(gender = "FEMALE")) },
                label = { Text("Female") }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Age range
        Text("Age Range (weeks)", style = MaterialTheme.typography.subtitle1)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = filter.ageWeeksMin?.toString() ?: "",
                onValueChange = { 
                    val value = it.toIntOrNull()
                    onFilterChange(filter.copy(ageWeeksMin = value))
                },
                label = { Text("Min Age") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            OutlinedTextField(
                value = filter.ageWeeksMax?.toString() ?: "",
                onValueChange = { 
                    val value = it.toIntOrNull()
                    onFilterChange(filter.copy(ageWeeksMax = value))
                },
                label = { Text("Max Age") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Location filter
        Text("Location", style = MaterialTheme.typography.subtitle1)
        ExposedDropdownMenuBox(
            expanded = false, // Simplified for this example
            onExpandedChange = { }
        ) {
            OutlinedTextField(
                value = filter.location ?: "",
                onValueChange = { onFilterChange(filter.copy(location = if (it.isEmpty()) null else it)) },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Availability filter
        Text("Availability", style = MaterialTheme.typography.subtitle1)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(
                selected = filter.availability == MarketplaceFilter.AvailabilityFilter.AVAILABLE_ONLY,
                onClick = { 
                    onFilterChange(
                        filter.copy(
                            availability = MarketplaceFilter.AvailabilityFilter.AVAILABLE_ONLY
                        )
                    )
                },
                label = { Text("Available Only") }
            )
            
            FilterChip(
                selected = filter.availability == MarketplaceFilter.AvailabilityFilter.ALL,
                onClick = { 
                    onFilterChange(
                        filter.copy(
                            availability = MarketplaceFilter.AvailabilityFilter.ALL
                        )
                    )
                },
                label = { Text("All") }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sort by
        Text("Sort By", style = MaterialTheme.typography.subtitle1)
        Column {
            MarketplaceFilter.SortBy.values().forEach { sortBy ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = filter.sortBy == sortBy,
                        onClick = { onFilterChange(filter.copy(sortBy = sortBy)) }
                    )
                    Text(
                        text = when (sortBy) {
                            MarketplaceFilter.SortBy.NEWEST -> "Newest First"
                            MarketplaceFilter.SortBy.OLDEST -> "Oldest First"
                            MarketplaceFilter.SortBy.PRICE_LOW_TO_HIGH -> "Price: Low to High"
                            MarketplaceFilter.SortBy.PRICE_HIGH_TO_LOW -> "Price: High to Low"
                            MarketplaceFilter.SortBy.AGE_YOUNG_TO_OLD -> "Age: Young to Old"
                            MarketplaceFilter.SortBy.AGE_OLD_TO_YOUNG -> "Age: Old to Young"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .align(androidx.compose.ui.Alignment.CenterVertically)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Apply button
        Button(
            onClick = onApply,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Apply Filters")
        }
    }
}

@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.padding(end = 8.dp),
        color = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
        contentColor = if (selected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface,
        shape = MaterialTheme.shapes.medium,
        elevation = if (selected) 4.dp else 0.dp
    ) {
        TextButton(
            onClick = onClick,
            content = label
        )
    }
}
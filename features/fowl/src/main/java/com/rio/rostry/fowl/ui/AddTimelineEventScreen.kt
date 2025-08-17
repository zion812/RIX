package com.rio.rostry.fowl.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rio.rostry.fowl.ui.viewmodels.AddTimelineEventViewModel

/**
 * Screen for adding timeline events for a fowl
 */
@Composable
fun AddTimelineEventScreen(
    fowlId: String,
    onEventAdded: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AddTimelineEventViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(fowlId) {
        viewModel.setFowlId(fowlId)
    }
    
    AddTimelineEventContent(
        uiState = uiState,
        onEventTypeChange = viewModel::onEventTypeChange,
        onTitleChange = viewModel::onTitleChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onMediaReferenceChange = viewModel::onMediaReferenceChange,
        onAddEvent = {
            viewModel.addEvent(
                onSuccess = onEventAdded,
                onError = { /* Handle error */ }
            )
        },
        onCancel = onCancel
    )
}

@Composable
fun AddTimelineEventContent(
    uiState: AddTimelineEventUiState,
    onEventTypeChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onMediaReferenceChange: (String) -> Unit,
    onAddEvent: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Add Timeline Event",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            uiState.fowl?.let { fowl ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = fowl.name ?: "Unnamed Fowl",
                            style = MaterialTheme.typography.h6
                        )
                        Text(
                            text = "Breed: ${fowl.breedPrimary}",
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
            }
            
            // Event type dropdown
            ExposedDropdownMenuBox(
                expanded = uiState.isEventTypeMenuExpanded,
                onExpandedChange = { onEventTypeChange(if (it) uiState.eventTypes.first() else "") }
            ) {
                OutlinedTextField(
                    value = uiState.selectedEventType,
                    onValueChange = {},
                    label = { Text("Event Type") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = uiState.isEventTypeMenuExpanded
                        )
                    }
                )
                ExposedDropdownMenu(
                    expanded = uiState.isEventTypeMenuExpanded,
                    onDismissRequest = { onEventTypeChange("") }
                ) {
                    uiState.eventTypes.forEach { eventType ->
                        DropdownMenuItem(
                            onClick = { onEventTypeChange(eventType) }
                        ) {
                            Text(text = eventType)
                        }
                    }
                }
            }
            
            OutlinedTextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                label = { Text("Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = uiState.mediaReference,
                onValueChange = onMediaReferenceChange,
                label = { Text("Media Reference") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = onAddEvent,
                    enabled = uiState.isFormValid
                ) {
                    Text("Add Event")
                }
            }
        }
    }
}

data class AddTimelineEventUiState(
    val isLoading: Boolean = false,
    val fowl: Fowl? = null,
    val eventTypes: List<String> = listOf("BREEDING", "HEALTH", "TRANSFER", "VACCINATION", "SALE", "DEATH"),
    val selectedEventType: String = "",
    val isEventTypeMenuExpanded: Boolean = false,
    val title: String = "",
    val description: String = "",
    val mediaReference: String = "",
    val isFormValid: Boolean = false
) {
    // Placeholder Fowl class - would be imported from the actual model
    data class Fowl(
        val id: String,
        val name: String?,
        val breedPrimary: String
    )
}
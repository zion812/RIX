package com.rio.rostry.fowl.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rio.rostry.fowl.ui.viewmodels.InitiateTransferViewModel

/**
 * Screen for initiating a fowl transfer to another user
 */
@Composable
fun InitiateTransferScreen(
    fowlId: String,
    onTransferInitiated: () -> Unit,
    onCancel: () -> Unit,
    viewModel: InitiateTransferViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(fowlId) {
        viewModel.loadFowlDetails(fowlId)
    }
    
    InitiateTransferContent(
        uiState = uiState,
        onPriceChange = viewModel::onPriceChange,
        onColorChange = viewModel::onColorChange,
        onAgeChange = viewModel::onAgeChange,
        onWeightChange = viewModel::onWeightChange,
        onPhotoReferenceChange = viewModel::onPhotoReferenceChange,
        onInitiateTransfer = {
            viewModel.initiateTransfer(
                fowlId = fowlId,
                onSuccess = onTransferInitiated,
                onError = { /* Handle error */ }
            )
        },
        onCancel = onCancel
    )
}

@Composable
fun InitiateTransferContent(
    uiState: InitiateTransferUiState,
    onPriceChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onPhotoReferenceChange: (String) -> Unit,
    onInitiateTransfer: () -> Unit,
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
            text = "Initiate Fowl Transfer",
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
            
            OutlinedTextField(
                value = uiState.expectedPrice,
                onValueChange = onPriceChange,
                label = { Text("Expected Price (Coins)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            OutlinedTextField(
                value = uiState.expectedColor,
                onValueChange = onColorChange,
                label = { Text("Expected Color") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = uiState.expectedAge,
                onValueChange = onAgeChange,
                label = { Text("Expected Age (Weeks)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            OutlinedTextField(
                value = uiState.expectedWeight,
                onValueChange = onWeightChange,
                label = { Text("Expected Weight (Grams)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            OutlinedTextField(
                value = uiState.photoReference,
                onValueChange = onPhotoReferenceChange,
                label = { Text("Photo Reference") },
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
                    onClick = onInitiateTransfer,
                    enabled = uiState.isFormValid
                ) {
                    Text("Initiate Transfer")
                }
            }
        }
    }
}

data class InitiateTransferUiState(
    val isLoading: Boolean = false,
    val fowl: Fowl? = null,
    val expectedPrice: String = "",
    val expectedColor: String = "",
    val expectedAge: String = "",
    val expectedWeight: String = "",
    val photoReference: String = "",
    val isFormValid: Boolean = false
) {
    // Placeholder Fowl class - would be imported from the actual model
    data class Fowl(
        val id: String,
        val name: String?,
        val breedPrimary: String
    )
}
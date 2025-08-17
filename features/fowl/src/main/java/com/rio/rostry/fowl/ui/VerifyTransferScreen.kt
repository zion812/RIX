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
import com.rio.rostry.fowl.ui.viewmodels.VerifyTransferViewModel

/**
 * Screen for verifying a fowl transfer
 */
@Composable
fun VerifyTransferScreen(
    transferId: String,
    onTransferVerified: () -> Unit,
    onCancel: () -> Unit,
    viewModel: VerifyTransferViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(transferId) {
        viewModel.loadTransferDetails(transferId)
    }
    
    VerifyTransferContent(
        uiState = uiState,
        onPriceChange = viewModel::onPriceChange,
        onColorChange = viewModel::onColorChange,
        onAgeChange = viewModel::onAgeChange,
        onWeightChange = viewModel::onWeightChange,
        onPhotoReferenceChange = viewModel::onPhotoReferenceChange,
        onVerifyTransfer = {
            viewModel.verifyTransfer(
                transferId = transferId,
                onSuccess = onTransferVerified,
                onError = { /* Handle error */ }
            )
        },
        onCancel = onCancel
    )
}

@Composable
fun VerifyTransferContent(
    uiState: VerifyTransferUiState,
    onPriceChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onPhotoReferenceChange: (String) -> Unit,
    onVerifyTransfer: () -> Unit,
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
            text = "Verify Fowl Transfer",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            uiState.transfer?.let { transfer ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Transfer from: ${transfer.fromUserId}",
                            style = MaterialTheme.typography.body1
                        )
                        Text(
                            text = "Expected Price: ${transfer.expectedPrice}",
                            style = MaterialTheme.typography.body2
                        )
                        Text(
                            text = "Expected Color: ${transfer.expectedColor}",
                            style = MaterialTheme.typography.body2
                        )
                        Text(
                            text = "Expected Age: ${transfer.expectedAgeWeeks} weeks",
                            style = MaterialTheme.typography.body2
                        )
                        Text(
                            text = "Expected Weight: ${transfer.expectedWeightGrams} grams",
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
            }
            
            OutlinedTextField(
                value = uiState.actualPrice,
                onValueChange = onPriceChange,
                label = { Text("Actual Price (Coins)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            OutlinedTextField(
                value = uiState.actualColor,
                onValueChange = onColorChange,
                label = { Text("Actual Color") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = uiState.actualAge,
                onValueChange = onAgeChange,
                label = { Text("Actual Age (Weeks)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            OutlinedTextField(
                value = uiState.actualWeight,
                onValueChange = onWeightChange,
                label = { Text("Actual Weight (Grams)") },
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
                    onClick = onVerifyTransfer,
                    enabled = uiState.isFormValid
                ) {
                    Text("Verify Transfer")
                }
            }
        }
    }
}

data class VerifyTransferUiState(
    val isLoading: Boolean = false,
    val transfer: Transfer? = null,
    val actualPrice: String = "",
    val actualColor: String = "",
    val actualAge: String = "",
    val actualWeight: String = "",
    val photoReference: String = "",
    val isFormValid: Boolean = false
) {
    // Placeholder Transfer class - would be imported from the actual model
    data class Transfer(
        val id: String,
        val fromUserId: String,
        val expectedPrice: Double?,
        val expectedColor: String?,
        val expectedAgeWeeks: Int?,
        val expectedWeightGrams: Int?,
        val photoReference: String?
    )
}
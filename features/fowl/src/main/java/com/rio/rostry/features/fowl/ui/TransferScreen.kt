package com.rio.rostry.features.fowl.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rio.rostry.features.fowl.viewmodel.TransferViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    transferId: String?,
    onTransferCompleted: () -> Unit,
    viewModel: TransferViewModel = hiltViewModel()
) {
    var transferState by remember { mutableStateOf<TransferUiState>(TransferUiState.Idle) }
    var verificationDetails by remember { mutableStateOf(VerificationDetails()) }
    
    LaunchedEffect(transferId) {
        if (transferId != null) {
            viewModel.loadTransfer(transferId)
        }
    }
    
    LaunchedEffect(viewModel.uiState) {
        when (val state = viewModel.uiState) {
            is TransferUiState.Success -> {
                transferState = state
                if (state.transfer.status.name == "VERIFIED") {
                    onTransferCompleted()
                }
            }
            is TransferUiState.Error -> {
                // Handle error
            }
            else -> transferState = state
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (transferState) {
            is TransferUiState.Idle -> {
                Text("Initializing transfer...")
            }
            
            is TransferUiState.Loading -> {
                CircularProgressIndicator()
            }
            
            is TransferUiState.Pending -> {
                val transfer = transferState.transfer
                Text("Transfer Pending", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text("From: ${transfer.giverId}")
                Text("To: ${transfer.receiverId}")
                Text("Fowl ID: ${transfer.fowlId}")
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Verification form for receiver
                Text("Verification Details", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = verificationDetails.color,
                    onValueChange = { verificationDetails = verificationDetails.copy(color = it) },
                    label = { Text("Color") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = verificationDetails.weight.toString(),
                    onValueChange = { 
                        verificationDetails = verificationDetails.copy(
                            weight = it.toDoubleOrNull() ?: 0.0
                        ) 
                    },
                    label = { Text("Weight (kg)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = verificationDetails.ageWeeks.toString(),
                    onValueChange = { 
                        verificationDetails = verificationDetails.copy(
                            ageWeeks = it.toIntOrNull() ?: 0
                        ) 
                    },
                    label = { Text("Age (weeks)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        viewModel.verifyTransfer(
                            transferId = transfer.id,
                            verificationDetails = mapOf(
                                "color" to verificationDetails.color,
                                "weight_kg" to verificationDetails.weight,
                                "age_weeks" to verificationDetails.ageWeeks,
                                "photo_keys" to listOf<String>(), // In a real app, this would be actual photo keys
                                "location" to mapOf("lat" to 0.0, "lng" to 0.0),
                                "agreed_price_cents" to 0
                            )
                        )
                    }
                ) {
                    Text("Verify Transfer")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = { viewModel.rejectTransfer(transfer.id) }
                ) {
                    Text("Reject Transfer")
                }
            }
            
            is TransferUiState.Verified -> {
                val transfer = transferState.transfer
                Text("Transfer Verified!", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Transfer completed successfully")
                Text("Fowl ownership transferred to: ${transfer.receiverId}")
            }
            
            is TransferUiState.Rejected -> {
                Text("Transfer Rejected", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text("The transfer has been rejected")
            }
            
            is TransferUiState.Error -> {
                Text("Error: ${transferState.message}")
            }
            
            else -> {
                Text("Unknown state")
            }
        }
    }
}

data class VerificationDetails(
    val color: String = "",
    val weight: Double = 0.0,
    val ageWeeks: Int = 0
)

sealed class TransferUiState {
    object Idle : TransferUiState()
    object Loading : TransferUiState()
    data class Pending(val transfer: TransferUiStateModel) : TransferUiState()
    data class Verified(val transfer: TransferUiStateModel) : TransferUiState()
    data class Rejected(val transfer: TransferUiStateModel) : TransferUiState()
    data class Success(val transfer: TransferUiStateModel) : TransferUiState()
    data class Error(val message: String) : TransferUiState()
}

data class TransferUiStateModel(
    val id: String,
    val fowlId: String,
    val giverId: String,
    val receiverId: String,
    val status: TransferStatusUi,
    val verificationDetails: Map<String, Any>? = null
)

enum class TransferStatusUi {
    PENDING,
    VERIFIED,
    REJECTED
}
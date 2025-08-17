package com.rio.rostry.fowl.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rio.rostry.core.data.usecase.FowlRecordCreationData
import com.rio.rostry.fowl.ui.viewmodels.SimpleFowlViewModel
import com.rio.rostry.core.navigation.RIONavigation
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFowlRecordScreen(
    fowlId: String,
    navController: NavController,
    viewModel: SimpleFowlViewModel = hiltViewModel()
) {
    var recordType by remember { mutableStateOf("") }
    var recordDate by remember { mutableStateOf(Date()) }
    var description by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var proofUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    var vaccineType by remember { mutableStateOf("") }
    var vaccineDate by remember { mutableStateOf<Date?>(null) }
    var weeklyMetrics by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    
    val recordTypes = listOf(
        "VACCINATION" to "Vaccination",
        "GROWTH" to "Growth Check",
        "QUARANTINE" to "Quarantine",
        "MORTALITY" to "Mortality",
        "MILESTONE_5W" to "5 Week Milestone",
        "MILESTONE_20W" to "20 Week Milestone",
        "WEEKLY_UPDATE" to "Weekly Update"
    )
    
    val vaccineTypes = listOf(
        "Newcastle Disease Vaccine",
        "Infectious Bronchitis Vaccine",
        "Fowl Pox Vaccine",
        "Marek's Disease Vaccine",
        "Avian Influenza Vaccine"
    )
    
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Record") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Create the record
                        val recordData = FowlRecordCreationData(
                            fowlId = fowlId,
                            recordType = recordType,
                            recordDate = recordDate,
                            description = description,
                            proofUrls = proofUrls,
                            proofCount = proofUrls.size,
                            createdBy = "current_user_id" // In a real app, get the actual user ID
                        )
                        viewModel.addFowlRecord(recordData)
                        navController.navigateUp()
                    }) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.Check, contentDescription = "Save")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Record Type Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Record Type",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    recordTypes.forEach { (type, displayName) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = recordType == type,
                                onClick = { recordType = type }
                            )
                            Text(
                                text = displayName,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
            
            // Smart suggestions based on record type
            if (recordType == "VACCINATION") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Vaccination Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Vaccine type with suggestions
                        Column {
                            Text(
                                text = "Vaccine Type",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            // Suggested vaccine types
                            vaccineTypes.forEach { type ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = vaccineType == type,
                                        onClick = { vaccineType = type }
                                    )
                                    Text(
                                        text = type,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                            
                            // Custom vaccine type
                            OutlinedTextField(
                                value = vaccineType,
                                onValueChange = { vaccineType = it },
                                label = { Text("Other Vaccine Type") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Last vaccination suggestion
                        // In a real app, we would fetch the last vaccination record
                        Text(
                            text = "Last vaccination: None recorded",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (recordType == "WEEKLY_UPDATE") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Weekly Update Metrics",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Smart suggestions for weekly metrics
                        Text(
                            text = "Previous metrics: None recorded",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Weight input
                        OutlinedTextField(
                            value = weeklyMetrics["weight"] ?: "",
                            onValueChange = { 
                                weeklyMetrics = weeklyMetrics + ("weight" to it)
                            },
                            label = { Text("Weight (grams)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Feed consumption input
                        OutlinedTextField(
                            value = weeklyMetrics["feed"] ?: "",
                            onValueChange = { 
                                weeklyMetrics = weeklyMetrics + ("feed" to it)
                            },
                            label = { Text("Feed Consumption (grams)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Activity level input
                        OutlinedTextField(
                            value = weeklyMetrics["activity"] ?: "",
                            onValueChange = { 
                                weeklyMetrics = weeklyMetrics + ("activity" to it)
                            },
                            label = { Text("Activity Level") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            // Date Picker
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Record Date",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = dateFormat.format(recordDate))
                    }
                    
                    if (showDatePicker) {
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(
                                    onClick = { showDatePicker = false }
                                ) {
                                    Text("OK")
                                }
                            }
                        ) {
                            DatePicker(
                                state = rememberDatePickerState(
                                    initialSelectedDateMillis = recordDate.time
                                ),
                                onDateChange = { 
                                    it?.let { selectedDate -> 
                                        recordDate = Date(selectedDate) 
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            // Description
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }
            
            // Proof Documentation
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Proof Documentation",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Proof documents (photos, certificates, etc.) can be added to verify this record.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { 
                            // In a real implementation, this would open a file picker or camera
                            // For now, we'll simulate adding a proof
                            proofUrls = proofUrls + "local_file_path_${System.currentTimeMillis()}"
                        }
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Proof")
                    }
                    
                    // Display added proofs
                    if (proofUrls.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Added proofs:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        proofUrls.forEachIndexed { index, url ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Proof ${index + 1}",
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        // Remove this proof
                                        proofUrls = proofUrls.filterIndexed { i, _ -> i != index }
                                    }
                                ) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = "Uploaded",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
package com.rio.rostry.ui.traceability

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rio.rostry.core.fieldtesting.FieldTestingManager
import java.text.SimpleDateFormat
import java.util.*

// Data Models
data class FowlTraceability(
    val id: String,
    val name: String,
    val breed: String,
    val gender: String,
    val dateOfBirth: Date,
    val ageInMonths: Int,
    val weight: Double,
    val color: String,
    val registryId: String,
    val sire: FowlLineage?,
    val dam: FowlLineage?,
    val generation: Int,
    val bloodline: String,
    val certifications: List<Certification>
)

data class FowlLineage(
    val id: String,
    val name: String,
    val registryId: String,
    val breed: String
)

data class Certification(
    val id: String,
    val name: String,
    val issuedBy: String,
    val dateIssued: Date,
    val expiryDate: Date?,
    val certificateNumber: String
)

data class FamilyTree(
    val currentFowl: FowlLineage,
    val sire: FowlLineage?,
    val dam: FowlLineage?,
    val paternalGrandfather: FowlLineage?,
    val paternalGrandmother: FowlLineage?,
    val maternalGrandfather: FowlLineage?,
    val maternalGrandmother: FowlLineage?
)

data class BreedingRecord(
    val id: String,
    val date: Date,
    val partnerName: String,
    val partnerRegistryId: String,
    val method: String,
    val successRate: Int,
    val offspringCount: Int,
    val notes: String
)

data class HealthRecord(
    val id: String,
    val date: Date,
    val type: String,
    val veterinarian: String,
    val treatment: String,
    val nextDueDate: Date?,
    val notes: String
)

/**
 * Traceability and Family Tree Screen
 * Comprehensive lineage tracking and breeding records management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraceabilityScreen(
    navController: NavController,
    fowlId: String,
    fieldTestingManager: FieldTestingManager? = null
) {
    var selectedTab by remember { mutableStateOf(0) }
    var fowlDetails by remember { mutableStateOf<FowlTraceability?>(null) }
    var familyTree by remember { mutableStateOf<FamilyTree?>(null) }
    var breedingRecords by remember { mutableStateOf<List<BreedingRecord>>(emptyList()) }
    var healthRecords by remember { mutableStateOf<List<HealthRecord>>(emptyList()) }
    
    // Track screen view
    LaunchedEffect(Unit) {
        fieldTestingManager?.trackUserAction("traceability_screen_viewed", "fowl_id=$fowlId")
        
        // Load demo data
        fowlDetails = generateDemoFowlTraceability(fowlId)
        familyTree = generateDemoFamilyTree(fowlId)
        breedingRecords = generateDemoBreedingRecords(fowlId)
        healthRecords = generateDemoHealthRecords(fowlId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Traceability - ${fowlDetails?.name ?: "Loading..."}") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        fieldTestingManager?.trackUserAction("traceability_qr_generated")
                    }) {
                        Icon(Icons.Default.QrCode, contentDescription = "Generate QR Code")
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
            // Tab Row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { 
                        selectedTab = 0
                        fieldTestingManager?.trackUserAction("traceability_tab_overview")
                    },
                    text = { Text("Overview") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1
                        fieldTestingManager?.trackUserAction("traceability_tab_family_tree")
                    },
                    text = { Text("Family Tree") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { 
                        selectedTab = 2
                        fieldTestingManager?.trackUserAction("traceability_tab_breeding")
                    },
                    text = { Text("Breeding") }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { 
                        selectedTab = 3
                        fieldTestingManager?.trackUserAction("traceability_tab_health")
                    },
                    text = { Text("Health") }
                )
            }
            
            // Tab Content
            when (selectedTab) {
                0 -> OverviewTab(fowlDetails)
                1 -> FamilyTreeTab(familyTree)
                2 -> BreedingTab(breedingRecords)
                3 -> HealthTab(healthRecords)
            }
        }
    }
}

@Composable
private fun OverviewTab(fowlDetails: FowlTraceability?) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        fowlDetails?.let { fowl ->
            item {
                BasicInfoCard(fowl)
            }
            
            item {
                LineageCard(fowl)
            }
            
            item {
                CertificationsCard(fowl.certifications)
            }
            
            item {
                QRCodeCard(fowl.id)
            }
        }
    }
}

@Composable
private fun BasicInfoCard(fowl: FowlTraceability) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Basic Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            InfoRow("Name", fowl.name)
            InfoRow("Breed", fowl.breed)
            InfoRow("Gender", fowl.gender)
            InfoRow("Date of Birth", SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(fowl.dateOfBirth))
            InfoRow("Age", "${fowl.ageInMonths} months")
            InfoRow("Weight", "${fowl.weight} kg")
            InfoRow("Color", fowl.color)
            InfoRow("Registry ID", fowl.registryId)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun LineageCard(fowl: FowlTraceability) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Lineage Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            fowl.sire?.let { sire ->
                InfoRow("Sire (Father)", "${sire.name} (${sire.registryId})")
            }
            
            fowl.dam?.let { dam ->
                InfoRow("Dam (Mother)", "${dam.name} (${dam.registryId})")
            }
            
            InfoRow("Generation", fowl.generation.toString())
            InfoRow("Bloodline", fowl.bloodline)
        }
    }
}

@Composable
private fun CertificationsCard(certifications: List<Certification>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Certifications & Awards",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (certifications.isEmpty()) {
                Text(
                    text = "No certifications yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                certifications.forEach { cert ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFFD700), // Gold color
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = cert.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Issued by ${cert.issuedBy} on ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(cert.dateIssued)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            cert.expiryDate?.let {
                                Text(
                                    text = "Expires on ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    if (cert != certifications.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun QRCodeCard(fowlId: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "QR Code for Traceability",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Placeholder for QR code
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.QrCode,
                    contentDescription = "QR Code",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "Scan to view complete traceability information",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(onClick = { /* Generate and share QR code */ }) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share QR Code")
            }
        }
    }
}

@Composable
private fun FamilyTreeTab(familyTree: FamilyTree?) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Family Tree Visualization",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        familyTree?.let { tree ->
            item {
                FamilyTreeVisualization(tree)
            }
        }
    }
}

@Composable
private fun FamilyTreeVisualization(familyTree: FamilyTree) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Lineage Chart",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simplified family tree representation
            Column {
                // Grandparents level
                Text(
                    text = "Grandparents",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    familyTree.paternalGrandfather?.let { 
                        FamilyMemberCard(it, "Paternal Grandfather")
                    }
                    familyTree.paternalGrandmother?.let { 
                        FamilyMemberCard(it, "Paternal Grandmother")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Parents level
                Text(
                    text = "Parents",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    familyTree.sire?.let { 
                        FamilyMemberCard(it, "Sire (Father)")
                    }
                    familyTree.dam?.let { 
                        FamilyMemberCard(it, "Dam (Mother)")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Current fowl
                Text(
                    text = "Current",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    FamilyMemberCard(familyTree.currentFowl, "Current Fowl", isHighlighted = true)
                }
            }
        }
    }
}

@Composable
private fun FamilyMemberCard(fowl: FowlLineage, relationship: String, isHighlighted: Boolean = false) {
    Card(
        modifier = Modifier.width(140.dp),
        colors = if (isHighlighted) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = fowl.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = relationship,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = fowl.registryId,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BreedingTab(breedingRecords: List<BreedingRecord>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Breeding Records",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(breedingRecords) { record ->
            BreedingRecordCard(record)
        }
    }
}

@Composable
private fun BreedingRecordCard(record: BreedingRecord) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Breeding #${record.id}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(record.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            InfoRow("Partner", "${record.partnerName} (${record.partnerRegistryId})")
            InfoRow("Method", record.method)
            InfoRow("Success Rate", "${record.successRate}%")
            InfoRow("Offspring Count", record.offspringCount.toString())
            
            if (record.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Notes: ${record.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HealthTab(healthRecords: List<HealthRecord>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Health & Vaccination Records",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(healthRecords) { record ->
            HealthRecordCard(record)
        }
    }
}

@Composable
private fun HealthRecordCard(record: HealthRecord) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.type,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(record.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            InfoRow("Administered By", record.veterinarian)
            InfoRow("Treatment", record.treatment)
            
            if (record.nextDueDate != null) {
                InfoRow("Next Due", SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(record.nextDueDate))
            }
            
            if (record.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Notes: ${record.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Demo data generation functions
private fun generateDemoFowlTraceability(fowlId: String): FowlTraceability {
    return FowlTraceability(
        id = fowlId,
        name = "Champion Aseel",
        breed = "Aseel",
        gender = "Male",
        dateOfBirth = Calendar.getInstance().apply { add(Calendar.MONTH, -18) }.time,
        ageInMonths = 18,
        weight = 3.5,
        color = "Red",
        registryId = "ASEEL-2023-001",
        sire = FowlLineage("Thunder King", "ASEEL-2021-045"),
        dam = FowlLineage("Golden Queen", "ASEEL-2021-067"),
        generation = 3,
        bloodline = "Royal Aseel Bloodline",
        certifications = listOf(
            Certification(
                id = "cert_001",
                name = "Best in Show",
                issuedBy = "Karnataka Poultry Association",
                dateIssued = Calendar.getInstance().apply { add(Calendar.MONTH, -6) }.time,
                expiryDate = null,
                certificateNumber = "KPA-2023-BS-001"
            )
        )
    )
}

private fun generateDemoFamilyTree(fowlId: String): FamilyTree {
    return FamilyTree(
        currentFowl = FowlLineage("Champion Aseel", "ASEEL-2023-001"),
        sire = FowlLineage("Thunder King", "ASEEL-2021-045"),
        dam = FowlLineage("Golden Queen", "ASEEL-2021-067"),
        paternalGrandfather = FowlLineage("Storm Lord", "ASEEL-2019-023"),
        paternalGrandmother = FowlLineage("Lightning Lady", "ASEEL-2019-034"),
        maternalGrandfather = FowlLineage("Gold Rush", "ASEEL-2019-056"),
        maternalGrandmother = FowlLineage("Royal Beauty", "ASEEL-2019-078")
    )
}

private fun generateDemoBreedingRecords(fowlId: String): List<BreedingRecord> {
    return listOf(
        BreedingRecord(
            id = "breed_001",
            date = Calendar.getInstance().apply { add(Calendar.MONTH, -3) }.time,
            partnerName = "Silver Belle",
            partnerRegistryId = "ASEEL-2022-089",
            method = "Natural Breeding",
            successRate = 85,
            offspringCount = 8,
            notes = "Excellent fertility rate, healthy offspring"
        ),
        BreedingRecord(
            id = "breed_002",
            date = Calendar.getInstance().apply { add(Calendar.MONTH, -8) }.time,
            partnerName = "Ruby Princess",
            partnerRegistryId = "ASEEL-2022-034",
            method = "Natural Breeding",
            successRate = 75,
            offspringCount = 6,
            notes = "Good breeding pair, strong genetic traits"
        )
    )
}

private fun generateDemoHealthRecords(fowlId: String): List<HealthRecord> {
    return listOf(
        HealthRecord(
            id = "health_001",
            date = Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, -2) }.time,
            type = "Vaccination",
            veterinarian = "Dr. Rajesh Kumar",
            treatment = "Newcastle Disease Vaccine",
            nextDueDate = Calendar.getInstance().apply { add(Calendar.MONTH, 6) }.time,
            notes = "Annual vaccination completed successfully"
        ),
        HealthRecord(
            id = "health_002",
            date = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.time,
            type = "Health Checkup",
            veterinarian = "Dr. Priya Sharma",
            treatment = "General Health Assessment",
            nextDueDate = Calendar.getInstance().apply { add(Calendar.MONTH, 3) }.time,
            notes = "Excellent health condition, no issues found"
        )
    )
}
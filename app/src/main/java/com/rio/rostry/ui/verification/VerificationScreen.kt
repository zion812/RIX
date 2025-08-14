package com.rio.rostry.ui.verification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

/**
 * Verification Screen for KYC and document verification
 * Supports offline document upload with sync when connected
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationScreen(
    navController: NavController,
    fieldTestingManager: FieldTestingManager? = null
) {
    var verificationStatus by remember { mutableStateOf(VerificationStatus.UNVERIFIED) }
    var uploadedDocuments by remember { mutableStateOf<List<VerificationDocument>>(emptyList()) }
    var showUploadDialog by remember { mutableStateOf(false) }
    
    // Track screen view
    LaunchedEffect(Unit) {
        fieldTestingManager?.trackUserAction("verification_screen_viewed")
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Verification") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                VerificationStatusCard(verificationStatus)
            }
            
            item {
                VerificationBenefitsCard()
            }
            
            item {
                RequiredDocumentsCard(
                    uploadedDocuments = uploadedDocuments,
                    onUploadDocument = { 
                        showUploadDialog = true
                        fieldTestingManager?.trackUserAction("document_upload_initiated")
                    }
                )
            }
            
            item {
                VerificationBadgesCard()
            }
        }
    }
    
    if (showUploadDialog) {
        DocumentUploadDialog(
            onDismiss = { showUploadDialog = false },
            onDocumentUploaded = { document ->
                uploadedDocuments = uploadedDocuments + document
                showUploadDialog = false
                fieldTestingManager?.trackUserAction("document_uploaded", "type=${document.type}")
            }
        )
    }
}

@Composable
private fun VerificationStatusCard(status: VerificationStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                VerificationStatus.VERIFIED -> MaterialTheme.colorScheme.primaryContainer
                VerificationStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer
                VerificationStatus.UNVERIFIED -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    when (status) {
                        VerificationStatus.VERIFIED -> Icons.Default.Verified
                        VerificationStatus.PENDING -> Icons.Default.Schedule
                        VerificationStatus.UNVERIFIED -> Icons.Default.Warning
                    },
                    contentDescription = null,
                    tint = when (status) {
                        VerificationStatus.VERIFIED -> MaterialTheme.colorScheme.onPrimaryContainer
                        VerificationStatus.PENDING -> MaterialTheme.colorScheme.onSecondaryContainer
                        VerificationStatus.UNVERIFIED -> MaterialTheme.colorScheme.onErrorContainer
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Verification Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (status) {
                        VerificationStatus.VERIFIED -> MaterialTheme.colorScheme.onPrimaryContainer
                        VerificationStatus.PENDING -> MaterialTheme.colorScheme.onSecondaryContainer
                        VerificationStatus.UNVERIFIED -> MaterialTheme.colorScheme.onErrorContainer
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = when (status) {
                    VerificationStatus.VERIFIED -> "Your account is fully verified"
                    VerificationStatus.PENDING -> "Verification documents under review"
                    VerificationStatus.UNVERIFIED -> "Account verification required"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = when (status) {
                    VerificationStatus.VERIFIED -> MaterialTheme.colorScheme.onPrimaryContainer
                    VerificationStatus.PENDING -> MaterialTheme.colorScheme.onSecondaryContainer
                    VerificationStatus.UNVERIFIED -> MaterialTheme.colorScheme.onErrorContainer
                }
            )
            
            Text(
                text = when (status) {
                    VerificationStatus.VERIFIED -> "You can now access all premium features"
                    VerificationStatus.PENDING -> "We'll notify you once review is complete"
                    VerificationStatus.UNVERIFIED -> "Upload documents to unlock premium features"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = when (status) {
                    VerificationStatus.VERIFIED -> MaterialTheme.colorScheme.onPrimaryContainer
                    VerificationStatus.PENDING -> MaterialTheme.colorScheme.onSecondaryContainer
                    VerificationStatus.UNVERIFIED -> MaterialTheme.colorScheme.onErrorContainer
                }
            )
        }
    }
}

@Composable
private fun VerificationBenefitsCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Verification Benefits",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val benefits = listOf(
                "Higher visibility in marketplace",
                "Access to premium buyer network",
                "Verified seller badge",
                "Priority customer support",
                "Advanced listing features",
                "Participation in exclusive events"
            )
            
            benefits.forEach { benefit ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = benefit,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (benefit != benefits.last()) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun RequiredDocumentsCard(
    uploadedDocuments: List<VerificationDocument>,
    onUploadDocument: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Required Documents",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val requiredDocs = listOf(
                DocumentType.AADHAAR_CARD to "Aadhaar Card",
                DocumentType.PAN_CARD to "PAN Card",
                DocumentType.FARM_CERTIFICATE to "Farm Ownership Certificate",
                DocumentType.BANK_PASSBOOK to "Bank Account Proof"
            )
            
            requiredDocs.forEach { (type, name) ->
                val isUploaded = uploadedDocuments.any { it.type == type }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isUploaded) Icons.Default.CheckCircle else Icons.Default.Upload,
                            contentDescription = null,
                            tint = if (isUploaded) Color.Green else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    if (!isUploaded) {
                        TextButton(onClick = onUploadDocument) {
                            Text("Upload")
                        }
                    } else {
                        Text(
                            text = "Uploaded",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Green
                        )
                    }
                }
                
                if (type != requiredDocs.last().first) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun VerificationBadgesCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Available Badges",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val badges = listOf(
                Badge("Verified Farmer", "Complete KYC verification", Icons.Default.Verified, true),
                Badge("Premium Seller", "Maintain 4.5+ rating", Icons.Default.Star, false),
                Badge("Expert Breeder", "5+ successful breeding records", Icons.Default.Pets, false),
                Badge("Community Leader", "Active in discussions", Icons.Default.Group, false)
            )
            
            badges.forEach { badge ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        badge.icon,
                        contentDescription = null,
                        tint = if (badge.earned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = badge.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = badge.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (badge.earned) {
                        Text(
                            text = "Earned",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                if (badge != badges.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun DocumentUploadDialog(
    onDismiss: () -> Unit,
    onDocumentUploaded: (VerificationDocument) -> Unit
) {
    var selectedDocType by remember { mutableStateOf<DocumentType?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Document") },
        text = {
            Column {
                Text("Select document type:")
                Spacer(modifier = Modifier.height(8.dp))
                
                DocumentType.values().forEach { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedDocType == type,
                            onClick = { selectedDocType = type }
                        )
                        Text(
                            text = when (type) {
                                DocumentType.AADHAAR_CARD -> "Aadhaar Card"
                                DocumentType.PAN_CARD -> "PAN Card"
                                DocumentType.FARM_CERTIFICATE -> "Farm Certificate"
                                DocumentType.BANK_PASSBOOK -> "Bank Passbook"
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedDocType?.let { type ->
                        // Simulate document upload
                        val document = VerificationDocument(
                            id = "doc_${System.currentTimeMillis()}",
                            type = type,
                            fileName = "${type.name.lowercase()}_${System.currentTimeMillis()}.jpg",
                            uploadDate = java.util.Date(),
                            status = DocumentStatus.PENDING_REVIEW
                        )
                        onDocumentUploaded(document)
                    }
                },
                enabled = selectedDocType != null
            ) {
                Text("Upload")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Data classes
enum class VerificationStatus {
    UNVERIFIED,
    PENDING,
    VERIFIED
}

enum class DocumentType {
    AADHAAR_CARD,
    PAN_CARD,
    FARM_CERTIFICATE,
    BANK_PASSBOOK
}

enum class DocumentStatus {
    PENDING_REVIEW,
    APPROVED,
    REJECTED
}

data class VerificationDocument(
    val id: String,
    val type: DocumentType,
    val fileName: String,
    val uploadDate: java.util.Date,
    val status: DocumentStatus
)

data class Badge(
    val name: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val earned: Boolean
)

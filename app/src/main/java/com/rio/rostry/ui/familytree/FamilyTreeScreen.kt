package com.rio.rostry.ui.familytree

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rio.rostry.auth.FirebaseAuthService
import com.rio.rostry.core.database.di.DatabaseProvider
import com.rio.rostry.core.database.entities.FowlEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Enhanced Family Tree Screen - Phase 3.2
 * Interactive lineage visualization with breeding history and genetic tracking
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyTreeScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Database and auth setup
    val database = remember { DatabaseProvider.getDatabase(context) }
    val authService = remember { FirebaseAuthService() }
    val currentUserId = authService.getCurrentUserId() ?: "demo-user"

    // State
    var fowls by remember { mutableStateOf<List<FowlEntity>>(emptyList()) }
    var selectedFowl by remember { mutableStateOf<FowlEntity?>(null) }
    var familyTree by remember { mutableStateOf<FamilyTreeData?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var viewMode by remember { mutableStateOf(ViewMode.TREE) }

    // Load fowls
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val fowlDao = database.fowlDao()
            fowls = fowlDao.getFowlsByOwner(currentUserId)

            // Generate demo family tree if no fowls exist
            if (fowls.isEmpty()) {
                fowls = generateDemoFowls(currentUserId)
                fowls.forEach { fowlDao.insertFowl(it) }
            }

            // Build family tree
            familyTree = buildFamilyTree(fowls)
        } catch (e: Exception) {
            // Handle error
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Family Tree",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewMode = if (viewMode == ViewMode.TREE) ViewMode.LIST else ViewMode.TREE
                        }
                    ) {
                        Icon(
                            if (viewMode == ViewMode.TREE) Icons.Default.ViewList else Icons.Default.AccountTree,
                            contentDescription = "Toggle View"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading family tree...")
                }
            }
        } else {
            when (viewMode) {
                ViewMode.TREE -> {
                    FamilyTreeView(
                        familyTree = familyTree,
                        selectedFowl = selectedFowl,
                        onFowlSelected = { selectedFowl = it },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
                ViewMode.LIST -> {
                    FamilyListView(
                        fowls = fowls,
                        selectedFowl = selectedFowl,
                        onFowlSelected = { selectedFowl = it },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
            }
        }
    }
}

/**
 * Family Tree Visualization Component
 */
@Composable
private fun FamilyTreeView(
    familyTree: FamilyTreeData?,
    selectedFowl: FowlEntity?,
    onFowlSelected: (FowlEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (familyTree == null) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text("No family tree data available")
        }
        return
    }

    Column(
        modifier = modifier.padding(16.dp)
    ) {
        // Family Tree Stats
        FamilyTreeStatsCard(familyTree)

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Tree Visualization
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (familyTree.generations.isNotEmpty()) {
                    InteractiveFamilyTree(
                        familyTree = familyTree,
                        selectedFowl = selectedFowl,
                        onFowlSelected = onFowlSelected,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.AccountTree,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No breeding relationships found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Add parent-child relationships to see the family tree",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Selected Fowl Details
        selectedFowl?.let { fowl ->
            Spacer(modifier = Modifier.height(16.dp))
            SelectedFowlCard(fowl)
        }
    }
}

/**
 * Family List View Component
 */
@Composable
private fun FamilyListView(
    fowls: List<FowlEntity>,
    selectedFowl: FowlEntity?,
    onFowlSelected: (FowlEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "All Fowls (${fowls.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(fowls) { fowl ->
            FowlLineageCard(
                fowl = fowl,
                isSelected = selectedFowl?.id == fowl.id,
                onClick = { onFowlSelected(fowl) }
            )
        }
    }
}

/**
 * Family Tree Stats Card
 */
@Composable
private fun FamilyTreeStatsCard(familyTree: FamilyTreeData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = "Total Fowls",
                value = familyTree.totalFowls.toString(),
                icon = Icons.Default.Pets
            )
            StatItem(
                label = "Generations",
                value = familyTree.generations.size.toString(),
                icon = Icons.Default.AccountTree
            )
            StatItem(
                label = "Breeding Pairs",
                value = familyTree.breedingPairs.toString(),
                icon = Icons.Default.Favorite
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/**
 * Interactive Family Tree Component
 */
@Composable
private fun InteractiveFamilyTree(
    familyTree: FamilyTreeData,
    selectedFowl: FowlEntity?,
    onFowlSelected: (FowlEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier.verticalScroll(scrollState)
    ) {
        familyTree.generations.forEachIndexed { index, generation ->
            GenerationRow(
                generation = generation,
                generationNumber = index + 1,
                selectedFowl = selectedFowl,
                onFowlSelected = onFowlSelected
            )

            if (index < familyTree.generations.size - 1) {
                Spacer(modifier = Modifier.height(24.dp))
                // Connection lines between generations
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                ) {
                    drawConnectionLines(this)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Generation Row Component
 */
@Composable
private fun GenerationRow(
    generation: List<FowlEntity>,
    generationNumber: Int,
    selectedFowl: FowlEntity?,
    onFowlSelected: (FowlEntity) -> Unit
) {
    Column {
        Text(
            text = "Generation $generationNumber",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            generation.forEach { fowl ->
                FowlTreeNode(
                    fowl = fowl,
                    isSelected = selectedFowl?.id == fowl.id,
                    onClick = { onFowlSelected(fowl) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Fowl Tree Node Component
 */
@Composable
private fun FowlTreeNode(
    fowl: FowlEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .then(
                if (isSelected) {
                    Modifier.border(
                        2.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(8.dp)
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gender indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (fowl.gender) {
                            "MALE" -> Color(0xFF2196F3)
                            "FEMALE" -> Color(0xFFE91E63)
                            else -> MaterialTheme.colorScheme.outline
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (fowl.gender) {
                        "MALE" -> Icons.Default.Male
                        "FEMALE" -> Icons.Default.Female
                        else -> Icons.Default.QuestionMark
                    },
                    contentDescription = fowl.gender,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = fowl.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = fowl.breed,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            fowl.birthDate?.let { birthDate ->
                val formatter = SimpleDateFormat("yyyy", Locale.getDefault())
                Text(
                    text = formatter.format(birthDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Fowl Lineage Card for List View
 */
@Composable
private fun FowlLineageCard(
    fowl: FowlEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .then(
                if (isSelected) {
                    Modifier.border(
                        2.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(8.dp)
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gender indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when (fowl.gender) {
                            "MALE" -> Color(0xFF2196F3)
                            "FEMALE" -> Color(0xFFE91E63)
                            else -> MaterialTheme.colorScheme.outline
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (fowl.gender) {
                        "MALE" -> Icons.Default.Male
                        "FEMALE" -> Icons.Default.Female
                        else -> Icons.Default.QuestionMark
                    },
                    contentDescription = fowl.gender,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fowl.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${fowl.breed} • ${fowl.gender}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                fowl.birthDate?.let { birthDate ->
                    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    Text(
                        text = "Born: ${formatter.format(birthDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View Details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Selected Fowl Details Card
 */
@Composable
private fun SelectedFowlCard(fowl: FowlEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Selected: ${fowl.name}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Breed",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = fowl.breed,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Column {
                    Text(
                        text = "Gender",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = fowl.gender,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                fowl.weight?.let { weight ->
                    Column {
                        Text(
                            text = "Weight",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${weight}kg",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            fowl.description?.let { description ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Draw connection lines between generations
 */
private fun drawConnectionLines(drawScope: DrawScope) {
    with(drawScope) {
        val strokeWidth = 2.dp.toPx()
        val color = Color.Gray

        // Draw horizontal line
        drawLine(
            color = color,
            start = Offset(size.width * 0.2f, size.height / 2),
            end = Offset(size.width * 0.8f, size.height / 2),
            strokeWidth = strokeWidth
        )

        // Draw vertical connectors
        val connectorCount = 3
        for (i in 0 until connectorCount) {
            val x = size.width * (0.3f + i * 0.2f)
            drawLine(
                color = color,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = strokeWidth
            )
        }
    }
}

/**
 * Data structures for family tree
 */
data class FamilyTreeData(
    val generations: List<List<FowlEntity>>,
    val totalFowls: Int,
    val breedingPairs: Int
)

enum class ViewMode {
    TREE, LIST
}

/**
 * Build family tree from fowl list
 */
private fun buildFamilyTree(fowls: List<FowlEntity>): FamilyTreeData {
    // For demo purposes, organize fowls by age/birth date
    val sortedFowls = fowls.sortedBy { it.birthDate ?: Date(0) }

    // Group into generations (simplified logic)
    val generations = mutableListOf<List<FowlEntity>>()
    val generationSize = 3 // Max fowls per generation for demo

    for (i in sortedFowls.indices step generationSize) {
        val generation = sortedFowls.subList(
            i,
            minOf(i + generationSize, sortedFowls.size)
        )
        generations.add(generation)
    }

    // Calculate breeding pairs (simplified)
    val breedingPairs = fowls.count { it.gender == "MALE" } / 2

    return FamilyTreeData(
        generations = generations,
        totalFowls = fowls.size,
        breedingPairs = breedingPairs
    )
}

/**
 * Generate demo fowls for family tree demonstration
 */
private fun generateDemoFowls(ownerId: String): List<FowlEntity> {
    val breeds = listOf("Rhode Island Red", "Leghorn", "Brahma", "Orpington", "Sussex")
    val colors = listOf("Brown", "White", "Black", "Red", "Buff")
    val names = listOf(
        "Charlie", "Bella", "Max", "Luna", "Rocky", "Daisy",
        "Duke", "Ruby", "Zeus", "Pearl", "Thor", "Honey"
    )

    return (1..9).map { index ->
        val birthDate = Calendar.getInstance().apply {
            add(Calendar.YEAR, -(index / 3 + 1))
            add(Calendar.MONTH, -(index % 12))
        }.time

        FowlEntity(
            id = "demo-fowl-$index",
            ownerId = ownerId,
            name = names[index % names.size],
            breed = breeds[index % breeds.size],
            gender = if (index % 2 == 0) "MALE" else "FEMALE",
            birthDate = birthDate,
            color = colors[index % colors.size],
            weight = 2.0 + (index % 3) * 0.5,
            status = "ACTIVE",
            description = "Demo fowl for family tree visualization",
            region = "Demo Region",
            district = "Demo District",
            createdAt = Date(),
            updatedAt = Date()
        )
    }
}

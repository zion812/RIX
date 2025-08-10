package com.rio.rostry.familytree.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rio.rostry.familytree.domain.model.FowlNode
import com.rio.rostry.familytree.domain.model.FamilyTreeData
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * ✅ Memory-optimized family tree component for large datasets
 * Handles 200+ nodes efficiently with viewport culling and lazy rendering
 */
@Composable
fun MemoryOptimizedFamilyTree(
    familyTreeData: FamilyTreeData,
    modifier: Modifier = Modifier,
    onNodeClick: (FowlNode) -> Unit = {}
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    
    // ✅ Memory management state
    var visibleNodes by remember { mutableStateOf<List<FowlNode>>(emptyList()) }
    var isLowMemoryMode by remember { mutableStateOf(false) }
    var renderQuality by remember { mutableStateOf(RenderQuality.HIGH) }
    
    // ✅ Performance monitoring
    val memoryMonitor = remember { MemoryMonitor() }
    
    LaunchedEffect(familyTreeData, scale, offset, canvasSize) {
        // ✅ Update visible nodes based on viewport
        visibleNodes = calculateVisibleNodes(
            familyTreeData.fowls,
            offset,
            scale,
            canvasSize
        )
        
        // ✅ Adjust rendering quality based on memory pressure
        val memoryPressure = memoryMonitor.getMemoryPressure()
        renderQuality = when {
            memoryPressure > 0.9f -> RenderQuality.EMERGENCY
            memoryPressure > 0.8f -> RenderQuality.LOW
            memoryPressure > 0.6f -> RenderQuality.MEDIUM
            else -> RenderQuality.HIGH
        }
        
        isLowMemoryMode = memoryPressure > 0.8f
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        // ✅ Main family tree canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.1f, 5f)
                        offset += pan
                    }
                }
        ) {
            canvasSize = size
            
            // ✅ Draw based on current render quality
            when (renderQuality) {
                RenderQuality.HIGH -> drawHighQuality(
                    visibleNodes,
                    familyTreeData.relationships,
                    scale,
                    offset
                )
                RenderQuality.MEDIUM -> drawMediumQuality(
                    visibleNodes,
                    scale,
                    offset
                )
                RenderQuality.LOW -> drawLowQuality(
                    visibleNodes,
                    scale,
                    offset
                )
                RenderQuality.EMERGENCY -> drawEmergencyMode(
                    familyTreeData.fowls.take(10), // Only show 10 nodes
                    scale,
                    offset
                )
            }
        }
        
        // ✅ Memory status indicator
        if (isLowMemoryMode) {
            MemoryWarningIndicator(
                modifier = Modifier.align(Alignment.TopEnd),
                memoryPressure = memoryMonitor.getMemoryPressure(),
                onOptimize = {
                    // Force garbage collection and reduce quality
                    System.gc()
                    renderQuality = RenderQuality.LOW
                }
            )
        }
        
        // ✅ Performance stats (debug mode)
        if (BuildConfig.DEBUG) {
            PerformanceStats(
                modifier = Modifier.align(Alignment.TopStart),
                visibleNodeCount = visibleNodes.size,
                totalNodeCount = familyTreeData.fowls.size,
                renderQuality = renderQuality,
                memoryUsage = memoryMonitor.getMemoryUsagePercent()
            )
        }
    }
}

/**
 * ✅ Calculate visible nodes based on viewport culling
 */
private fun calculateVisibleNodes(
    allNodes: List<FowlNode>,
    offset: Offset,
    scale: Float,
    canvasSize: Size
): List<FowlNode> {
    if (canvasSize == Size.Zero) return emptyList()
    
    // ✅ Calculate viewport bounds
    val viewportLeft = -offset.x / scale
    val viewportTop = -offset.y / scale
    val viewportRight = viewportLeft + canvasSize.width / scale
    val viewportBottom = viewportTop + canvasSize.height / scale
    
    // ✅ Filter nodes within viewport with margin
    val margin = 100f // Extra margin for smooth scrolling
    return allNodes.filter { node ->
        node.x + node.radius >= viewportLeft - margin &&
        node.x - node.radius <= viewportRight + margin &&
        node.y + node.radius >= viewportTop - margin &&
        node.y - node.radius <= viewportBottom + margin
    }.sortedByDescending { it.radius } // Render larger nodes first
}

/**
 * ✅ High quality rendering with all features
 */
private fun DrawScope.drawHighQuality(
    nodes: List<FowlNode>,
    relationships: List<FamilyRelationship>,
    scale: Float,
    offset: Offset
) {
    // Draw connections first
    drawConnections(relationships, nodes, scale, offset, strokeWidth = 2.dp.toPx())
    
    // Draw nodes with full detail
    nodes.forEach { node ->
        drawDetailedNode(node, scale, offset)
    }
}

/**
 * ✅ Medium quality rendering with reduced details
 */
private fun DrawScope.drawMediumQuality(
    nodes: List<FowlNode>,
    scale: Float,
    offset: Offset
) {
    // Skip connections for performance
    
    // Draw nodes with medium detail
    nodes.forEach { node ->
        drawMediumNode(node, scale, offset)
    }
}

/**
 * ✅ Low quality rendering for performance
 */
private fun DrawScope.drawLowQuality(
    nodes: List<FowlNode>,
    scale: Float,
    offset: Offset
) {
    // Only draw essential nodes (generation 0-2)
    val essentialNodes = nodes.filter { it.generation <= 2 }
    
    essentialNodes.forEach { node ->
        drawSimpleNode(node, scale, offset)
    }
}

/**
 * ✅ Emergency mode for extreme memory pressure
 */
private fun DrawScope.drawEmergencyMode(
    nodes: List<FowlNode>,
    scale: Float,
    offset: Offset
) {
    // Only show root and immediate children
    val emergencyNodes = nodes.filter { it.generation <= 1 }
    
    emergencyNodes.forEach { node ->
        drawBasicCircle(node, scale, offset)
    }
    
    // Show warning text
    drawContext.canvas.nativeCanvas.drawText(
        "Memory Low - Simplified View",
        size.width / 2,
        50f,
        android.graphics.Paint().apply {
            color = android.graphics.Color.RED
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
        }
    )
}

/**
 * ✅ Draw detailed node with all features
 */
private fun DrawScope.drawDetailedNode(
    node: FowlNode,
    scale: Float,
    offset: Offset
) {
    val center = Offset(
        node.x * scale + offset.x,
        node.y * scale + offset.y
    )
    val radius = node.radius * scale
    
    // ✅ Node background with gradient
    val gradient = Brush.radialGradient(
        colors = listOf(
            getNodeColor(node).copy(alpha = 0.8f),
            getNodeColor(node).copy(alpha = 0.6f)
        ),
        center = center,
        radius = radius
    )
    
    drawCircle(
        brush = gradient,
        radius = radius,
        center = center
    )
    
    // ✅ Node border
    drawCircle(
        color = Color.White,
        radius = radius,
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )
    
    // ✅ Node text (if large enough)
    if (radius > 30) {
        drawText(
            textMeasurer = TextMeasurer(),
            text = node.fowl.name.take(10),
            topLeft = Offset(
                center.x - radius * 0.8f,
                center.y - 8.sp.toPx()
            ),
            style = TextStyle(
                color = Color.White,
                fontSize = (radius * 0.2f).sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
    
    // ✅ Generation indicator
    if (node.generation > 0) {
        drawCircle(
            color = Color.Yellow,
            radius = 8.dp.toPx(),
            center = Offset(center.x + radius * 0.7f, center.y - radius * 0.7f)
        )
    }
}

/**
 * ✅ Draw medium detail node
 */
private fun DrawScope.drawMediumNode(
    node: FowlNode,
    scale: Float,
    offset: Offset
) {
    val center = Offset(
        node.x * scale + offset.x,
        node.y * scale + offset.y
    )
    val radius = node.radius * scale
    
    // Simple colored circle
    drawCircle(
        color = getNodeColor(node),
        radius = radius,
        center = center
    )
    
    // Text only for larger nodes
    if (radius > 40) {
        drawText(
            textMeasurer = TextMeasurer(),
            text = node.fowl.name.take(6),
            topLeft = Offset(center.x - radius * 0.6f, center.y - 6.sp.toPx()),
            style = TextStyle(
                color = Color.White,
                fontSize = (radius * 0.15f).sp
            )
        )
    }
}

/**
 * ✅ Draw simple node for low quality
 */
private fun DrawScope.drawSimpleNode(
    node: FowlNode,
    scale: Float,
    offset: Offset
) {
    val center = Offset(
        node.x * scale + offset.x,
        node.y * scale + offset.y
    )
    val radius = node.radius * scale
    
    drawCircle(
        color = getNodeColor(node),
        radius = radius,
        center = center
    )
}

/**
 * ✅ Draw basic circle for emergency mode
 */
private fun DrawScope.drawBasicCircle(
    node: FowlNode,
    scale: Float,
    offset: Offset
) {
    val center = Offset(
        node.x * scale + offset.x,
        node.y * scale + offset.y
    )
    
    drawCircle(
        color = Color.Gray,
        radius = 20f,
        center = center
    )
}

/**
 * ✅ Draw connections between nodes
 */
private fun DrawScope.drawConnections(
    relationships: List<FamilyRelationship>,
    nodes: List<FowlNode>,
    scale: Float,
    offset: Offset,
    strokeWidth: Float
) {
    val nodeMap = nodes.associateBy { it.fowl.id }
    
    relationships.forEach { relationship ->
        val parentNode = nodeMap[relationship.parentId]
        val childNode = nodeMap[relationship.childId]
        
        if (parentNode != null && childNode != null) {
            val start = Offset(
                parentNode.x * scale + offset.x,
                parentNode.y * scale + offset.y
            )
            val end = Offset(
                childNode.x * scale + offset.x,
                childNode.y * scale + offset.y
            )
            
            drawLine(
                color = Color.Gray.copy(alpha = 0.6f),
                start = start,
                end = end,
                strokeWidth = strokeWidth
            )
        }
    }
}

/**
 * ✅ Get node color based on fowl properties
 */
private fun getNodeColor(node: FowlNode): Color {
    return when (node.fowl.gender) {
        "MALE" -> Color.Blue
        "FEMALE" -> Color.Magenta
        else -> Color.Gray
    }
}

/**
 * ✅ Memory warning indicator
 */
@Composable
private fun MemoryWarningIndicator(
    modifier: Modifier = Modifier,
    memoryPressure: Float,
    onOptimize: () -> Unit
) {
    Card(
        modifier = modifier.padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Red.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Memory: ${(memoryPressure * 100).toInt()}%",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = onOptimize,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "Optimize",
                    color = Color.Red,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * ✅ Performance statistics (debug mode)
 */
@Composable
private fun PerformanceStats(
    modifier: Modifier = Modifier,
    visibleNodeCount: Int,
    totalNodeCount: Int,
    renderQuality: RenderQuality,
    memoryUsage: Float
) {
    Card(
        modifier = modifier.padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "Nodes: $visibleNodeCount/$totalNodeCount",
                color = Color.White,
                fontSize = 10.sp
            )
            Text(
                text = "Quality: ${renderQuality.name}",
                color = Color.White,
                fontSize = 10.sp
            )
            Text(
                text = "Memory: ${(memoryUsage * 100).toInt()}%",
                color = Color.White,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * ✅ Render quality levels
 */
enum class RenderQuality {
    HIGH,
    MEDIUM,
    LOW,
    EMERGENCY
}

/**
 * ✅ Memory monitoring utility
 */
class MemoryMonitor {
    private val runtime = Runtime.getRuntime()
    
    fun getMemoryPressure(): Float {
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        return (usedMemory.toFloat() / maxMemory).coerceIn(0f, 1f)
    }
    
    fun getMemoryUsagePercent(): Float {
        return getMemoryPressure()
    }
}

/**
 * ✅ Family relationship data class
 */
data class FamilyRelationship(
    val parentId: String,
    val childId: String,
    val relationshipType: String = "parent-child"
)

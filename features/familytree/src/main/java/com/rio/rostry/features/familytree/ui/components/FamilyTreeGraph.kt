package com.rio.rostry.features.familytree.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.rio.rostry.features.familytree.domain.model.FowlNode
import kotlin.math.roundToInt

@Composable
fun FamilyTreeGraph(
    nodes: List<FowlNode>,
    modifier: Modifier = Modifier,
    onNodeClick: (FowlNode) -> Unit = {}
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var selectedNode by remember { mutableStateOf<FowlNode?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 2.5f)
                        offset += pan
                    }
                }
        ) {
            // Apply transformations
            val scaledSize = size * scale
            val currentOffset = offset + (size - scaledSize) / 2f

            // Draw connections first
            nodes.forEach { node ->
                node.parents.forEach { parentId ->
                    val parent = nodes.find { it.id == parentId }
                    if (parent != null) {
                        drawConnection(
                            start = getNodePosition(parent, nodes),
                            end = getNodePosition(node, nodes),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Draw nodes
            nodes.forEach { node ->
                val position = getNodePosition(node, nodes)
                drawNode(
                    position = position + currentOffset,
                    node = node,
                    isSelected = node == selectedNode,
                    scale = scale
                )
            }
        }
    }
}

private fun DrawScope.drawNode(
    position: Offset,
    node: FowlNode,
    isSelected: Boolean,
    scale: Float
) {
    val radius = 30f * scale
    val borderWidth = if (isSelected) 4f else 2f
    
    // Draw node circle
    drawCircle(
        color = when (node.verificationStatus) {
            "verified" -> Color.Green.copy(alpha = 0.2f)
            "pending" -> Color.Yellow.copy(alpha = 0.2f)
            else -> Color.Gray.copy(alpha = 0.2f)
        },
        radius = radius,
        center = position
    )
    
    // Draw border
    drawCircle(
        color = if (isSelected) 
            MaterialTheme.colorScheme.primary 
        else 
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        radius = radius,
        center = position,
        style = androidx.compose.ui.graphics.drawscope.Stroke(borderWidth)
    )
}

private fun DrawScope.drawConnection(
    start: Offset,
    end: Offset,
    color: Color
) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = 2f
    )
}

private fun getNodePosition(node: FowlNode, allNodes: List<FowlNode>): Offset {
    // Calculate generation level (depth in tree)
    val generation = calculateGeneration(node, allNodes)
    
    // Calculate horizontal position based on siblings
    val siblings = allNodes.filter { 
        calculateGeneration(it, allNodes) == generation 
    }
    val horizontalIndex = siblings.indexOf(node)
    
    // Layout parameters
    val verticalSpacing = 150f
    val horizontalSpacing = 120f
    
    return Offset(
        x = (horizontalIndex + 1) * horizontalSpacing,
        y = generation * verticalSpacing
    )
}

private fun calculateGeneration(node: FowlNode, allNodes: List<FowlNode>): Int {
    var generation = 0
    var current = node
    
    while (current.parents.isNotEmpty()) {
        generation++
        current = allNodes.find { it.id == current.parents.first() } ?: break
    }
    
    return generation
}

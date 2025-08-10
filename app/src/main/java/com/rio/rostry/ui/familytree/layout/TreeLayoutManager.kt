package com.rio.rostry.ui.familytree.layout

import android.graphics.RectF
import com.rio.rostry.ui.familytree.model.FowlNode
import com.rio.rostry.ui.familytree.model.TreeConnection
import kotlin.math.*

/**
 * Manages the layout calculation for fowl family tree visualization
 * Implements hierarchical positioning with collision detection
 */
class TreeLayoutManager {
    
    // Layout configuration
    private var nodeRadius = 40f
    private var horizontalSpacing = 120f
    private var verticalSpacing = 150f
    private var generationSpacing = 200f
    private var minNodeDistance = 100f
    
    // Layout state
    private var layoutBounds = RectF()
    private var generationLevels = mutableMapOf<Int, MutableList<FowlNode>>()
    private var layoutWidth = 0
    private var layoutHeight = 0
    
    /**
     * Calculate the complete tree layout
     */
    fun calculateLayout(
        nodes: List<FowlNode>,
        connections: List<TreeConnection>,
        viewWidth: Int,
        viewHeight: Int
    ) {
        if (nodes.isEmpty()) return
        
        layoutWidth = viewWidth
        layoutHeight = viewHeight
        
        // Group nodes by generation
        groupNodesByGeneration(nodes)
        
        // Calculate initial positions
        calculateInitialPositions()
        
        // Apply force-directed layout for better positioning
        applyForceDirectedLayout(nodes, connections)
        
        // Resolve collisions
        resolveCollisions(nodes)
        
        // Center the tree
        centerTree(nodes)
        
        // Update node bounds
        nodes.forEach { it.updateBounds() }
        
        // Calculate final layout bounds
        calculateLayoutBounds(nodes)
    }
    
    /**
     * Group nodes by their generation level
     */
    private fun groupNodesByGeneration(nodes: List<FowlNode>) {
        generationLevels.clear()
        
        nodes.forEach { node ->
            val generation = node.generation
            if (!generationLevels.containsKey(generation)) {
                generationLevels[generation] = mutableListOf()
            }
            generationLevels[generation]?.add(node)
        }
    }
    
    /**
     * Calculate initial positions based on generation hierarchy
     */
    private fun calculateInitialPositions() {
        val generations = generationLevels.keys.sorted()
        val centerX = layoutWidth / 2f
        
        generations.forEach { generation ->
            val nodesInGeneration = generationLevels[generation] ?: return@forEach
            val generationY = generation * generationSpacing
            
            // Sort nodes for better visual arrangement
            val sortedNodes = sortNodesInGeneration(nodesInGeneration)
            
            // Calculate horizontal positions
            val totalWidth = (sortedNodes.size - 1) * horizontalSpacing
            val startX = centerX - totalWidth / 2f
            
            sortedNodes.forEachIndexed { index, node ->
                node.x = startX + index * horizontalSpacing
                node.y = generationY
                node.targetX = node.x
                node.targetY = node.y
            }
        }
    }
    
    /**
     * Sort nodes within a generation for better visual arrangement
     */
    private fun sortNodesInGeneration(nodes: List<FowlNode>): List<FowlNode> {
        return nodes.sortedWith(compareBy<FowlNode> { it.fowl.gender }
            .thenBy { it.fowl.breed }
            .thenBy { it.fowl.name })
    }
    
    /**
     * Apply force-directed layout algorithm for natural positioning
     */
    private fun applyForceDirectedLayout(
        nodes: List<FowlNode>,
        connections: List<TreeConnection>,
        iterations: Int = 50
    ) {
        val damping = 0.9f
        val repulsionStrength = 1000f
        val attractionStrength = 0.1f
        val maxForce = 50f
        
        repeat(iterations) { iteration ->
            val forces = mutableMapOf<String, Pair<Float, Float>>()
            
            // Initialize forces
            nodes.forEach { node ->
                forces[node.id] = Pair(0f, 0f)
            }
            
            // Calculate repulsion forces between all nodes
            for (i in nodes.indices) {
                for (j in i + 1 until nodes.size) {
                    val node1 = nodes[i]
                    val node2 = nodes[j]
                    
                    val dx = node2.x - node1.x
                    val dy = node2.y - node1.y
                    val distance = max(sqrt(dx * dx + dy * dy), 1f)
                    
                    if (distance < minNodeDistance * 2) {
                        val force = repulsionStrength / (distance * distance)
                        val fx = force * dx / distance
                        val fy = force * dy / distance
                        
                        val force1 = forces[node1.id]!!
                        val force2 = forces[node2.id]!!
                        
                        forces[node1.id] = Pair(force1.first - fx, force1.second - fy)
                        forces[node2.id] = Pair(force2.first + fx, force2.second + fy)
                    }
                }
            }
            
            // Calculate attraction forces for connected nodes
            connections.forEach { connection ->
                val parent = connection.parent
                val child = connection.child
                
                val dx = child.x - parent.x
                val dy = child.y - parent.y
                val distance = sqrt(dx * dx + dy * dy)
                
                val idealDistance = when (connection.type) {
                    TreeConnection.Type.PATERNAL, TreeConnection.Type.MATERNAL -> generationSpacing
                    TreeConnection.Type.BREEDING -> horizontalSpacing * 0.8f
                    TreeConnection.Type.SIBLING -> horizontalSpacing
                }
                
                val force = attractionStrength * (distance - idealDistance)
                val fx = force * dx / max(distance, 1f)
                val fy = force * dy / max(distance, 1f)
                
                val parentForce = forces[parent.id]!!
                val childForce = forces[child.id]!!
                
                forces[parent.id] = Pair(parentForce.first + fx, parentForce.second + fy)
                forces[child.id] = Pair(childForce.first - fx, childForce.second - fy)
            }
            
            // Apply forces with damping
            nodes.forEach { node ->
                val force = forces[node.id]!!
                val fx = force.first.coerceIn(-maxForce, maxForce)
                val fy = force.second.coerceIn(-maxForce, maxForce)
                
                node.velocityX = (node.velocityX + fx) * damping
                node.velocityY = (node.velocityY + fy) * damping
                
                node.x += node.velocityX
                node.y += node.velocityY
                
                // Keep nodes within reasonable bounds
                node.x = node.x.coerceIn(-layoutWidth.toFloat(), layoutWidth * 2f)
                node.y = node.y.coerceIn(-layoutHeight.toFloat(), layoutHeight * 2f)
            }
        }
    }
    
    /**
     * Resolve node collisions using simple separation
     */
    private fun resolveCollisions(nodes: List<FowlNode>) {
        val iterations = 10
        
        repeat(iterations) {
            for (i in nodes.indices) {
                for (j in i + 1 until nodes.size) {
                    val node1 = nodes[i]
                    val node2 = nodes[j]
                    
                    val dx = node2.x - node1.x
                    val dy = node2.y - node1.y
                    val distance = sqrt(dx * dx + dy * dy)
                    val minDistance = (node1.radius + node2.radius) * 1.5f
                    
                    if (distance < minDistance && distance > 0) {
                        val overlap = minDistance - distance
                        val separationX = (dx / distance) * overlap * 0.5f
                        val separationY = (dy / distance) * overlap * 0.5f
                        
                        node1.x -= separationX
                        node1.y -= separationY
                        node2.x += separationX
                        node2.y += separationY
                    }
                }
            }
        }
    }
    
    /**
     * Center the tree within the view bounds
     */
    private fun centerTree(nodes: List<FowlNode>) {
        if (nodes.isEmpty()) return
        
        val bounds = getTreeBounds(nodes)
        val centerX = layoutWidth / 2f
        val centerY = layoutHeight / 2f
        
        val offsetX = centerX - bounds.centerX()
        val offsetY = centerY - bounds.centerY()
        
        nodes.forEach { node ->
            node.x += offsetX
            node.y += offsetY
        }
    }
    
    /**
     * Get the bounding rectangle of all nodes
     */
    fun getTreeBounds(nodes: List<FowlNode>): RectF {
        if (nodes.isEmpty()) return RectF()
        
        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE
        
        nodes.forEach { node ->
            minX = min(minX, node.x - node.radius)
            maxX = max(maxX, node.x + node.radius)
            minY = min(minY, node.y - node.radius)
            maxY = max(maxY, node.y + node.radius)
        }
        
        return RectF(minX, minY, maxX, maxY)
    }
    
    /**
     * Calculate and store the final layout bounds
     */
    private fun calculateLayoutBounds(nodes: List<FowlNode>) {
        layoutBounds = getTreeBounds(nodes)
    }
    
    /**
     * Get the calculated layout bounds
     */
    fun getLayoutBounds(): RectF = layoutBounds
    
    /**
     * Update layout configuration
     */
    fun updateConfiguration(
        nodeRadius: Float? = null,
        horizontalSpacing: Float? = null,
        verticalSpacing: Float? = null,
        generationSpacing: Float? = null
    ) {
        nodeRadius?.let { this.nodeRadius = it }
        horizontalSpacing?.let { this.horizontalSpacing = it }
        verticalSpacing?.let { this.verticalSpacing = it }
        generationSpacing?.let { this.generationSpacing = it }
    }
    
    /**
     * Find the optimal position for a new node
     */
    fun findOptimalPosition(
        newNode: FowlNode,
        existingNodes: List<FowlNode>,
        generation: Int
    ): Pair<Float, Float> {
        val generationY = generation * generationSpacing
        val nodesInGeneration = existingNodes.filter { it.generation == generation }
        
        if (nodesInGeneration.isEmpty()) {
            return Pair(layoutWidth / 2f, generationY)
        }
        
        // Find the best X position with minimum conflicts
        val candidatePositions = generateCandidatePositions(nodesInGeneration, generationY)
        
        return candidatePositions.minByOrNull { position ->
            calculatePositionScore(position.first, position.second, existingNodes)
        } ?: Pair(layoutWidth / 2f, generationY)
    }
    
    private fun generateCandidatePositions(
        nodesInGeneration: List<FowlNode>,
        y: Float
    ): List<Pair<Float, Float>> {
        val positions = mutableListOf<Pair<Float, Float>>()
        
        // Add positions between existing nodes
        val sortedNodes = nodesInGeneration.sortedBy { it.x }
        for (i in 0 until sortedNodes.size - 1) {
            val x = (sortedNodes[i].x + sortedNodes[i + 1].x) / 2f
            positions.add(Pair(x, y))
        }
        
        // Add positions at the ends
        if (sortedNodes.isNotEmpty()) {
            positions.add(Pair(sortedNodes.first().x - horizontalSpacing, y))
            positions.add(Pair(sortedNodes.last().x + horizontalSpacing, y))
        }
        
        return positions
    }
    
    private fun calculatePositionScore(
        x: Float,
        y: Float,
        existingNodes: List<FowlNode>
    ): Float {
        var score = 0f
        
        existingNodes.forEach { node ->
            val distance = sqrt((x - node.x).pow(2) + (y - node.y).pow(2))
            if (distance < minNodeDistance) {
                score += (minNodeDistance - distance) * 10f
            }
        }
        
        // Prefer positions closer to center
        val centerDistance = abs(x - layoutWidth / 2f)
        score += centerDistance * 0.1f
        
        return score
    }
}

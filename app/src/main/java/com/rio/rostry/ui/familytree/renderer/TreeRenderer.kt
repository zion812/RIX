package com.rio.rostry.ui.familytree.renderer

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.rio.rostry.R
import com.rio.rostry.ui.familytree.model.FowlNode
import com.rio.rostry.ui.familytree.model.TreeConnection
import com.rio.rostry.ui.familytree.model.VerificationLevel
import kotlin.math.*

/**
 * Handles rendering of the fowl family tree visualization
 * Optimized for performance with level-of-detail rendering
 */
class TreeRenderer(private val context: Context) {
    
    // Paint objects for different elements
    private val nodePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val connectionPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    // Path effects
    private val dashedPathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
    
    // Colors
    private val primaryColor = ContextCompat.getColor(context, R.color.rio_primary)
    private val surfaceColor = ContextCompat.getColor(context, R.color.surface)
    private val onSurfaceColor = ContextCompat.getColor(context, R.color.on_surface)
    private val verifiedColor = ContextCompat.getColor(context, R.color.success)
    private val unverifiedColor = ContextCompat.getColor(context, R.color.outline)
    
    // Icons
    private val maleIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_male)
    private val femaleIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_female)
    private val verifiedIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_verified)
    private val healthIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_health)
    
    // Dimensions
    private var baseNodeRadius = 30f
    private var baseStrokeWidth = 3f
    private var baseTextSize = 12f
    
    // Performance optimization
    private val nodeCache = mutableMapOf<String, Bitmap>()
    private var lastZoomLevel = 1f
    
    init {
        initialize()
    }
    
    fun initialize() {
        // Configure paints
        nodePaint.style = Paint.Style.FILL
        
        connectionPaint.style = Paint.Style.STROKE
        connectionPaint.strokeWidth = baseStrokeWidth
        connectionPaint.strokeCap = Paint.Cap.ROUND
        
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = baseTextSize
        textPaint.color = onSurfaceColor
        
        shadowPaint.style = Paint.Style.FILL
        shadowPaint.color = Color.BLACK
        shadowPaint.alpha = 50
        
        highlightPaint.style = Paint.Style.STROKE
        highlightPaint.strokeWidth = baseStrokeWidth * 2
        highlightPaint.color = primaryColor
    }
    
    /**
     * Draw all connections between nodes
     */
    fun drawConnections(
        canvas: Canvas,
        connections: List<TreeConnection>,
        visibleBounds: RectF,
        zoomLevel: Float
    ) {
        connections.forEach { connection ->
            if (connection.shouldRender(zoomLevel)) {
                drawConnection(canvas, connection, zoomLevel)
            }
        }
    }
    
    /**
     * Draw a single connection
     */
    private fun drawConnection(canvas: Canvas, connection: TreeConnection, zoomLevel: Float) {
        val parent = connection.parent
        val child = connection.child
        
        // Configure paint for this connection
        connectionPaint.color = connection.getConnectionColor()
        connectionPaint.strokeWidth = connection.getStrokeWidth(baseStrokeWidth * zoomLevel)
        connectionPaint.pathEffect = if (connection.isDashed()) dashedPathEffect else null
        
        // Draw connection line
        when (connection.type) {
            TreeConnection.Type.PATERNAL, TreeConnection.Type.MATERNAL -> {
                drawParentChildConnection(canvas, parent, child)
            }
            TreeConnection.Type.BREEDING -> {
                drawBreedingConnection(canvas, parent, child)
            }
            TreeConnection.Type.SIBLING -> {
                drawSiblingConnection(canvas, parent, child)
            }
        }
        
        // Draw connection decorations
        if (zoomLevel > 0.5f) {
            drawConnectionDecorations(canvas, connection, zoomLevel)
        }
    }
    
    private fun drawParentChildConnection(canvas: Canvas, parent: FowlNode, child: FowlNode) {
        // Draw curved connection
        val path = Path()
        val midY = (parent.y + child.y) / 2f
        
        path.moveTo(parent.x, parent.y + parent.radius)
        path.cubicTo(
            parent.x, midY,
            child.x, midY,
            child.x, child.y - child.radius
        )
        
        canvas.drawPath(path, connectionPaint)
    }
    
    private fun drawBreedingConnection(canvas: Canvas, fowl1: FowlNode, fowl2: FowlNode) {
        // Draw heart-shaped connection for breeding pairs
        val centerX = (fowl1.x + fowl2.x) / 2f
        val centerY = (fowl1.y + fowl2.y) / 2f
        
        canvas.drawLine(fowl1.x, fowl1.y, centerX, centerY, connectionPaint)
        canvas.drawLine(centerX, centerY, fowl2.x, fowl2.y, connectionPaint)
        
        // Draw heart symbol at center
        drawHeartSymbol(canvas, centerX, centerY, 8f)
    }
    
    private fun drawSiblingConnection(canvas: Canvas, sibling1: FowlNode, sibling2: FowlNode) {
        // Draw arc connection for siblings
        val path = Path()
        val midX = (sibling1.x + sibling2.x) / 2f
        val arcHeight = abs(sibling2.x - sibling1.x) * 0.2f
        
        path.moveTo(sibling1.x, sibling1.y)
        path.quadTo(midX, sibling1.y - arcHeight, sibling2.x, sibling2.y)
        
        canvas.drawPath(path, connectionPaint)
    }
    
    private fun drawConnectionDecorations(canvas: Canvas, connection: TreeConnection, zoomLevel: Float) {
        if (connection.isVerified) {
            val centerX = (connection.parent.x + connection.child.x) / 2f
            val centerY = (connection.parent.y + connection.child.y) / 2f
            
            verifiedIcon?.let { icon ->
                val size = (16 * zoomLevel).toInt()
                icon.setBounds(
                    (centerX - size/2).toInt(),
                    (centerY - size/2).toInt(),
                    (centerX + size/2).toInt(),
                    (centerY + size/2).toInt()
                )
                icon.draw(canvas)
            }
        }
    }
    
    /**
     * Draw all fowl nodes
     */
    fun drawNodes(
        canvas: Canvas,
        nodes: List<FowlNode>,
        visibleBounds: RectF,
        zoomLevel: Float
    ) {
        // Sort nodes by display priority
        val sortedNodes = nodes
            .filter { it.shouldRender(zoomLevel, visibleBounds) }
            .sortedBy { it.getDisplayPriority() }
        
        sortedNodes.forEach { node ->
            drawNode(canvas, node, zoomLevel)
        }
    }
    
    /**
     * Draw a single fowl node
     */
    private fun drawNode(canvas: Canvas, node: FowlNode, zoomLevel: Float) {
        val scaledRadius = node.radius * zoomLevel
        
        // Draw shadow
        if (zoomLevel > 0.3f) {
            canvas.drawCircle(
                node.x + 2f,
                node.y + 2f,
                scaledRadius,
                shadowPaint
            )
        }
        
        // Draw node background
        nodePaint.color = getNodeBackgroundColor(node)
        canvas.drawCircle(node.x, node.y, scaledRadius, nodePaint)
        
        // Draw health status border
        drawHealthStatusBorder(canvas, node, scaledRadius)
        
        // Draw gender indicator
        if (zoomLevel > 0.4f) {
            drawGenderIndicator(canvas, node, scaledRadius)
        }
        
        // Draw verification indicators
        if (zoomLevel > 0.5f) {
            drawVerificationIndicators(canvas, node, scaledRadius)
        }
        
        // Draw node text
        if (zoomLevel > 0.6f) {
            drawNodeText(canvas, node, scaledRadius, zoomLevel)
        }
        
        // Draw selection/highlight
        if (node.isSelected || node.isHighlighted) {
            drawNodeHighlight(canvas, node, scaledRadius)
        }
    }
    
    private fun getNodeBackgroundColor(node: FowlNode): Int {
        return when (node.getVerificationLevel()) {
            VerificationLevel.FULLY_VERIFIED -> Color.WHITE
            VerificationLevel.LINEAGE_VERIFIED -> 0xFFF8F9FA.toInt()
            VerificationLevel.HEALTH_VERIFIED -> 0xFFF1F8E9.toInt()
            VerificationLevel.UNVERIFIED -> 0xFFE0E0E0.toInt()
        }
    }
    
    private fun drawHealthStatusBorder(canvas: Canvas, node: FowlNode, radius: Float) {
        nodePaint.style = Paint.Style.STROKE
        nodePaint.strokeWidth = baseStrokeWidth
        nodePaint.color = node.getHealthStatusColor()
        
        canvas.drawCircle(node.x, node.y, radius, nodePaint)
        
        nodePaint.style = Paint.Style.FILL
    }
    
    private fun drawGenderIndicator(canvas: Canvas, node: FowlNode, radius: Float) {
        val icon = when (node.gender.lowercase()) {
            "male", "rooster" -> maleIcon
            "female", "hen" -> femaleIcon
            else -> null
        }
        
        icon?.let {
            val iconSize = (radius * 0.6f).toInt()
            val left = (node.x - iconSize/2).toInt()
            val top = (node.y - iconSize/2).toInt()
            
            it.setBounds(left, top, left + iconSize, top + iconSize)
            it.setTint(node.getGenderColor())
            it.draw(canvas)
        }
    }
    
    private fun drawVerificationIndicators(canvas: Canvas, node: FowlNode, radius: Float) {
        val iconSize = (radius * 0.3f).toInt()
        var offsetX = radius * 0.7f
        
        if (node.fowl.lineageVerified) {
            verifiedIcon?.let { icon ->
                val left = (node.x + offsetX - iconSize/2).toInt()
                val top = (node.y - radius + iconSize/2).toInt()
                
                icon.setBounds(left, top, left + iconSize, top + iconSize)
                icon.setTint(verifiedColor)
                icon.draw(canvas)
                
                offsetX -= iconSize * 1.2f
            }
        }
        
        if (node.fowl.healthCertified) {
            healthIcon?.let { icon ->
                val left = (node.x + offsetX - iconSize/2).toInt()
                val top = (node.y - radius + iconSize/2).toInt()
                
                icon.setBounds(left, top, left + iconSize, top + iconSize)
                icon.setTint(verifiedColor)
                icon.draw(canvas)
            }
        }
    }
    
    private fun drawNodeText(canvas: Canvas, node: FowlNode, radius: Float, zoomLevel: Float) {
        textPaint.textSize = baseTextSize * zoomLevel
        
        // Draw name
        val nameY = node.y + radius + textPaint.textSize + 5f
        canvas.drawText(node.name, node.x, nameY, textPaint)
        
        // Draw additional info at higher zoom levels
        if (zoomLevel > 0.8f) {
            textPaint.textSize = baseTextSize * zoomLevel * 0.8f
            
            // Draw breed
            val breedY = nameY + textPaint.textSize + 3f
            canvas.drawText(node.breed, node.x, breedY, textPaint)
            
            // Draw age
            if (zoomLevel > 1.0f) {
                val ageY = breedY + textPaint.textSize + 3f
                canvas.drawText(node.getFormattedAge(), node.x, ageY, textPaint)
            }
        }
    }
    
    private fun drawNodeHighlight(canvas: Canvas, node: FowlNode, radius: Float) {
        highlightPaint.strokeWidth = baseStrokeWidth * 2
        highlightPaint.color = if (node.isSelected) primaryColor else Color.YELLOW
        
        canvas.drawCircle(node.x, node.y, radius + 5f, highlightPaint)
    }
    
    private fun drawHeartSymbol(canvas: Canvas, x: Float, y: Float, size: Float) {
        val path = Path()
        
        // Create heart shape
        path.moveTo(x, y + size/4)
        path.cubicTo(x - size/2, y - size/2, x - size, y + size/4, x, y + size)
        path.cubicTo(x + size, y + size/4, x + size/2, y - size/2, x, y + size/4)
        
        nodePaint.color = Color.RED
        canvas.drawPath(path, nodePaint)
    }
    
    /**
     * Draw UI overlays (zoom controls, legends, etc.)
     */
    fun drawOverlays(canvas: Canvas, viewWidth: Int, viewHeight: Int) {
        // Draw zoom level indicator
        drawZoomIndicator(canvas, viewWidth, viewHeight)
        
        // Draw legend
        drawLegend(canvas, viewWidth, viewHeight)
    }
    
    private fun drawZoomIndicator(canvas: Canvas, viewWidth: Int, viewHeight: Int) {
        val text = "Zoom: ${String.format("%.1f", lastZoomLevel)}x"
        val padding = 20f
        
        textPaint.textSize = 14f
        textPaint.color = onSurfaceColor
        
        canvas.drawText(
            text,
            viewWidth - padding,
            padding + textPaint.textSize,
            textPaint
        )
    }
    
    private fun drawLegend(canvas: Canvas, viewWidth: Int, viewHeight: Int) {
        // Draw legend background
        val legendWidth = 200f
        val legendHeight = 150f
        val padding = 20f
        
        val legendRect = RectF(
            padding,
            viewHeight - legendHeight - padding,
            padding + legendWidth,
            viewHeight - padding
        )
        
        nodePaint.color = Color.WHITE
        nodePaint.alpha = 200
        canvas.drawRoundRect(legendRect, 10f, 10f, nodePaint)
        
        // Draw legend items
        textPaint.textSize = 12f
        textPaint.color = onSurfaceColor
        
        var y = legendRect.top + 25f
        canvas.drawText("Legend", legendRect.left + 10f, y, textPaint)
        
        y += 20f
        drawLegendItem(canvas, legendRect.left + 10f, y, verifiedColor, "Verified")
        
        y += 20f
        drawLegendItem(canvas, legendRect.left + 10f, y, unverifiedColor, "Unverified")
        
        y += 20f
        drawLegendItem(canvas, legendRect.left + 10f, y, Color.BLUE, "Male")
        
        y += 20f
        drawLegendItem(canvas, legendRect.left + 10f, y, Color.MAGENTA, "Female")
    }
    
    private fun drawLegendItem(canvas: Canvas, x: Float, y: Float, color: Int, text: String) {
        nodePaint.color = color
        canvas.drawCircle(x + 8f, y - 4f, 6f, nodePaint)
        
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText(text, x + 20f, y, textPaint)
        textPaint.textAlign = Paint.Align.CENTER
    }
    
    /**
     * Get the current node radius
     */
    fun getNodeRadius(): Float = baseNodeRadius
    
    /**
     * Update zoom level for rendering optimizations
     */
    fun updateZoomLevel(zoomLevel: Float) {
        lastZoomLevel = zoomLevel
        
        // Clear cache if zoom level changed significantly
        if (abs(zoomLevel - lastZoomLevel) > 0.2f) {
            nodeCache.clear()
        }
    }
    
    /**
     * Clear rendering cache
     */
    fun clearCache() {
        nodeCache.clear()
    }
}

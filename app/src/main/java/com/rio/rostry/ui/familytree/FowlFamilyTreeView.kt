package com.rio.rostry.ui.familytree

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.rio.rostry.R
import com.rio.rostry.core.database.entities.RoosterEntity
import com.rio.rostry.ui.familytree.model.FowlNode
import com.rio.rostry.ui.familytree.model.TreeConnection
import com.rio.rostry.ui.familytree.layout.TreeLayoutManager
import com.rio.rostry.ui.familytree.renderer.TreeRenderer
import com.rio.rostry.ui.familytree.gesture.TreeGestureHandler
import kotlin.math.*

/**
 * Custom view for displaying interactive fowl family trees
 * Supports multi-generational lineage visualization with touch interactions
 */
class FowlFamilyTreeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Core components
    private val layoutManager = TreeLayoutManager()
    private val renderer = TreeRenderer(context)
    private val gestureHandler = TreeGestureHandler(this)
    
    // Data
    private var rootFowl: RoosterEntity? = null
    private var fowlNodes = mutableListOf<FowlNode>()
    private var connections = mutableListOf<TreeConnection>()
    
    // View state
    private var viewMatrix = Matrix()
    private var inverseMatrix = Matrix()
    private var currentScale = 1f
    private var minScale = 0.1f
    private var maxScale = 2f
    
    // Touch handling
    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    private val gestureDetector = GestureDetector(context, GestureListener())
    private var isScaling = false
    
    // Animation
    private var zoomAnimator: ValueAnimator? = null
    private var panAnimator: ValueAnimator? = null
    
    // Callbacks
    var onFowlClickListener: ((RoosterEntity) -> Unit)? = null
    var onFowlLongClickListener: ((RoosterEntity) -> Unit)? = null
    var onConnectionClickListener: ((TreeConnection) -> Unit)? = null
    
    // Performance optimization
    private var visibleBounds = RectF()
    private var lastUpdateTime = 0L
    private val frameRateThrottle = 16L // 60fps
    
    init {
        setupView()
        setupAccessibility()
    }

    private fun setupView() {
        // Enable hardware acceleration
        setLayerType(LAYER_TYPE_HARDWARE, null)
        
        // Configure view properties
        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true
        isLongClickable = true
        
        // Initialize renderer
        renderer.initialize()
    }

    private fun setupAccessibility() {
        ViewCompat.setAccessibilityDelegate(this, object : androidx.core.view.AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.className = FowlFamilyTreeView::class.java.name
                info.contentDescription = "Family tree with ${fowlNodes.size} fowl members"
                info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD)
                info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD)
            }
        })
    }

    /**
     * Set the root fowl and build the family tree
     */
    fun setRootFowl(fowl: RoosterEntity, familyData: List<RoosterEntity>) {
        rootFowl = fowl
        buildFamilyTree(fowl, familyData)
        requestLayout()
        invalidate()
    }

    /**
     * Build the family tree structure from data
     */
    private fun buildFamilyTree(root: RoosterEntity, allFowl: List<RoosterEntity>) {
        fowlNodes.clear()
        connections.clear()
        
        // Create nodes for all fowl
        val nodeMap = mutableMapOf<String, FowlNode>()
        allFowl.forEach { fowl ->
            val node = FowlNode(
                fowl = fowl,
                x = 0f,
                y = 0f,
                generation = calculateGeneration(fowl, root, allFowl)
            )
            fowlNodes.add(node)
            nodeMap[fowl.id] = node
        }
        
        // Create connections based on parent-child relationships
        allFowl.forEach { fowl ->
            fowl.fatherId?.let { fatherId ->
                nodeMap[fatherId]?.let { fatherNode ->
                    nodeMap[fowl.id]?.let { childNode ->
                        connections.add(
                            TreeConnection(
                                parent = fatherNode,
                                child = childNode,
                                type = TreeConnection.Type.PATERNAL,
                                isVerified = fowl.lineageVerified
                            )
                        )
                    }
                }
            }
            
            fowl.motherId?.let { motherId ->
                nodeMap[motherId]?.let { motherNode ->
                    nodeMap[fowl.id]?.let { childNode ->
                        connections.add(
                            TreeConnection(
                                parent = motherNode,
                                child = childNode,
                                type = TreeConnection.Type.MATERNAL,
                                isVerified = fowl.lineageVerified
                            )
                        )
                    }
                }
            }
        }
        
        // Calculate layout
        layoutManager.calculateLayout(fowlNodes, connections, width, height)
    }

    private fun calculateGeneration(fowl: RoosterEntity, root: RoosterEntity, allFowl: List<RoosterEntity>): Int {
        if (fowl.id == root.id) return 0
        
        // Simple generation calculation - can be enhanced
        var generation = 0
        var current = fowl
        val visited = mutableSetOf<String>()
        
        while (current.fatherId != null || current.motherId != null) {
            if (current.id in visited) break // Prevent infinite loops
            visited.add(current.id)
            
            val parent = allFowl.find { it.id == current.fatherId || it.id == current.motherId }
            if (parent != null) {
                current = parent
                generation++
                if (generation > 10) break // Safety limit
            } else {
                break
            }
        }
        
        return generation
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom
        
        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // Recalculate layout for new dimensions
        if (fowlNodes.isNotEmpty()) {
            layoutManager.calculateLayout(fowlNodes, connections, w, h)
            centerTree()
        }
        
        // Update visible bounds
        updateVisibleBounds()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Throttle frame rate for performance
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime < frameRateThrottle) {
            return
        }
        lastUpdateTime = currentTime
        
        // Apply transformations
        canvas.save()
        canvas.concat(viewMatrix)
        
        // Update visible bounds for culling
        updateVisibleBounds()
        
        // Render tree components
        renderer.drawConnections(canvas, connections, visibleBounds, currentScale)
        renderer.drawNodes(canvas, fowlNodes, visibleBounds, currentScale)
        
        canvas.restore()
        
        // Draw UI overlays (zoom controls, etc.)
        renderer.drawOverlays(canvas, width, height)
    }

    private fun updateVisibleBounds() {
        val bounds = RectF(0f, 0f, width.toFloat(), height.toFloat())
        inverseMatrix.mapRect(bounds)
        visibleBounds = bounds
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var handled = false
        
        // Handle scale gestures first
        handled = scaleGestureDetector.onTouchEvent(event) || handled
        
        // Handle other gestures if not scaling
        if (!isScaling) {
            handled = gestureDetector.onTouchEvent(event) || handled
        }
        
        // Handle gesture end
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            isScaling = false
        }
        
        return handled || super.onTouchEvent(event)
    }

    /**
     * Scale gesture listener
     */
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            isScaling = true
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            val newScale = (currentScale * scaleFactor).coerceIn(minScale, maxScale)
            
            if (newScale != currentScale) {
                val focusX = detector.focusX
                val focusY = detector.focusY
                
                // Scale around focal point
                viewMatrix.postScale(
                    newScale / currentScale,
                    newScale / currentScale,
                    focusX,
                    focusY
                )
                
                currentScale = newScale
                viewMatrix.invert(inverseMatrix)
                invalidate()
            }
            
            return true
        }
    }

    /**
     * General gesture listener
     */
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean = true

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (!isScaling) {
                viewMatrix.postTranslate(-distanceX, -distanceY)
                viewMatrix.invert(inverseMatrix)
                invalidate()
                return true
            }
            return false
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            val point = floatArrayOf(e.x, e.y)
            inverseMatrix.mapPoints(point)
            
            val tappedNode = findNodeAt(point[0], point[1])
            if (tappedNode != null) {
                onFowlClickListener?.invoke(tappedNode.fowl)
                return true
            }
            
            return false
        }

        override fun onLongPress(e: MotionEvent) {
            val point = floatArrayOf(e.x, e.y)
            inverseMatrix.mapPoints(point)
            
            val tappedNode = findNodeAt(point[0], point[1])
            if (tappedNode != null) {
                onFowlLongClickListener?.invoke(tappedNode.fowl)
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            val point = floatArrayOf(e.x, e.y)
            inverseMatrix.mapPoints(point)
            
            val tappedNode = findNodeAt(point[0], point[1])
            if (tappedNode != null) {
                centerAndZoomToNode(tappedNode)
                return true
            }
            
            return false
        }
    }

    private fun findNodeAt(x: Float, y: Float): FowlNode? {
        return fowlNodes.find { node ->
            val distance = sqrt((node.x - x).pow(2) + (node.y - y).pow(2))
            distance <= renderer.getNodeRadius()
        }
    }

    /**
     * Center the tree in the view
     */
    fun centerTree() {
        if (fowlNodes.isEmpty()) return
        
        val bounds = layoutManager.getTreeBounds(fowlNodes)
        val centerX = bounds.centerX()
        val centerY = bounds.centerY()
        
        val viewCenterX = width / 2f
        val viewCenterY = height / 2f
        
        viewMatrix.reset()
        viewMatrix.postTranslate(viewCenterX - centerX, viewCenterY - centerY)
        viewMatrix.invert(inverseMatrix)
        
        currentScale = 1f
        invalidate()
    }

    /**
     * Center and zoom to a specific node
     */
    private fun centerAndZoomToNode(node: FowlNode) {
        val targetScale = 1.5f.coerceIn(minScale, maxScale)
        val viewCenterX = width / 2f
        val viewCenterY = height / 2f
        
        // Calculate target transformation
        val targetMatrix = Matrix()
        targetMatrix.postScale(targetScale, targetScale, viewCenterX, viewCenterY)
        targetMatrix.postTranslate(
            viewCenterX - node.x * targetScale,
            viewCenterY - node.y * targetScale
        )
        
        // Animate to target
        animateToMatrix(targetMatrix, targetScale)
    }

    private fun animateToMatrix(targetMatrix: Matrix, targetScale: Float) {
        val startMatrix = Matrix(viewMatrix)
        val startScale = currentScale
        
        zoomAnimator?.cancel()
        zoomAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                
                // Interpolate matrix
                val tempMatrix = Matrix()
                tempMatrix.set(startMatrix)
                tempMatrix.postConcat(targetMatrix)
                
                viewMatrix.set(startMatrix)
                viewMatrix.postScale(
                    1f + progress * (targetScale / startScale - 1f),
                    1f + progress * (targetScale / startScale - 1f),
                    width / 2f,
                    height / 2f
                )
                
                currentScale = startScale + progress * (targetScale - startScale)
                viewMatrix.invert(inverseMatrix)
                invalidate()
            }
            start()
        }
    }

    /**
     * Update tree data
     */
    fun updateTreeData(updatedFowl: List<RoosterEntity>) {
        rootFowl?.let { root ->
            buildFamilyTree(root, updatedFowl)
            invalidate()
        }
    }

    /**
     * Get current zoom level
     */
    fun getCurrentZoom(): Float = currentScale

    /**
     * Set zoom level programmatically
     */
    fun setZoom(scale: Float, animate: Boolean = true) {
        val targetScale = scale.coerceIn(minScale, maxScale)
        if (animate) {
            animateToMatrix(viewMatrix, targetScale)
        } else {
            currentScale = targetScale
            invalidate()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        zoomAnimator?.cancel()
        panAnimator?.cancel()
    }
}

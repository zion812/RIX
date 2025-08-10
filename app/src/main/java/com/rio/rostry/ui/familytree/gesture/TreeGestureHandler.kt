package com.rio.rostry.ui.familytree.gesture

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.animation.DecelerateInterpolator
import com.rio.rostry.ui.familytree.FowlFamilyTreeView
import kotlin.math.*

/**
 * Handles all touch gestures for the fowl family tree view
 * Supports zoom, pan, tap, and long press interactions
 */
class TreeGestureHandler(private val treeView: FowlFamilyTreeView) {
    
    private val context: Context = treeView.context
    
    // Gesture detectors
    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    private val gestureDetector = GestureDetector(context, GestureListener())
    
    // Touch state
    private var isScaling = false
    private var isPanning = false
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var activePointerId = MotionEvent.INVALID_POINTER_ID
    
    // Pan momentum
    private var velocityX = 0f
    private var velocityY = 0f
    private var momentumAnimator: ValueAnimator? = null
    
    // Zoom constraints
    private val minZoom = 0.1f
    private val maxZoom = 3.0f
    private val zoomSensitivity = 1.0f
    
    // Pan constraints
    private var panBounds = android.graphics.RectF()
    private val boundaryElasticity = 0.3f
    
    // Double tap zoom
    private val doubleTapZoomFactor = 2.0f
    private var doubleTapAnimator: ValueAnimator? = null
    
    // Performance optimization
    private val gestureThreshold = 5f // Minimum movement to start gesture
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var hasMovedBeyondThreshold = false
    
    /**
     * Handle touch events
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
        var handled = false
        
        // Handle scale gestures first
        handled = scaleGestureDetector.onTouchEvent(event) || handled
        
        // Handle other gestures if not scaling
        if (!isScaling) {
            handled = gestureDetector.onTouchEvent(event) || handled
            handled = handlePanGesture(event) || handled
        }
        
        // Handle touch end
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handleTouchEnd()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                handlePointerUp(event)
            }
        }
        
        return handled
    }
    
    /**
     * Scale gesture listener
     */
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        
        private var lastScaleFactor = 1f
        
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            isScaling = true
            isPanning = false
            stopMomentumAnimation()
            lastScaleFactor = 1f
            return true
        }
        
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor * zoomSensitivity
            val currentZoom = treeView.getCurrentZoom()
            val newZoom = (currentZoom * scaleFactor).coerceIn(minZoom, maxZoom)
            
            if (newZoom != currentZoom) {
                val focusX = detector.focusX
                val focusY = detector.focusY
                
                // Apply zoom with focal point preservation
                applyZoom(newZoom / currentZoom, focusX, focusY)
                
                // Provide haptic feedback for zoom boundaries
                if (newZoom == minZoom || newZoom == maxZoom) {
                    if (abs(scaleFactor - lastScaleFactor) > 0.1f) {
                        treeView.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    }
                }
                
                lastScaleFactor = scaleFactor
            }
            
            return true
        }
        
        override fun onScaleEnd(detector: ScaleGestureDetector) {
            isScaling = false
            
            // Snap to nice zoom levels
            val currentZoom = treeView.getCurrentZoom()
            val snapZoom = findSnapZoomLevel(currentZoom)
            
            if (abs(currentZoom - snapZoom) > 0.1f) {
                animateZoomTo(snapZoom)
            }
        }
    }
    
    /**
     * General gesture listener
     */
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        
        override fun onDown(e: MotionEvent): Boolean {
            stopMomentumAnimation()
            initialTouchX = e.x
            initialTouchY = e.y
            hasMovedBeyondThreshold = false
            return true
        }
        
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (!hasMovedBeyondThreshold) {
                return handleSingleTap(e.x, e.y)
            }
            return false
        }
        
        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (!hasMovedBeyondThreshold) {
                return handleDoubleTap(e.x, e.y)
            }
            return false
        }
        
        override fun onLongPress(e: MotionEvent) {
            if (!hasMovedBeyondThreshold) {
                handleLongPress(e.x, e.y)
            }
        }
        
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (!isScaling && isPanning) {
                startMomentumAnimation(velocityX, velocityY)
                return true
            }
            return false
        }
    }
    
    /**
     * Handle pan gestures manually for better control
     */
    private fun handlePanGesture(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = event.getPointerId(0)
                lastTouchX = event.x
                lastTouchY = event.y
                return true
            }
            
            MotionEvent.ACTION_MOVE -> {
                if (!isScaling && activePointerId != MotionEvent.INVALID_POINTER_ID) {
                    val pointerIndex = event.findPointerIndex(activePointerId)
                    if (pointerIndex >= 0) {
                        val x = event.getX(pointerIndex)
                        val y = event.getY(pointerIndex)
                        
                        val dx = x - lastTouchX
                        val dy = y - lastTouchY
                        
                        // Check if movement exceeds threshold
                        if (!hasMovedBeyondThreshold) {
                            val totalMovement = sqrt(
                                (x - initialTouchX).pow(2) + (y - initialTouchY).pow(2)
                            )
                            if (totalMovement > gestureThreshold) {
                                hasMovedBeyondThreshold = true
                                isPanning = true
                            }
                        }
                        
                        if (isPanning) {
                            applyPan(dx, dy)
                            velocityX = dx
                            velocityY = dy
                        }
                        
                        lastTouchX = x
                        lastTouchY = y
                        return true
                    }
                }
            }
        }
        return false
    }
    
    private fun handleTouchEnd() {
        isScaling = false
        isPanning = false
        activePointerId = MotionEvent.INVALID_POINTER_ID
        hasMovedBeyondThreshold = false
    }
    
    private fun handlePointerUp(event: MotionEvent) {
        val pointerIndex = event.action and MotionEvent.ACTION_POINTER_INDEX_MASK shr 
                          MotionEvent.ACTION_POINTER_INDEX_SHIFT
        val pointerId = event.getPointerId(pointerIndex)
        
        if (pointerId == activePointerId) {
            // Pick a new active pointer
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            if (newPointerIndex < event.pointerCount) {
                activePointerId = event.getPointerId(newPointerIndex)
                lastTouchX = event.getX(newPointerIndex)
                lastTouchY = event.getY(newPointerIndex)
            } else {
                activePointerId = MotionEvent.INVALID_POINTER_ID
            }
        }
    }
    
    private fun handleSingleTap(x: Float, y: Float): Boolean {
        // Convert screen coordinates to tree coordinates
        val treePoint = screenToTreeCoordinates(x, y)
        
        // Find node at this position
        val tappedNode = findNodeAt(treePoint.x, treePoint.y)
        
        if (tappedNode != null) {
            treeView.onFowlClickListener?.invoke(tappedNode.fowl)
            treeView.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            return true
        }
        
        return false
    }
    
    private fun handleDoubleTap(x: Float, y: Float): Boolean {
        val treePoint = screenToTreeCoordinates(x, y)
        val tappedNode = findNodeAt(treePoint.x, treePoint.y)
        
        if (tappedNode != null) {
            // Zoom to node
            centerAndZoomToNode(tappedNode, x, y)
        } else {
            // Smart zoom
            val currentZoom = treeView.getCurrentZoom()
            val targetZoom = if (currentZoom < 1.0f) 1.0f else 0.5f
            animateZoomTo(targetZoom, x, y)
        }
        
        treeView.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        return true
    }
    
    private fun handleLongPress(x: Float, y: Float) {
        val treePoint = screenToTreeCoordinates(x, y)
        val tappedNode = findNodeAt(treePoint.x, treePoint.y)
        
        if (tappedNode != null) {
            treeView.onFowlLongClickListener?.invoke(tappedNode.fowl)
            treeView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
    
    private fun applyZoom(scaleFactor: Float, focusX: Float, focusY: Float) {
        // Implementation would update the tree view's transformation matrix
        // This is a simplified version - actual implementation would be in FowlFamilyTreeView
        treeView.invalidate()
    }
    
    private fun applyPan(dx: Float, dy: Float) {
        // Apply boundary constraints with elasticity
        val constrainedDx = applyBoundaryElasticity(dx, true)
        val constrainedDy = applyBoundaryElasticity(dy, false)
        
        // Implementation would update the tree view's transformation matrix
        treeView.invalidate()
    }
    
    private fun applyBoundaryElasticity(delta: Float, isHorizontal: Boolean): Float {
        // Apply elastic resistance when approaching boundaries
        // This is a simplified implementation
        return delta * boundaryElasticity
    }
    
    private fun startMomentumAnimation(initialVelocityX: Float, initialVelocityY: Float) {
        stopMomentumAnimation()
        
        val friction = 0.95f
        val minVelocity = 10f
        
        momentumAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000
            interpolator = DecelerateInterpolator()
            
            var currentVelX = initialVelocityX
            var currentVelY = initialVelocityY
            
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                val frameVelX = currentVelX * (1f - progress)
                val frameVelY = currentVelY * (1f - progress)
                
                if (abs(frameVelX) > minVelocity || abs(frameVelY) > minVelocity) {
                    applyPan(frameVelX * 0.016f, frameVelY * 0.016f) // 60fps
                }
                
                currentVelX *= friction
                currentVelY *= friction
            }
            
            start()
        }
    }
    
    private fun stopMomentumAnimation() {
        momentumAnimator?.cancel()
        momentumAnimator = null
    }
    
    private fun animateZoomTo(targetZoom: Float, focusX: Float = treeView.width / 2f, focusY: Float = treeView.height / 2f) {
        doubleTapAnimator?.cancel()
        
        val startZoom = treeView.getCurrentZoom()
        
        doubleTapAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                val currentZoom = startZoom + (targetZoom - startZoom) * progress
                val scaleFactor = currentZoom / treeView.getCurrentZoom()
                
                applyZoom(scaleFactor, focusX, focusY)
            }
            
            start()
        }
    }
    
    private fun findSnapZoomLevel(currentZoom: Float): Float {
        val snapLevels = floatArrayOf(0.25f, 0.5f, 0.75f, 1.0f, 1.5f, 2.0f, 3.0f)
        
        return snapLevels.minByOrNull { abs(it - currentZoom) } ?: currentZoom
    }
    
    private fun centerAndZoomToNode(node: FowlNode, focusX: Float, focusY: Float) {
        // This would center the view on the selected node and zoom to an appropriate level
        animateZoomTo(1.5f, focusX, focusY)
    }
    
    private fun screenToTreeCoordinates(screenX: Float, screenY: Float): PointF {
        // Convert screen coordinates to tree coordinates using inverse transformation
        // This is a placeholder - actual implementation would use the view's transformation matrix
        return PointF(screenX, screenY)
    }
    
    private fun findNodeAt(x: Float, y: Float): FowlNode? {
        // This would find the node at the given tree coordinates
        // Placeholder implementation
        return null
    }
    
    /**
     * Update pan boundaries based on tree content
     */
    fun updatePanBounds(bounds: android.graphics.RectF) {
        panBounds = bounds
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        stopMomentumAnimation()
        doubleTapAnimator?.cancel()
    }
}

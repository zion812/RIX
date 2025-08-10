package com.rio.rostry.ui.familytree.model

import android.graphics.PointF
import android.graphics.RectF
import com.rio.rostry.core.database.entities.RoosterEntity
import java.util.*

/**
 * Represents a fowl node in the family tree visualization
 */
data class FowlNode(
    val fowl: RoosterEntity,
    var x: Float = 0f,
    var y: Float = 0f,
    var generation: Int = 0,
    var isVisible: Boolean = true,
    var isSelected: Boolean = false,
    var isHighlighted: Boolean = false,
    var animationProgress: Float = 1f
) {
    
    // Calculated properties
    val id: String get() = fowl.id
    val name: String get() = fowl.name
    val breed: String get() = fowl.breed
    val gender: String get() = fowl.gender
    val birthDate: Date? get() = fowl.birthDate
    val healthStatus: String get() = fowl.healthStatus
    val isVerified: Boolean get() = fowl.lineageVerified
    val ownerId: String get() = fowl.ownerId
    
    // Visual properties
    var radius: Float = 30f
    var bounds: RectF = RectF()
    
    // Layout properties
    var layoutWeight: Float = 1f
    var hasChildren: Boolean = false
    var hasParents: Boolean = false
    var childrenCount: Int = 0
    var parentsCount: Int = 0
    
    // Animation properties
    var targetX: Float = x
    var targetY: Float = y
    var velocityX: Float = 0f
    var velocityY: Float = 0f
    
    /**
     * Update the node's bounds based on current position and radius
     */
    fun updateBounds() {
        bounds.set(
            x - radius,
            y - radius,
            x + radius,
            y + radius
        )
    }
    
    /**
     * Check if a point is within this node
     */
    fun contains(pointX: Float, pointY: Float): Boolean {
        val dx = pointX - x
        val dy = pointY - y
        return (dx * dx + dy * dy) <= (radius * radius)
    }
    
    /**
     * Get the distance to another node
     */
    fun distanceTo(other: FowlNode): Float {
        val dx = x - other.x
        val dy = y - other.y
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }
    
    /**
     * Get the center point of this node
     */
    fun getCenterPoint(): PointF {
        return PointF(x, y)
    }
    
    /**
     * Get the age of the fowl in months
     */
    fun getAgeInMonths(): Int {
        return birthDate?.let { birth ->
            val now = Calendar.getInstance()
            val birthCal = Calendar.getInstance().apply { time = birth }
            
            val years = now.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
            val months = now.get(Calendar.MONTH) - birthCal.get(Calendar.MONTH)
            
            years * 12 + months
        } ?: 0
    }
    
    /**
     * Get formatted age string
     */
    fun getFormattedAge(): String {
        val ageMonths = getAgeInMonths()
        return when {
            ageMonths < 12 -> "${ageMonths}m"
            ageMonths < 24 -> "1y ${ageMonths % 12}m"
            else -> "${ageMonths / 12}y"
        }
    }
    
    /**
     * Get health status color
     */
    fun getHealthStatusColor(): Int {
        return when (healthStatus.lowercase()) {
            "excellent" -> 0xFF4CAF50.toInt() // Green
            "good" -> 0xFF8BC34A.toInt()      // Light Green
            "fair" -> 0xFFFFC107.toInt()      // Amber
            "poor" -> 0xFFFF9800.toInt()      // Orange
            "critical" -> 0xFFF44336.toInt()  // Red
            else -> 0xFF9E9E9E.toInt()        // Grey
        }
    }
    
    /**
     * Get gender color
     */
    fun getGenderColor(): Int {
        return when (gender.lowercase()) {
            "male", "rooster" -> 0xFF2196F3.toInt()  // Blue
            "female", "hen" -> 0xFFE91E63.toInt()    // Pink
            else -> 0xFF9E9E9E.toInt()               // Grey
        }
    }
    
    /**
     * Check if this fowl is breeding age
     */
    fun isBreedingAge(): Boolean {
        val ageMonths = getAgeInMonths()
        return ageMonths >= 6 && ageMonths <= 60 // 6 months to 5 years
    }
    
    /**
     * Get breeding status indicator
     */
    fun getBreedingStatus(): BreedingStatus {
        return when {
            !isBreedingAge() -> BreedingStatus.TOO_YOUNG_OR_OLD
            healthStatus.lowercase() in listOf("poor", "critical") -> BreedingStatus.HEALTH_ISSUES
            isBreedingAge() -> BreedingStatus.READY
            else -> BreedingStatus.UNKNOWN
        }
    }
    
    /**
     * Get verification status
     */
    fun getVerificationLevel(): VerificationLevel {
        return when {
            fowl.lineageVerified && fowl.healthCertified -> VerificationLevel.FULLY_VERIFIED
            fowl.lineageVerified -> VerificationLevel.LINEAGE_VERIFIED
            fowl.healthCertified -> VerificationLevel.HEALTH_VERIFIED
            else -> VerificationLevel.UNVERIFIED
        }
    }
    
    /**
     * Check if this node should be rendered at current zoom level
     */
    fun shouldRender(zoomLevel: Float, visibleBounds: RectF): Boolean {
        if (!isVisible) return false
        
        // Level of detail based on zoom
        if (zoomLevel < 0.3f && generation > 2) return false
        if (zoomLevel < 0.5f && generation > 3) return false
        
        // Frustum culling
        return bounds.intersects(
            visibleBounds.left - radius,
            visibleBounds.top - radius,
            visibleBounds.right + radius,
            visibleBounds.bottom + radius
        )
    }
    
    /**
     * Get display priority for rendering order
     */
    fun getDisplayPriority(): Int {
        var priority = 0
        
        // Higher priority for selected/highlighted nodes
        if (isSelected) priority += 1000
        if (isHighlighted) priority += 500
        
        // Higher priority for verified fowl
        if (isVerified) priority += 100
        
        // Higher priority for breeding age fowl
        if (isBreedingAge()) priority += 50
        
        // Lower generation = higher priority
        priority += (10 - generation) * 10
        
        return priority
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FowlNode) return false
        return id == other.id
    }
    
    override fun hashCode(): Int {
        return id.hashCode()
    }
}

/**
 * Breeding status enumeration
 */
enum class BreedingStatus {
    READY,
    TOO_YOUNG_OR_OLD,
    HEALTH_ISSUES,
    UNKNOWN
}

/**
 * Verification level enumeration
 */
enum class VerificationLevel {
    FULLY_VERIFIED,
    LINEAGE_VERIFIED,
    HEALTH_VERIFIED,
    UNVERIFIED
}

/**
 * Tree connection between fowl nodes
 */
data class TreeConnection(
    val parent: FowlNode,
    val child: FowlNode,
    val type: Type,
    val isVerified: Boolean = false,
    val strength: Float = 1f,
    var isVisible: Boolean = true,
    var isHighlighted: Boolean = false
) {
    
    enum class Type {
        PATERNAL,    // Father to child
        MATERNAL,    // Mother to child
        BREEDING,    // Breeding pair connection
        SIBLING      // Sibling relationship
    }
    
    /**
     * Get connection color based on type and verification
     */
    fun getConnectionColor(): Int {
        return when {
            !isVerified -> 0xFF9E9E9E.toInt()  // Grey for unverified
            type == Type.PATERNAL -> 0xFF2196F3.toInt()  // Blue for paternal
            type == Type.MATERNAL -> 0xFFE91E63.toInt()  // Pink for maternal
            type == Type.BREEDING -> 0xFFFF6B35.toInt()  // RIO orange for breeding
            type == Type.SIBLING -> 0xFF9C27B0.toInt()   // Purple for siblings
            else -> 0xFF757575.toInt()  // Dark grey default
        }
    }
    
    /**
     * Get connection stroke width
     */
    fun getStrokeWidth(baseWidth: Float): Float {
        var width = baseWidth
        if (isHighlighted) width *= 1.5f
        if (isVerified) width *= 1.2f
        return width * strength
    }
    
    /**
     * Check if connection should be rendered
     */
    fun shouldRender(zoomLevel: Float): Boolean {
        if (!isVisible) return false
        if (!parent.isVisible || !child.isVisible) return false
        
        // Hide some connections at low zoom levels
        if (zoomLevel < 0.2f && type == Type.SIBLING) return false
        
        return true
    }
    
    /**
     * Get path effect for unverified connections
     */
    fun isDashed(): Boolean {
        return !isVerified
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TreeConnection) return false
        return parent.id == other.parent.id && 
               child.id == other.child.id && 
               type == other.type
    }
    
    override fun hashCode(): Int {
        return "${parent.id}_${child.id}_$type".hashCode()
    }
}

/**
 * Tree statistics data class
 */
data class TreeStatistics(
    val totalFowl: Int = 0,
    val totalGenerations: Int = 0,
    val verifiedLineages: Int = 0,
    val breedingAgeFowl: Int = 0,
    val generationDistribution: Map<Int, Int> = emptyMap(),
    val healthDistribution: Map<String, Int> = emptyMap(),
    val breedDistribution: Map<String, Int> = emptyMap()
)

/**
 * Tree settings data class
 */
data class TreeSettings(
    val showHealthStatus: Boolean = true,
    val showVerificationBadges: Boolean = true,
    val showGenerationLabels: Boolean = true,
    val showBreedingConnections: Boolean = true,
    val nodeSize: NodeSize = NodeSize.MEDIUM,
    val connectionStyle: ConnectionStyle = ConnectionStyle.CURVED,
    val colorScheme: ColorScheme = ColorScheme.DEFAULT
)

enum class NodeSize(val scale: Float) {
    SMALL(0.8f),
    MEDIUM(1.0f),
    LARGE(1.2f)
}

enum class ConnectionStyle {
    STRAIGHT,
    CURVED,
    ORTHOGONAL
}

enum class ColorScheme {
    DEFAULT,
    HIGH_CONTRAST,
    COLORBLIND_FRIENDLY
}

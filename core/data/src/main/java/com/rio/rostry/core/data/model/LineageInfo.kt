package com.rio.rostry.core.data.model

/**
 * Data model representing a fowl in a lineage visualization
 */
data class LineageFowl(
    val id: String,
    val name: String?,
    val breedPrimary: String,
    val gender: String?, // MALE, FEMALE, UNKNOWN
    val photoReference: String?,
    val isDeceased: Boolean = false
)

/**
 * Data model representing a relationship between fowls
 */
data class LineageRelationship(
    val parentId: String,
    val childId: String,
    val relationshipType: String // FATHER, MOTHER
)

/**
 * Data model representing the complete lineage information for visualization
 */
data class LineageInfo(
    val subjectFowl: LineageFowl, // The fowl we're viewing lineage for
    val parents: List<LineageFowl>, // The subject's parents
    val children: List<LineageFowl>, // The subject's children
    val relationships: List<LineageRelationship> // All relationships in the visualization
)
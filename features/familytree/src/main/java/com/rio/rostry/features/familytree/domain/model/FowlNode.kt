package com.rio.rostry.features.familytree.domain.model

data class FowlNode(
    val id: String,
    val name: String,
    val breedType: String,
    val age: Int,
    val parents: List<String>, // Parent IDs
    val children: List<String>, // Child IDs
    val verificationStatus: String,
    val ownerInfo: OwnerInfo,
    val metadata: FowlMetadata
)

data class OwnerInfo(
    val id: String,
    val name: String,
    val farmName: String?,
    val verificationLevel: String
)

data class FowlMetadata(
    val createdAt: Long,
    val lastModified: Long,
    val verifiedAt: Long?,
    val verifiedBy: String?,
    val generation: Int,
    val lineageScore: Double // 0.0 to 1.0, indicating lineage verification confidence
)

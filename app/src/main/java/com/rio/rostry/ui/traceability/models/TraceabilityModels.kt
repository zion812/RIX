package com.rio.rostry.ui.traceability.models

import java.util.Date

data class FowlTraceability(
    val id: String,
    val name: String,
    val breed: String,
    val gender: String,
    val dateOfBirth: Date,
    val ageInMonths: Int,
    val weight: Double,
    val color: String,
    val registryId: String,
    val sire: FowlLineage? = null,
    val dam: FowlLineage? = null,
    val generation: Int,
    val bloodline: String,
    val certifications: List<Certification> = emptyList()
)

data class FowlLineage(
    val id: String,
    val name: String,
    val registryId: String,
    val breed: String
)

data class FamilyTree(
    val currentFowl: FowlLineage,
    val sire: FowlLineage? = null,
    val dam: FowlLineage? = null,
    val paternalGrandfather: FowlLineage? = null,
    val paternalGrandmother: FowlLineage? = null,
    val maternalGrandfather: FowlLineage? = null,
    val maternalGrandmother: FowlLineage? = null
)

data class BreedingRecord(
    val id: String,
    val date: Date,
    val partnerName: String,
    val method: String,
    val successRate: Int,
    val offspringCount: Int,
    val notes: String = ""
)

data class HealthRecord(
    val id: String,
    val date: Date,
    val type: HealthRecordType,
    val description: String,
    val veterinarian: String? = null,
    val medication: String? = null,
    val notes: String = ""
)

enum class HealthRecordType {
    VACCINATION,
    TREATMENT,
    CHECKUP,
    ILLNESS,
    INJURY
}

data class Certification(
    val id: String,
    val name: String,
    val issuedBy: String,
    val dateIssued: Date,
    val expiryDate: Date? = null,
    val certificateNumber: String
)
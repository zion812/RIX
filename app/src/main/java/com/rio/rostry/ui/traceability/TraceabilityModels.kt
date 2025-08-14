package com.rio.rostry.ui.traceability

import java.util.*

/**
 * Data models for traceability and family tree functionality
 */

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
    val sire: FowlLineage?,
    val dam: FowlLineage?,
    val generation: Int,
    val bloodline: String,
    val certifications: List<Certification>
)

data class FowlLineage(
    val name: String,
    val registryId: String,
    val breed: String = "",
    val dateOfBirth: Date? = null
)

data class FamilyTree(
    val currentFowl: FowlLineage,
    val sire: FowlLineage?,
    val dam: FowlLineage?,
    val paternalGrandfather: FowlLineage?,
    val paternalGrandmother: FowlLineage?,
    val maternalGrandfather: FowlLineage?,
    val maternalGrandmother: FowlLineage?
)

data class Certification(
    val id: String,
    val name: String,
    val issuedBy: String,
    val dateIssued: Date,
    val expiryDate: Date?,
    val certificateNumber: String,
    val description: String = ""
)

data class BreedingRecord(
    val id: String,
    val date: Date,
    val partnerName: String,
    val partnerRegistryId: String,
    val method: String, // Natural, Artificial Insemination, etc.
    val successRate: Int, // Percentage
    val offspringCount: Int,
    val notes: String = ""
)

data class HealthRecord(
    val id: String,
    val date: Date,
    val type: String, // Vaccination, Treatment, Checkup
    val veterinarian: String,
    val treatment: String,
    val nextDueDate: Date?,
    val notes: String = ""
)
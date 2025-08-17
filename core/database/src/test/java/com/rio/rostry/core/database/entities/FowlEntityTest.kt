package com.rio.rostry.core.database.entities

import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FowlEntityTest {

    @Test
    fun `fowl entity creation with all fields`() {
        val id = UUID.randomUUID().toString()
        val ownerId = UUID.randomUUID().toString()
        val motherId = UUID.randomUUID().toString()
        val fatherId = UUID.randomUUID().toString()
        val dob = Date()
        val createdAt = Date()
        val updatedAt = Date()
        val createdBy = UUID.randomUUID().toString()
        val updatedBy = UUID.randomUUID().toString()
        val traits = mapOf("temperament" to "calm", "eggColor" to "brown")
        val siblings = listOf(UUID.randomUUID().toString(), UUID.randomUUID().toString())
        val coverThumbnailUrl = "https://example.com/thumbnail.jpg"

        val fowl = FowlEntity(
            id = id,
            ownerId = ownerId,
            name = "Test Fowl",
            motherId = motherId,
            fatherId = fatherId,
            dob = dob,
            breederStatus = "ELIGIBLE",
            status = "AVAILABLE",
            gender = "MALE",
            color = "Black",
            breedPrimary = "Rhode Island Red",
            breedSecondary = "Plymouth Rock",
            generation = 2,
            healthStatus = "GOOD",
            availabilityStatus = "AVAILABLE",
            region = "Telangana",
            district = "Hyderabad",
            inbreedingCoefficient = 0.125,
            totalOffspring = 5,
            fightingWins = 3,
            fightingLosses = 1,
            showWins = 2,
            traits = traits,
            siblings = siblings,
            notes = "This is a test fowl",
            coverThumbnailUrl = coverThumbnailUrl,
            createdAt = createdAt,
            updatedAt = updatedAt,
            createdBy = createdBy,
            updatedBy = updatedBy,
            version = 1
        )

        assertEquals(id, fowl.id)
        assertEquals(ownerId, fowl.ownerId)
        assertEquals("Test Fowl", fowl.name)
        assertEquals(motherId, fowl.motherId)
        assertEquals(fatherId, fowl.fatherId)
        assertEquals(dob, fowl.dob)
        assertEquals("ELIGIBLE", fowl.breederStatus)
        assertEquals("AVAILABLE", fowl.status)
        assertEquals("MALE", fowl.gender)
        assertEquals("Black", fowl.color)
        assertEquals("Rhode Island Red", fowl.breedPrimary)
        assertEquals("Plymouth Rock", fowl.breedSecondary)
        assertEquals(2, fowl.generation)
        assertEquals("GOOD", fowl.healthStatus)
        assertEquals("AVAILABLE", fowl.availabilityStatus)
        assertEquals("Telangana", fowl.region)
        assertEquals("Hyderabad", fowl.district)
        assertEquals(0.125, fowl.inbreedingCoefficient)
        assertEquals(5, fowl.totalOffspring)
        assertEquals(3, fowl.fightingWins)
        assertEquals(1, fowl.fightingLosses)
        assertEquals(2, fowl.showWins)
        assertEquals(traits, fowl.traits)
        assertEquals(siblings, fowl.siblings)
        assertEquals("This is a test fowl", fowl.notes)
        assertEquals(coverThumbnailUrl, fowl.coverThumbnailUrl)
        assertEquals(createdAt, fowl.createdAt)
        assertEquals(updatedAt, fowl.updatedAt)
        assertEquals(createdBy, fowl.createdBy)
        assertEquals(updatedBy, fowl.updatedBy)
        assertEquals(1, fowl.version)
    }

    @Test
    fun `fowl entity creation with minimal fields`() {
        val id = UUID.randomUUID().toString()
        val ownerId = UUID.randomUUID().toString()
        val dob = Date()
        val createdAt = Date()
        val updatedAt = Date()
        val createdBy = UUID.randomUUID().toString()
        val updatedBy = UUID.randomUUID().toString()

        val fowl = FowlEntity(
            id = id,
            ownerId = ownerId,
            dob = dob,
            gender = "FEMALE",
            color = "White",
            breedPrimary = "Leghorn",
            region = "Andhra Pradesh",
            district = "Visakhapatnam",
            createdAt = createdAt,
            updatedAt = updatedAt,
            createdBy = createdBy,
            updatedBy = updatedBy
        )

        assertEquals(id, fowl.id)
        assertEquals(ownerId, fowl.ownerId)
        assertNull(fowl.name)
        assertNull(fowl.motherId)
        assertNull(fowl.fatherId)
        assertEquals(dob, fowl.dob)
        assertEquals("INELIGIBLE", fowl.breederStatus)
        assertEquals("AVAILABLE", fowl.status)
        assertEquals("FEMALE", fowl.gender)
        assertEquals("White", fowl.color)
        assertEquals("Leghorn", fowl.breedPrimary)
        assertNull(fowl.breedSecondary)
        assertEquals(0, fowl.generation)
        assertEquals("GOOD", fowl.healthStatus)
        assertEquals("AVAILABLE", fowl.availabilityStatus)
        assertEquals("Andhra Pradesh", fowl.region)
        assertEquals("Visakhapatnam", fowl.district)
        assertNull(fowl.inbreedingCoefficient)
        assertEquals(0, fowl.totalOffspring)
        assertEquals(0, fowl.fightingWins)
        assertEquals(0, fowl.fightingLosses)
        assertEquals(0, fowl.showWins)
        assertEquals(emptyMap(), fowl.traits)
        assertEquals(emptyList(), fowl.siblings)
        assertNull(fowl.notes)
        assertNull(fowl.coverThumbnailUrl)
        assertEquals(createdAt, fowl.createdAt)
        assertEquals(updatedAt, fowl.updatedAt)
        assertEquals(createdBy, fowl.createdBy)
        assertEquals(updatedBy, fowl.updatedBy)
        assertEquals(1, fowl.version)
    }

    @Test
    fun `fowl entity is not null`() {
        val id = UUID.randomUUID().toString()
        val ownerId = UUID.randomUUID().toString()
        val dob = Date()
        val createdAt = Date()
        val updatedAt = Date()
        val createdBy = UUID.randomUUID().toString()
        val updatedBy = UUID.randomUUID().toString()

        val fowl = FowlEntity(
            id = id,
            ownerId = ownerId,
            dob = dob,
            gender = "MALE",
            color = "Brown",
            breedPrimary = "Plymouth Rock",
            region = "Karnataka",
            district = "Bangalore",
            createdAt = createdAt,
            updatedAt = updatedAt,
            createdBy = createdBy,
            updatedBy = updatedBy
        )

        assertNotNull(fowl)
    }
}
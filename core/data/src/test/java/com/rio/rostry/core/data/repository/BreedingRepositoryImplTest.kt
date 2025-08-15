package com.rio.rostry.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rostry.core.data.service.UserValidationService
import com.rio.rostry.core.data.service.ValidationResult
import com.rio.rostry.core.database.dao.BreedingDao
import com.rio.rostry.core.database.dao.FowlDao
import com.rio.rostry.core.database.entities.FowlEntity
import com.rio.rostry.core.network.NetworkStateManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.util.*

@ExperimentalCoroutinesApi
class BreedingRepositoryImplTest {

    private lateinit var breedingRepository: BreedingRepositoryImpl
    private val fowlDao: FowlDao = mock()
    private val breedingDao: BreedingDao = mock()
    private val userValidationService: UserValidationService = mock()
    private val firestore: FirebaseFirestore = mock()
    private val networkStateManager: NetworkStateManager = mock()

    @Before
    fun setUp() {
        breedingRepository = BreedingRepositoryImpl(
            fowlDao,
            breedingDao,
            userValidationService,
            firestore,
            networkStateManager
        )
        // Assume network is connected for simplicity in most tests
        whenever(networkStateManager.isConnected()).thenReturn(true)
    }

    private fun createTestFowl(id: String, ownerId: String) = FowlEntity(
        id = id,
        ownerId = ownerId,
        breedPrimary = "Test Breed",
        healthStatus = "GOOD",
        availabilityStatus = "AVAILABLE",
        region = "Test Region",
        district = "Test District",
        createdAt = Date()
    )

    @Test
    fun `recordBreeding success when valid returns success`() = runTest {
        // Arrange
        val breederId = "breeder1"
        val male = createTestFowl("male1", breederId)
        val female = createTestFowl("female1", breederId)
        whenever(userValidationService.validateUserExists(breederId)).thenReturn(ValidationResult(true))
        whenever(fowlDao.getById("male1")).thenReturn(male)
        whenever(fowlDao.getById("female1")).thenReturn(female)

        // Act
        val result = breedingRepository.recordBreeding("male1", "female1", breederId)

        // Assert
        assert(result.isSuccess)
        verify(breedingDao).insert(any())
    }

    @Test
    fun `recordBreeding failure when breeder does not own fowls`() = runTest {
        // Arrange
        val breederId = "breeder1"
        val male = createTestFowl("male1", "anotherOwner") // Different owner
        val female = createTestFowl("female1", breederId)
        whenever(userValidationService.validateUserExists(breederId)).thenReturn(ValidationResult(true))
        whenever(fowlDao.getById("male1")).thenReturn(male)
        whenever(fowlDao.getById("female1")).thenReturn(female)

        // Act
        val result = breedingRepository.recordBreeding("male1", "female1", breederId)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message == "Breeder must own both parent fowls.")
    }

    @Test
    fun `registerOffspring creates fowl with correct parent IDs`() = runTest {
        // Arrange
        val breedingRecordId = "breeding1"
        val breederId = "breeder1"
        val maleId = "male1"
        val femaleId = "female1"
        val breedingRecord = com.rio.rostry.core.database.entities.BreedingRecordEntity(
            id = breedingRecordId,
            maleId = maleId,
            femaleId = femaleId,
            breederId = breederId,
            breedingDate = Date(),
            expectedHatchDate = null,
            status = "ACTIVE"
        )
        whenever(breedingDao.getById(breedingRecordId)).thenReturn(breedingRecord)

        val offspringData = mapOf(
            "name" to "Junior",
            "breed" to "Test Breed",
            "region" to "Test Region",
            "district" to "Test District"
        )

        // Act
        val result = breedingRepository.registerOffspring(breedingRecordId, offspringData)

        // Assert
        assert(result.isSuccess)
        val fowlCaptor = argumentCaptor<FowlEntity>()
        verify(fowlDao).insert(fowlCaptor.capture())

        val capturedFowl = fowlCaptor.firstValue
        assert(capturedFowl.name == "Junior")
        assert(capturedFowl.ownerId == breederId)
        assert(capturedFowl.parentMaleId == maleId)
        assert(capturedFowl.parentFemaleId == femaleId)
    }
}

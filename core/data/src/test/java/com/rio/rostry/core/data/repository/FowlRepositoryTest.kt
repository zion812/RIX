package com.rio.rostry.core.data.repository

import com.rio.rostry.core.database.dao.FowlDaoV2
import com.rio.rostry.core.database.dao.OutboxDao
import com.rio.rostry.core.database.entities.FowlEntity
import com.rio.rostry.core.database.entities.OutboxEntity
import com.rio.rostry.core.common.model.Result
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.junit.Assert.*
import java.util.*

class FowlRepositoryTest {

    @Mock
    private lateinit var fowlDao: FowlDaoV2

    @Mock
    private lateinit var outboxDao: OutboxDao

    private lateinit var fowlRepository: FowlRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        fowlRepository = FowlRepository(fowlDao, outboxDao)
    }

    @Test
    fun `getFowlById should return success when fowl exists`() = runBlocking {
        // Given
        val fowlId = "fowl123"
        val fowl = FowlEntity(
            id = fowlId,
            ownerId = "user456",
            breedPrimary = "Rhode Island Red",
            healthStatus = "GOOD",
            availabilityStatus = "AVAILABLE",
            createdAt = Date(),
            updatedAt = Date()
        )

        `when`(fowlDao.getFowlById(fowlId)).thenReturn(fowl)

        // When
        val result = fowlRepository.getFowlById(fowlId)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(fowl, (result as Result.Success).data)
        verify(fowlDao).getFowlById(fowlId)
    }

    @Test
    fun `insertFowl should return success when fowl is inserted`() = runBlocking {
        // Given
        val fowl = FowlEntity(
            id = "fowl123",
            ownerId = "user456",
            breedPrimary = "Rhode Island Red",
            healthStatus = "GOOD",
            availabilityStatus = "AVAILABLE",
            createdAt = Date(),
            updatedAt = Date()
        )

        `when`(fowlDao.insertFowl(any())).thenReturn(1L)
        `when`(outboxDao.insert(any())).thenReturn(1L)

        // When
        val result = fowlRepository.insertFowl(fowl)

        // Then
        assertTrue(result is Result.Success)
        assertNotNull((result as Result.Success).data)
        verify(fowlDao).insertFowl(fowl)
        verify(outboxDao).insert(any<OutboxEntity>())
    }

    @Test
    fun `updateFowl should return success when fowl is updated`() = runBlocking {
        // Given
        val fowl = FowlEntity(
            id = "fowl123",
            ownerId = "user456",
            breedPrimary = "Rhode Island Red",
            healthStatus = "GOOD",
            availabilityStatus = "AVAILABLE",
            createdAt = Date(),
            updatedAt = Date()
        )

        `when`(fowlDao.updateFowl(any())).thenReturn(1)
        `when`(outboxDao.insert(any())).thenReturn(1L)

        // When
        val result = fowlRepository.updateFowl(fowl)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(true, (result as Result.Success).data)
        verify(fowlDao).updateFowl(fowl)
        verify(outboxDao).insert(any<OutboxEntity>())
    }

    @Test
    fun `deleteFowl should return success when fowl is deleted`() = runBlocking {
        // Given
        val fowlId = "fowl123"

        `when`(fowlDao.deleteFowlById(fowlId)).thenReturn(1)
        `when`(outboxDao.insert(any())).thenReturn(1L)

        // When
        val result = fowlRepository.deleteFowl(fowlId)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(true, (result as Result.Success).data)
        verify(fowlDao).deleteFowlById(fowlId)
        verify(outboxDao).insert(any<OutboxEntity>())
    }
}
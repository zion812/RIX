package com.rio.rostry.core.data.usecase

import com.rio.rostry.core.data.repository.TransferRepository
import com.rio.rostry.core.common.model.Result
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.junit.Assert.*
import java.util.*

class InitiateTransferUseCaseTest {

    @Mock
    private lateinit var transferRepository: TransferRepository

    private lateinit var initiateTransferUseCase: InitiateTransferUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        initiateTransferUseCase = InitiateTransferUseCase(transferRepository)
    }

    @Test
    fun `invoke should return success when transfer initiation succeeds`() = runBlocking {
        // Given
        val fowlId = "fowl123"
        val fromUserId = "user456"
        val toUserId = "user789"
        val transferData = TransferInitiationData(
            price = 100,
            color = "brown",
            ageInWeeks = 10,
            weightInGrams = 2000,
            photoReference = "photo999"
        )
        val transferId = "transfer123"

        `when`(transferRepository.initiateTransfer(org.mockito.ArgumentMatchers.any()))
            .thenReturn(Result.Success(transferId))

        // When
        val result = initiateTransferUseCase(fowlId, fromUserId, toUserId, transferData)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(transferId, (result as Result.Success).data)
    }

    @Test
    fun `invoke should return error when transfer initiation fails`() = runBlocking {
        // Given
        val fowlId = "fowl123"
        val fromUserId = "user456"
        val toUserId = "user789"
        val transferData = TransferInitiationData(
            price = 100,
            color = "brown",
            ageInWeeks = 10,
            weightInGrams = 2000,
            photoReference = "photo999"
        )
        val exception = Exception("Initiation failed")

        `when`(transferRepository.initiateTransfer(org.mockito.ArgumentMatchers.any()))
            .thenReturn(Result.Error(exception))

        // When
        val result = initiateTransferUseCase(fowlId, fromUserId, toUserId, transferData)

        // Then
        assertTrue(result is Result.Error)
        assertEquals(exception, (result as Result.Error).exception)
    }
}
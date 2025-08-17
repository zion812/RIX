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

class VerifyTransferUseCaseTest {

    @Mock
    private lateinit var transferRepository: TransferRepository

    private lateinit var verifyTransferUseCase: VerifyTransferUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        verifyTransferUseCase = VerifyTransferUseCase(transferRepository)
    }

    @Test
    fun `invoke should return success when transfer verification succeeds`() = runBlocking {
        // Given
        val transferId = "transfer123"
        val verifierId = "user456"
        val verificationData = TransferVerificationData(
            price = 100,
            color = "brown",
            ageInWeeks = 10,
            weightInGrams = 2000,
            photoReference = "photo789"
        )

        `when`(transferRepository.verifyTransfer(transferId, verifierId, verificationData))
            .thenReturn(Result.Success(true))

        // When
        val result = verifyTransferUseCase(transferId, verifierId, verificationData)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(true, (result as Result.Success).data)
    }

    @Test
    fun `invoke should return error when transfer verification fails`() = runBlocking {
        // Given
        val transferId = "transfer123"
        val verifierId = "user456"
        val verificationData = TransferVerificationData(
            price = 100,
            color = "brown",
            ageInWeeks = 10,
            weightInGrams = 2000,
            photoReference = "photo789"
        )
        val exception = Exception("Verification failed")

        `when`(transferRepository.verifyTransfer(transferId, verifierId, verificationData))
            .thenReturn(Result.Error(exception))

        // When
        val result = verifyTransferUseCase(transferId, verifierId, verificationData)

        // Then
        assertTrue(result is Result.Error)
        assertEquals(exception, (result as Result.Error).exception)
    }
}
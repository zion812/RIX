package com.rio.rostry.core.data.workflow

import com.rio.rostry.core.data.usecase.*
import com.rio.rostry.core.data.repository.TransferRepository
import com.rio.rostry.core.database.entities.TransferLogDao
import com.rio.rostry.core.database.dao.OutboxDao
import com.rio.rostry.core.database.entities.TransferLogEntity
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

class TransferWorkflowTest {

    @Mock
    private lateinit var transferLogDao: TransferLogDao

    @Mock
    private lateinit var outboxDao: OutboxDao

    private lateinit var transferRepository: TransferRepository
    private lateinit var initiateTransferUseCase: InitiateTransferUseCase
    private lateinit var verifyTransferUseCase: VerifyTransferUseCase
    private lateinit var rejectTransferUseCase: RejectTransferUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        transferRepository = TransferRepository(transferLogDao, outboxDao)
        initiateTransferUseCase = InitiateTransferUseCase(transferRepository)
        verifyTransferUseCase = VerifyTransferUseCase(transferRepository)
        rejectTransferUseCase = RejectTransferUseCase(transferRepository)
    }

    @Test
    fun `transfer workflow should work from initiation to verification`() = runBlocking {
        // Given
        val fowlId = "fowl123"
        val fromUserId = "user456"
        val toUserId = "user789"
        val now = Date()

        val transferInitiationData = TransferInitiationData(
            price = 150,
            color = "brown",
            ageInWeeks = 12,
            weightInGrams = 2100,
            photoReference = "photo_12345"
        )

        val transferVerificationData = TransferVerificationData(
            price = 150,
            color = "brown",
            ageInWeeks = 12,
            weightInGrams = 2100,
            photoReference = "photo_67890"
        )

        // Mock the DAO responses
        `when`(transferLogDao.insert(any())).thenReturn(1L)
        `when`(outboxDao.insert(any())).thenReturn(1L)

        // When - Initiate transfer
        val initiateResult = initiateTransferUseCase(
            fowlId = fowlId,
            fromUserId = fromUserId,
            toUserId = toUserId,
            transferData = transferInitiationData
        )

        // Then - Initiate transfer succeeded
        assertTrue(initiateResult is Result.Success)
        val transferId = (initiateResult as Result.Success).data
        assertNotNull(transferId)
        verify(transferLogDao).insert(any<TransferLogEntity>())
        verify(outboxDao).insert(any<OutboxEntity>())

        // Given - For verification
        val transferLog = TransferLogEntity(
            id = transferId,
            fowlId = fowlId,
            fromUserId = fromUserId,
            toUserId = toUserId,
            transferStatus = "PENDING",
            verificationRequired = true,
            verificationStatus = "PENDING",
            expectedPrice = 150.0,
            expectedColor = "brown",
            expectedAgeWeeks = 12,
            expectedWeightGrams = 2100,
            photoReference = "photo_12345",
            initiatedAt = now,
            updatedAt = now,
            createdAt = now
        )

        `when`(transferLogDao.getById(transferId)).thenReturn(transferLog)
        `when`(transferLogDao.updateVerificationStatus(any(), any(), any(), any(), any(), any())).thenReturn(1)

        // When - Verify transfer
        val verifyResult = verifyTransferUseCase(
            transferId = transferId,
            verifierId = toUserId,
            verificationData = transferVerificationData
        )

        // Then - Verify transfer succeeded
        assertTrue(verifyResult is Result.Success)
        assertEquals(true, (verifyResult as Result.Success).data)
        verify(transferLogDao).getById(transferId)
        verify(transferLogDao).updateVerificationStatus(any(), any(), any(), any(), any(), any())
        verify(outboxDao, times(2)).insert(any<OutboxEntity>())
    }

    @Test
    fun `transfer workflow should work from initiation to rejection`() = runBlocking {
        // Given
        val fowlId = "fowl123"
        val fromUserId = "user456"
        val toUserId = "user789"
        val now = Date()

        val transferInitiationData = TransferInitiationData(
            price = 150,
            color = "brown",
            ageInWeeks = 12,
            weightInGrams = 2100,
            photoReference = "photo_12345"
        )

        // Mock the DAO responses
        `when`(transferLogDao.insert(any())).thenReturn(1L)
        `when`(outboxDao.insert(any())).thenReturn(1L)

        // When - Initiate transfer
        val initiateResult = initiateTransferUseCase(
            fowlId = fowlId,
            fromUserId = fromUserId,
            toUserId = toUserId,
            transferData = transferInitiationData
        )

        // Then - Initiate transfer succeeded
        assertTrue(initiateResult is Result.Success)
        val transferId = (initiateResult as Result.Success).data
        assertNotNull(transferId)
        verify(transferLogDao).insert(any<TransferLogEntity>())
        verify(outboxDao).insert(any<OutboxEntity>())

        // Given - For rejection
        val transferLog = TransferLogEntity(
            id = transferId,
            fowlId = fowlId,
            fromUserId = fromUserId,
            toUserId = toUserId,
            transferStatus = "PENDING",
            verificationRequired = true,
            verificationStatus = "PENDING",
            expectedPrice = 150.0,
            expectedColor = "brown",
            expectedAgeWeeks = 12,
            expectedWeightGrams = 2100,
            photoReference = "photo_12345",
            initiatedAt = now,
            updatedAt = now,
            createdAt = now
        )

        `when`(transferLogDao.getById(transferId)).thenReturn(transferLog)
        `when`(transferLogDao.updateVerificationStatus(any(), any(), any(), any(), any(), any())).thenReturn(1)

        // When - Reject transfer
        val rejectResult = rejectTransferUseCase(
            transferId = transferId,
            rejectorId = toUserId,
            reason = "Fowl doesn't match description"
        )

        // Then - Reject transfer succeeded
        assertTrue(rejectResult is Result.Success)
        assertEquals(true, (rejectResult as Result.Success).data)
        verify(transferLogDao).getById(transferId)
        verify(transferLogDao).updateVerificationStatus(any(), any(), any(), any(), any(), any())
        verify(outboxDao, times(2)).insert(any<OutboxEntity>())
    }
}
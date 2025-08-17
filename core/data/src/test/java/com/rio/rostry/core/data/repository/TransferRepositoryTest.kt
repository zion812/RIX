package com.rio.rostry.core.data.repository

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import com.rio.rostry.core.data.model.Transfer
import com.rio.rostry.core.data.model.TransferStatus
import junit.framework.TestCase.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class TransferRepositoryTest {

    @Mock
    private lateinit var mockFirestore: FirebaseFirestore

    @Mock
    private lateinit var mockCollection: CollectionReference

    @Mock
    private lateinit var mockDocument: DocumentReference

    @Mock
    private lateinit var mockDocumentSnapshot: DocumentSnapshot

    @Mock
    private lateinit var mockTask: Task<Void>

    @Mock
    private lateinit var mockGetTask: Task<DocumentSnapshot>

    private lateinit var transferRepository: TransferRepository
    private lateinit var testTransfer: Transfer

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        transferRepository = TransferRepository(mockFirestore)
        
        testTransfer = Transfer(
            id = "test-transfer-id",
            fowlId = "test-fowl-id",
            giverId = "giver-id",
            receiverId = "receiver-id",
            status = TransferStatus.PENDING,
            initiatedAt = Date()
        )
        
        // Setup common mock behavior
        whenever(mockFirestore.collection("transfers")).thenReturn(mockCollection)
        whenever(mockCollection.document()).thenReturn(mockDocument)
        whenever(mockCollection.document(anyString())).thenReturn(mockDocument)
        whenever(mockDocument.set(any())).thenReturn(mockTask)
        whenever(mockDocument.get()).thenReturn(mockGetTask)
        whenever(mockTask.await()).thenReturn(null)
    }

    @Test
    fun `createTransfer should return success with transfer ID`() = runTest {
        // Given
        whenever(mockDocument.id).thenReturn("generated-id")
        whenever(mockTask.await()).thenReturn(null)
        
        // When
        val result = transferRepository.createTransfer(testTransfer)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("generated-id", result.getOrNull())
        verify(mockDocument).set(any())
    }

    @Test
    fun `getTransfer should return success with transfer when found`() = runTest {
        // Given
        whenever(mockGetTask.await()).thenReturn(mockDocumentSnapshot)
        whenever(mockDocumentSnapshot.exists()).thenReturn(true)
        whenever(mockDocumentSnapshot.toObject(Transfer::class.java)).thenReturn(testTransfer)
        
        // When
        val result = transferRepository.getTransfer("test-transfer_id")
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(testTransfer, result.getOrNull())
        verify(mockDocument).get()
    }

    @Test
    fun `getTransfer should return failure when transfer not found`() = runTest {
        // Given
        whenever(mockGetTask.await()).thenReturn(mockDocumentSnapshot)
        whenever(mockDocumentSnapshot.exists()).thenReturn(false)
        
        // When
        val result = transferRepository.getTransfer("non-existent-id")
        
        // Then
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
        verify(mockDocument).get()
    }

    @Test
    fun `verifyTransfer should return success with verified transfer`() = runTest {
        // Given
        val pendingTransfer = testTransfer.copy(status = TransferStatus.PENDING)
        val verificationDetails = mapOf(
            "color" to "black",
            "weight_kg" to 2.5,
            "age_weeks" to 20
        )
        
        // Mock getTransfer call
        whenever(mockGetTask.await()).thenReturn(mockDocumentSnapshot)
        whenever(mockDocumentSnapshot.exists()).thenReturn(true)
        whenever(mockDocumentSnapshot.toObject(Transfer::class.java)).thenReturn(pendingTransfer)
        
        // Mock updateTransfer call
        val verifiedTransfer = pendingTransfer.copy(
            status = TransferStatus.VERIFIED,
            verificationDetails = verificationDetails,
            verifiedAt = Date()
        )
        
        // When
        val result = transferRepository.verifyTransfer("test-transfer-id", verificationDetails)
        
        // Then
        assertTrue(result.isSuccess)
        val returnedTransfer = result.getOrNull()
        assertNotNull(returnedTransfer)
        assertEquals(TransferStatus.VERIFIED, returnedTransfer?.status)
        assertEquals(verificationDetails, returnedTransfer?.verificationDetails)
        verify(mockDocument, times(2)).set(any()) // Once for get, once for update
    }

    @Test
    fun `verifyTransfer should return failure when transfer is not pending`() = runTest {
        // Given
        val verifiedTransfer = testTransfer.copy(status = TransferStatus.VERIFIED)
        val verificationDetails = mapOf<String, Any>()
        
        // Mock getTransfer call
        whenever(mockGetTask.await()).thenReturn(mockDocumentSnapshot)
        whenever(mockDocumentSnapshot.exists()).thenReturn(true)
        whenever(mockDocumentSnapshot.toObject(Transfer::class.java)).thenReturn(verifiedTransfer)
        
        // When
        val result = transferRepository.verifyTransfer("test-transfer-id", verificationDetails)
        
        // Then
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
        verify(mockDocument).get() // Only called for get, not update
    }

    @Test
    fun `rejectTransfer should return success with rejected transfer`() = runTest {
        // Given
        val pendingTransfer = testTransfer.copy(status = TransferStatus.PENDING)
        
        // Mock getTransfer call
        whenever(mockGetTask.await()).thenReturn(mockDocumentSnapshot)
        whenever(mockDocumentSnapshot.exists()).thenReturn(true)
        whenever(mockDocumentSnapshot.toObject(Transfer::class.java)).thenReturn(pendingTransfer)
        
        // When
        val result = transferRepository.rejectTransfer("test-transfer-id")
        
        // Then
        assertTrue(result.isSuccess)
        val returnedTransfer = result.getOrNull()
        assertNotNull(returnedTransfer)
        assertEquals(TransferStatus.REJECTED, returnedTransfer?.status)
        verify(mockDocument, times(2)).set(any()) // Once for get, once for update
    }

    @Test
    fun `rejectTransfer should return failure when transfer is not pending`() = runTest {
        // Given
        val rejectedTransfer = testTransfer.copy(status = TransferStatus.REJECTED)
        
        // Mock getTransfer call
        whenever(mockGetTask.await()).thenReturn(mockDocumentSnapshot)
        whenever(mockDocumentSnapshot.exists()).thenReturn(true)
        whenever(mockDocumentSnapshot.toObject(Transfer::class.java)).thenReturn(rejectedTransfer)
        
        // When
        val result = transferRepository.rejectTransfer("test-transfer-id")
        
        // Then
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
        verify(mockDocument).get() // Only called for get, not update
    }

    @Test
    fun `initiateTransfer should return success when transfer is created`() = runTest {
        // Given
        val fowlId = "fowl123"
        val fromUserId = "user456"
        val toUserId = "user789"
        val expectedPrice = 100.0
        val expectedColor = "brown"
        val expectedAgeWeeks = 10
        val expectedWeightGrams = 2000
        val photoReference = "photo999"
        
        // When
        val result = transferRepository.initiateTransfer(
            fowlId, fromUserId, toUserId,
            expectedPrice, expectedColor, expectedAgeWeeks,
            expectedWeightGrams, photoReference
        )
        
        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        verify(mockCollection).document(anyString()) // Verify document creation with any ID
        verify(mockDocument).set(any<Transfer>()) // Verify transfer object was set
    }

    @Test
    fun `verifyTransfer should return success when transfer is verified`() = runTest {
        // Given
        val transferId = "test-transfer-id"
        val verifierId = "user789"
        val verificationDetails = mapOf(
            "color" to "black",
            "weight_kg" to 2.5,
            "age_weeks" to 20
        )
        
        // Mock existing transfer
        val pendingTransfer = testTransfer.copy(
            status = TransferStatus.PENDING,
            verificationRequired = true,
            verificationStatus = "PENDING"
        )
        
        whenever(mockGetTask.await()).thenReturn(mockDocumentSnapshot)
        whenever(mockDocumentSnapshot.exists()).thenReturn(true)
        whenever(mockDocumentSnapshot.toObject(Transfer::class.java)).thenReturn(pendingTransfer)
        
        // When
        val result = transferRepository.verifyTransfer(transferId, verifierId, verificationDetails)
        
        // Then
        assertTrue(result.isSuccess)
        val updatedTransfer = result.getOrNull()
        assertNotNull(updatedTransfer)
        assertEquals(TransferStatus.VERIFIED, updatedTransfer?.status)
        assertEquals(verificationDetails, updatedTransfer?.verificationDetails)
        verify(mockDocument, times(2)).set(any()) // Once for get, once for update
    }

    @Test
    fun `verifyTransfer should return failure when verification_required is_false`() = runTest {
        // Given
        val transferId = "test-transfer-id"
        val verifierId = "user789"
        val verificationDetails = mapOf(
            "color" to "black",
            "weight_kg" to 2.5,
            "age_weeks" to 20
        )
        
        // Mock existing transfer without verification required
        val pendingTransfer = testTransfer.copy(
            status = TransferStatus.PENDING,
            verificationRequired = false,
            verificationStatus = "NOT_REQUIRED"
        )
        
        whenever(mockGetTask.await()).thenReturn(mockDocumentSnapshot)
        whenever(mockDocumentSnapshot.exists()).thenReturn(true)
        whenever(mockDocumentSnapshot.toObject(Transfer::class.java)).thenReturn(pendingTransfer)
        
        // When
        val result = transferRepository.verifyTransfer(transferId, verifierId, verificationDetails)
        
        // Then
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
        verify(mockDocument).get() // Only called for get, not update
    }

    @Test
    fun `rejectTransfer should return success with_rejection_details`() = runTest {
        // Given
        val transferId = "test-transfer-id"
        val rejectorId = "user789"
        val rejectionReason = "Incorrect fowl description"
        
        // Mock existing transfer
        val pendingTransfer = testTransfer.copy(status = TransferStatus.PENDING)
        
        whenever(mockGetTask.await()).thenReturn(mockDocumentSnapshot)
        whenever(mockDocumentSnapshot.exists()).thenReturn(true)
        whenever(mockDocumentSnapshot.toObject(Transfer::class.java)).thenReturn(pendingTransfer)
        
        // When
        val result = transferRepository.rejectTransfer(transferId, rejectorId, rejectionReason)
        
        // Then
        assertTrue(result.isSuccess)
        val returnedTransfer = result.getOrNull()
        assertNotNull(returnedTransfer)
        assertEquals(TransferStatus.REJECTED, returnedTransfer?.status)
        assertEquals(rejectorId, returnedTransfer?.rejectionDetails?.rejectedBy)
        assertEquals(rejectionReason, returnedTransfer?.rejectionDetails?.reason)
        verify(mockDocument, times(2)).set(any()) // Once for get, once for update
    }
}
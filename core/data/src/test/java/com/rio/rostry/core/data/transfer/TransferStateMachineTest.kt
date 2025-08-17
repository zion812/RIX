package com.rio.rostry.core.data.transfer

import com.rio.rostry.core.data.model.Transfer
import com.rio.rostry.core.data.model.TransferStatus
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class TransferStateMachineTest {

    private lateinit var stateMachine: TransferStateMachine
    private lateinit var testTransfer: Transfer

    @Before
    fun setup() {
        stateMachine = TransferStateMachine()
        testTransfer = Transfer(
            id = "test-transfer-id",
            fowlId = "test-fowl-id",
            giverId = "giver-id",
            receiverId = "receiver-id"
        )
    }

    @Test
    fun `initial state should be Idle`() {
        assertTrue(stateMachine.state.value is TransferState.Idle)
    }

    @Test
    fun `initiateTransfer should change state to Pending`() = runTest {
        val result = stateMachine.initiateTransfer(testTransfer)
        
        assertTrue(result.isSuccess)
        assertTrue(stateMachine.state.value is TransferState.Pending)
        assertEquals(TransferStatus.PENDING, (stateMachine.state.value as TransferState.Pending).transfer.status)
    }

    @Test
    fun `verifyTransfer should change state to Verified when transfer is pending`() = runTest {
        // First initiate transfer
        stateMachine.initiateTransfer(testTransfer)
        
        val verificationDetails = mapOf(
            "photo_keys" to listOf("photo1.jpg", "photo2.jpg"),
            "color" to "black",
            "weight_kg" to 2.5,
            "age_weeks" to 20,
            "location" to mapOf("lat" to 17.3850, "lng" to 78.4867),
            "agreed_price_cents" to 15000
        )
        
        val result = stateMachine.verifyTransfer(testTransfer, verificationDetails)
        
        assertTrue(result.isSuccess)
        assertTrue(stateMachine.state.value is TransferState.Verified)
        val verifiedState = stateMachine.state.value as TransferState.Verified
        assertEquals(TransferStatus.VERIFIED, verifiedState.transfer.status)
        assertEquals(verificationDetails, verifiedState.transfer.verificationDetails)
    }

    @Test
    fun `verifyTransfer should fail when transfer is not pending`() = runTest {
        // Try to verify without initiating first
        val verificationDetails = mapOf<String, Any>()
        val result = stateMachine.verifyTransfer(testTransfer, verificationDetails)
        
        assertTrue(result.isFailure)
        assertTrue(stateMachine.state.value is TransferState.Idle)
    }

    @Test
    fun `rejectTransfer should change state to Rejected when transfer is pending`() = runTest {
        // First initiate transfer
        stateMachine.initiateTransfer(testTransfer)
        
        val result = stateMachine.rejectTransfer(testTransfer)
        
        assertTrue(result.isSuccess)
        assertTrue(stateMachine.state.value is TransferState.Rejected)
        assertEquals(TransferStatus.REJECTED, (stateMachine.state.value as TransferState.Rejected).transfer.status)
    }

    @Test
    fun `rejectTransfer should fail when transfer is not pending`() = runTest {
        // Try to reject without initiating first
        val result = stateMachine.rejectTransfer(testTransfer)
        
        assertTrue(result.isFailure)
        assertTrue(stateMachine.state.value is TransferState.Idle)
    }

    @Test
    fun `reset should change state back to Idle`() = runTest {
        // First initiate transfer
        stateMachine.initiateTransfer(testTransfer)
        assertTrue(stateMachine.state.value is TransferState.Pending)
        
        // Reset state machine
        stateMachine.reset()
        assertTrue(stateMachine.state.value is TransferState.Idle)
    }
}
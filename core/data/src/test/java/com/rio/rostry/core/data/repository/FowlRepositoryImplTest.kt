package com.rio.rostry.core.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rostry.core.database.dao.FowlDao
import com.rio.rostry.core.database.entities.FowlEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.util.*

@ExperimentalCoroutinesApi
class FowlRepositoryImplTest {

    private lateinit var fowlRepository: FowlRepositoryImpl
    private val fowlDao: FowlDao = mock()
    private val firestore: FirebaseFirestore = mock()

    // Mocks for Firestore path
    private val collectionReference: CollectionReference = mock()
    private val documentReference: DocumentReference = mock()
    private val documentSnapshot: DocumentSnapshot = mock()
    private val task: Task<DocumentSnapshot> = mock()
    private val voidTask: Task<Void> = mock()

    @Before
    fun setUp() {
        // Mock the Firestore path: firestore.collection("fowls").document(id)
        whenever(firestore.collection(any())).thenReturn(collectionReference)
        whenever(collectionReference.document(any())).thenReturn(documentReference)

        fowlRepository = FowlRepositoryImpl(fowlDao, firestore)
    }

    // Helper to create the complex FowlEntity for tests
    private fun createTestFowl(id: String) = FowlEntity(
        id = id,
        ownerId = "owner1",
        name = "Test Fowl",
        breedPrimary = "Test Breed",
        healthStatus = "GOOD",
        availabilityStatus = "AVAILABLE",
        region = "Test Region",
        district = "Test District",
        createdAt = Date(),
        updatedAt = Date()
    )

    @Test
    fun `getFowlById when fowl exists in local DAO returns local entity`() = runTest {
        // Arrange
        val fowlId = "123"
        val localFowl = createTestFowl(fowlId)
        whenever(fowlDao.getById(fowlId)).thenReturn(localFowl)

        // Act
        val result = fowlRepository.getFowlById(fowlId)

        // Assert
        assert(result == localFowl)
        verify(firestore, never()).collection(any()) // Verify Firestore was NOT called
    }

    @Test
    fun `getFowlById when fowl not in DAO fetches from Firestore and inserts locally`() = runTest {
        // Arrange
        val fowlId = "123"
        val serverFowl = createTestFowl(fowlId)
        whenever(fowlDao.getById(fowlId)).thenReturn(null) // Not in local cache

        // Mock Firestore response
        whenever(documentReference.get()).thenReturn(task)
        whenever(task.await()).thenReturn(documentSnapshot)
        whenever(documentSnapshot.exists()).thenReturn(true)
        whenever(documentSnapshot.toFowlEntity()).thenReturn(serverFowl)

        // Act
        val result = fowlRepository.getFowlById(fowlId)

        // Assert
        assert(result == serverFowl)
        verify(fowlDao).getById(fowlId)      // Checked local first
        verify(documentReference).get()      // Then fetched from server
        verify(fowlDao).insert(serverFowl)   // And inserted into local cache
    }

    @Test
    fun `saveFowl writes to DAO and then to Firestore`() = runTest {
        // Arrange
        val fowlToSave = createTestFowl("123")
        whenever(documentReference.set(any())).thenReturn(voidTask)
        whenever(voidTask.await()).thenReturn(null) // Successful task

        // Act
        val result = fowlRepository.saveFowl(fowlToSave)

        // Assert
        assert(result.isSuccess)
        verify(fowlDao).insert(fowlToSave)
        verify(documentReference).set(any())
    }

    @Test
    fun `saveFowl succeeds even if Firestore write fails`() = runTest {
        // Arrange
        val fowlToSave = createTestFowl("123")
        // Make Firestore throw an exception
        whenever(documentReference.set(any())).thenThrow(RuntimeException("Firestore error"))

        // Act
        val result = fowlRepository.saveFowl(fowlToSave)

        // Assert
        assert(result.isSuccess) // The operation should still succeed from the user's perspective
        verify(fowlDao).insert(fowlToSave) // Verify DAO was still called
        verify(documentReference).set(any()) // Verify firestore was still attempted
    }
}

package com.rio.rostry.core.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.rio.rostry.core.database.dao.UserDao
import com.rio.rostry.core.database.entities.UserEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.util.Date

@ExperimentalCoroutinesApi
class UserRepositoryImplTest {

    private lateinit var userRepository: UserRepositoryImpl
    private val userDao: UserDao = mock()
    private val firestore: FirebaseFirestore = mock()

    // Mocks for Firestore path
    private val collectionReference: CollectionReference = mock()
    private val documentReference: DocumentReference = mock()
    private val query: Query = mock()
    private val documentSnapshot: DocumentSnapshot = mock()
    private val querySnapshot: QuerySnapshot = mock()
    private val task: Task<DocumentSnapshot> = mock()
    private val queryTask: Task<QuerySnapshot> = mock()
    private val voidTask: Task<Void> = mock()

    @Before
    fun setUp() {
        whenever(firestore.collection(any())).thenReturn(collectionReference)
        whenever(collectionReference.document(any())).thenReturn(documentReference)
        whenever(collectionReference.whereGreaterThanOrEqualTo(any<String>(), any())).thenReturn(query)
        whenever(query.whereLessThanOrEqualTo(any<String>(), any())).thenReturn(query)
        whenever(query.limit(any())).thenReturn(query)

        userRepository = UserRepositoryImpl(userDao, firestore)
    }

    private fun createTestUser(id: String) = UserEntity(
        id = id,
        displayName = "Test User",
        email = "test@example.com",
        tier = "farmer",
        region = "Test Region",
        district = "Test District",
        createdAt = Date()
    )

    @Test
    fun `getUserById when user in DAO returns local user`() = runTest {
        val userId = "123"
        val localUser = createTestUser(userId)
        whenever(userDao.getById(userId)).thenReturn(localUser)

        val result = userRepository.getUserById(userId)

        assert(result == localUser)
        verify(firestore, never()).collection(any())
    }

    @Test
    fun `getUserById when user not in DAO fetches from Firestore`() = runTest {
        val userId = "123"
        val serverUser = createTestUser(userId)
        whenever(userDao.getById(userId)).thenReturn(null)
        whenever(documentReference.get()).thenReturn(task)
        whenever(task.await()).thenReturn(documentSnapshot)
        whenever(documentSnapshot.toUserEntity()).thenReturn(serverUser)

        val result = userRepository.getUserById(userId)

        assert(result == serverUser)
        verify(userDao).getById(userId)
        verify(documentReference).get()
        verify(userDao).insert(serverUser)
    }

    @Test
    fun `updateUserProfile updates DAO and then Firestore`() = runTest {
        val userToUpdate = createTestUser("123")
        whenever(documentReference.update(any<Map<String, Any?>>())).thenReturn(voidTask)
        whenever(voidTask.await()).thenReturn(null)

        val result = userRepository.updateUserProfile(userToUpdate)

        assert(result.isSuccess)
        verify(userDao).update(userToUpdate)
        verify(documentReference).update(any<Map<String, Any?>>())
    }

    @Test
    fun `searchUsers when Firestore fails falls back to local DAO search`() = runTest {
        val queryText = "test"
        val localUsers = listOf(createTestUser("123"))
        whenever(query.get()).thenThrow(RuntimeException("Network error"))
        whenever(userDao.searchUsersByName(queryText, 20)).thenReturn(localUsers)

        val result = userRepository.searchUsers(queryText)

        assert(result == localUsers)
        verify(userDao).searchUsersByName(queryText, 20)
    }
}

package com.rio.rostry.core.database.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rio.rostry.core.database.RIOLocalDatabase
import com.rio.rostry.core.database.entities.UserEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.google.common.truth.Truth.assertThat
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class UserDaoTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var database: RIOLocalDatabase
    private lateinit var userDao: UserDao
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RIOLocalDatabase::class.java
        ).allowMainThreadQueries().build()
        
        userDao = database.userDao()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun insertAndGetUser() = runTest {
        // Given
        val user = UserEntity(
            id = "test-user-id",
            displayName = "Test User",
            email = "test@example.com",
            phoneNumber = "+1234567890",
            tier = "GENERAL",
            profileImageUrl = null,
            location = "Test City",
            coinBalance = 100,
            isVerified = false,
            createdAt = Date(),
            updatedAt = Date(),
            lastLoginAt = Date(),
            isSynced = false
        )
        
        // When
        userDao.insert(user)
        val retrievedUser = userDao.getById(user.id)
        
        // Then
        assertThat(retrievedUser).isNotNull()
        assertThat(retrievedUser?.id).isEqualTo(user.id)
        assertThat(retrievedUser?.displayName).isEqualTo(user.displayName)
        assertThat(retrievedUser?.email).isEqualTo(user.email)
        assertThat(retrievedUser?.tier).isEqualTo(user.tier)
        assertThat(retrievedUser?.coinBalance).isEqualTo(user.coinBalance)
    }
    
    @Test
    fun updateUser() = runTest {
        // Given
        val user = UserEntity(
            id = "test-user-id",
            displayName = "Test User",
            email = "test@example.com",
            phoneNumber = null,
            tier = "GENERAL",
            profileImageUrl = null,
            location = null,
            coinBalance = 100,
            isVerified = false,
            createdAt = Date(),
            updatedAt = Date(),
            lastLoginAt = Date(),
            isSynced = false
        )
        
        userDao.insert(user)
        
        // When
        val updatedUser = user.copy(
            displayName = "Updated User",
            coinBalance = 200,
            isVerified = true
        )
        userDao.update(updatedUser)
        val retrievedUser = userDao.getById(user.id)
        
        // Then
        assertThat(retrievedUser?.displayName).isEqualTo("Updated User")
        assertThat(retrievedUser?.coinBalance).isEqualTo(200)
        assertThat(retrievedUser?.isVerified).isTrue()
    }
    
    @Test
    fun deleteUser() = runTest {
        // Given
        val user = UserEntity(
            id = "test-user-id",
            displayName = "Test User",
            email = "test@example.com",
            phoneNumber = null,
            tier = "GENERAL",
            profileImageUrl = null,
            location = null,
            coinBalance = 100,
            isVerified = false,
            createdAt = Date(),
            updatedAt = Date(),
            lastLoginAt = Date(),
            isSynced = false
        )
        
        userDao.insert(user)
        
        // When
        userDao.delete(user)
        val retrievedUser = userDao.getById(user.id)
        
        // Then
        assertThat(retrievedUser).isNull()
    }
    
    @Test
    fun observeUser() = runTest {
        // Given
        val user = UserEntity(
            id = "test-user-id",
            displayName = "Test User",
            email = "test@example.com",
            phoneNumber = null,
            tier = "GENERAL",
            profileImageUrl = null,
            location = null,
            coinBalance = 100,
            isVerified = false,
            createdAt = Date(),
            updatedAt = Date(),
            lastLoginAt = Date(),
            isSynced = false
        )
        
        // When
        userDao.insert(user)
        val observedUser = userDao.observeUser(user.id).first()
        
        // Then
        assertThat(observedUser).isNotNull()
        assertThat(observedUser?.id).isEqualTo(user.id)
        assertThat(observedUser?.displayName).isEqualTo(user.displayName)
    }
    
    @Test
    fun searchByName() = runTest {
        // Given
        val users = listOf(
            UserEntity(
                id = "user1",
                displayName = "John Doe",
                email = "john@example.com",
                phoneNumber = null,
                tier = "GENERAL",
                profileImageUrl = null,
                location = null,
                coinBalance = 100,
                isVerified = false,
                createdAt = Date(),
                updatedAt = Date(),
                lastLoginAt = Date(),
                isSynced = false
            ),
            UserEntity(
                id = "user2",
                displayName = "Jane Smith",
                email = "jane@example.com",
                phoneNumber = null,
                tier = "FARMER",
                profileImageUrl = null,
                location = null,
                coinBalance = 200,
                isVerified = true,
                createdAt = Date(),
                updatedAt = Date(),
                lastLoginAt = Date(),
                isSynced = false
            ),
            UserEntity(
                id = "user3",
                displayName = "John Johnson",
                email = "johnson@example.com",
                phoneNumber = null,
                tier = "ENTHUSIAST",
                profileImageUrl = null,
                location = null,
                coinBalance = 300,
                isVerified = true,
                createdAt = Date(),
                updatedAt = Date(),
                lastLoginAt = Date(),
                isSynced = false
            )
        )
        
        users.forEach { userDao.insert(it) }
        
        // When
        val searchResults = userDao.searchByName("John", 10)
        
        // Then
        assertThat(searchResults).hasSize(2)
        assertThat(searchResults.map { it.displayName }).containsExactly("John Doe", "John Johnson")
    }
    
    @Test
    fun getUsersByTier() = runTest {
        // Given
        val users = listOf(
            UserEntity(
                id = "user1",
                displayName = "General User 1",
                email = "general1@example.com",
                phoneNumber = null,
                tier = "GENERAL",
                profileImageUrl = null,
                location = null,
                coinBalance = 100,
                isVerified = false,
                createdAt = Date(),
                updatedAt = Date(),
                lastLoginAt = Date(),
                isSynced = false
            ),
            UserEntity(
                id = "user2",
                displayName = "Farmer User",
                email = "farmer@example.com",
                phoneNumber = null,
                tier = "FARMER",
                profileImageUrl = null,
                location = null,
                coinBalance = 200,
                isVerified = true,
                createdAt = Date(),
                updatedAt = Date(),
                lastLoginAt = Date(),
                isSynced = false
            ),
            UserEntity(
                id = "user3",
                displayName = "General User 2",
                email = "general2@example.com",
                phoneNumber = null,
                tier = "GENERAL",
                profileImageUrl = null,
                location = null,
                coinBalance = 150,
                isVerified = false,
                createdAt = Date(),
                updatedAt = Date(),
                lastLoginAt = Date(),
                isSynced = false
            )
        )
        
        users.forEach { userDao.insert(it) }
        
        // When
        val generalUsers = userDao.getUsersByTier("GENERAL")
        val farmerUsers = userDao.getUsersByTier("FARMER")
        
        // Then
        assertThat(generalUsers).hasSize(2)
        assertThat(farmerUsers).hasSize(1)
        assertThat(farmerUsers[0].displayName).isEqualTo("Farmer User")
    }
    
    @Test
    fun getUnsyncedUsers() = runTest {
        // Given
        val users = listOf(
            UserEntity(
                id = "user1",
                displayName = "Synced User",
                email = "synced@example.com",
                phoneNumber = null,
                tier = "GENERAL",
                profileImageUrl = null,
                location = null,
                coinBalance = 100,
                isVerified = false,
                createdAt = Date(),
                updatedAt = Date(),
                lastLoginAt = Date(),
                isSynced = true // Already synced
            ),
            UserEntity(
                id = "user2",
                displayName = "Unsynced User",
                email = "unsynced@example.com",
                phoneNumber = null,
                tier = "FARMER",
                profileImageUrl = null,
                location = null,
                coinBalance = 200,
                isVerified = true,
                createdAt = Date(),
                updatedAt = Date(),
                lastLoginAt = Date(),
                isSynced = false // Not synced
            )
        )
        
        users.forEach { userDao.insert(it) }
        
        // When
        val unsyncedUsers = userDao.getUnsyncedUsers()
        
        // Then
        assertThat(unsyncedUsers).hasSize(1)
        assertThat(unsyncedUsers[0].displayName).isEqualTo("Unsynced User")
        assertThat(unsyncedUsers[0].isSynced).isFalse()
    }
    
    @Test
    fun markAsSynced() = runTest {
        // Given
        val user = UserEntity(
            id = "test-user-id",
            displayName = "Test User",
            email = "test@example.com",
            phoneNumber = null,
            tier = "GENERAL",
            profileImageUrl = null,
            location = null,
            coinBalance = 100,
            isVerified = false,
            createdAt = Date(),
            updatedAt = Date(),
            lastLoginAt = Date(),
            isSynced = false
        )
        
        userDao.insert(user)
        
        // When
        userDao.markAsSynced(user.id)
        val retrievedUser = userDao.getById(user.id)
        
        // Then
        assertThat(retrievedUser?.isSynced).isTrue()
    }
}

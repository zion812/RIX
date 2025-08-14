package com.rio.rostry.core.database.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Database entity for user information
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    override val id: String,
    val email: String,
    val displayName: String,
    val phoneNumber: String? = null,
    val photoUrl: String? = null,
    val tier: String = "general",
    val coinBalance: Int = 0,
    val pendingCoinBalance: Int = 0,
    val region: String = "other",
    val district: String = "",
    val language: String = "en",
    val isEmailVerified: Boolean = false,
    val isPhoneVerified: Boolean = false,
    val createdAt: Date,
    val updatedAt: Date = Date(),
    val lastLoginAt: Date? = null,
    val isSynced: Boolean = false,
    @Embedded
    override val syncMetadata: SyncMetadata = SyncMetadata()
) : SyncableEntity
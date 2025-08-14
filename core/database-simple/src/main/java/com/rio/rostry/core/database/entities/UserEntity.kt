package com.rio.rostry.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Simplified User entity for Phase 2
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val displayName: String,
    val phoneNumber: String? = null,
    val photoUrl: String? = null,
    val tier: String = "general", // general, premium, expert
    val region: String = "other",
    val district: String = "",
    val language: String = "en",
    val isEmailVerified: Boolean = false,
    val isPhoneVerified: Boolean = false,
    val createdAt: Date,
    val updatedAt: Date = Date(),
    val lastLoginAt: Date? = null
)

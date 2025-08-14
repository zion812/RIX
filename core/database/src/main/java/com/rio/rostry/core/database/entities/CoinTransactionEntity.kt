package com.rio.rostry.core.database.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rio.rostry.core.common.model.TransactionStatus
import com.rio.rostry.core.common.model.TransactionType
import java.util.*

/**
 * Database entity for coin transactions
 */
@Entity(tableName = "coin_transactions")
data class CoinTransactionEntity(
    @PrimaryKey
    override val id: String,
    val userId: String,
    val amount: Int,
    val transactionType: TransactionType,
    val status: TransactionStatus,
    val purpose: String,
    val description: String? = null,
    val referenceId: String? = null,
    val paymentId: String? = null,
    val orderId: String? = null,
    val createdAt: Date,
    val updatedAt: Date = Date(),
    val completedAt: Date? = null,
    val isSynced: Boolean = false,
    @Embedded
    override val syncMetadata: SyncMetadata = SyncMetadata()
) : SyncableEntity

/**
 * Database entity for user coin balance
 */
@Entity(tableName = "user_coin_balances")
data class UserCoinBalanceEntity(
    @PrimaryKey
    val userId: String,
    val balance: Int,
    val pendingBalance: Int = 0,
    val lastUpdated: Date = Date(),
    val isSynced: Boolean = false
)
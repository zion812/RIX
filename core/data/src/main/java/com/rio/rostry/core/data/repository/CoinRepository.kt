package com.rio.rostry.core.data.repository

import com.rio.rostry.core.database.dao.CoinDao
import com.rio.rostry.core.database.entities.CoinTransactionEntity
import com.rio.rostry.core.database.entities.UserCoinBalanceEntity
import com.rio.rostry.core.data.model.CoinTransaction
import com.rio.rostry.core.data.model.CoinTransactionType
import com.rio.rostry.core.data.util.DataSyncManager
import com.rio.rostry.core.data.util.SyncOperation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Repository for managing coin transactions with offline-first capabilities
 */
class CoinRepository @Inject constructor(
    private val coinDao: CoinDao,
    private val syncManager: DataSyncManager
) {
    
    /**
     * Get user's current coin balance
     */
    suspend fun getCoinBalance(userId: String): Result<Int> {
        return try {
            val balanceEntity = coinDao.getUserBalance(userId)
            Result.success(balanceEntity?.balance ?: 0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user's coin transaction history
     */
    fun getTransactionHistory(userId: String): Flow<List<CoinTransaction>> {
        return coinDao.getTransactionsByUser(userId)
            .map { entities ->
                entities.map { it.toModel() }
            }
    }
    
    /**
     * Add coins to user's balance (credit)
     */
    suspend fun addCoins(userId: String, amount: Int, transactionType: CoinTransactionType, relatedEntityId: String? = null): Result<Unit> {
        return try {
            // Create transaction
            val transaction = CoinTransaction(
                userId = userId,
                amount = amount,
                transactionType = transactionType,
                relatedEntityId = relatedEntityId
            )
            
            val transactionEntity = transaction.toEntity()
            coinDao.insertTransaction(transactionEntity)
            
            // Update user balance
            val currentBalanceResult = getCoinBalance(userId)
            val currentBalance = if (currentBalanceResult.isSuccess) currentBalanceResult.getOrNull() ?: 0 else 0
            val newBalance = currentBalance + amount
            
            val balanceEntity = UserCoinBalanceEntity(
                userId = userId,
                balance = newBalance,
                updatedAt = java.util.Date()
            )
            coinDao.updateUserBalance(balanceEntity)
            
            // Queue for sync
            syncManager.queueSyncOperation(
                SyncOperation.Create(
                    collection = "coin_transactions",
                    documentId = transactionEntity.id,
                    data = transactionEntity.toMap()
                )
            )
            
            syncManager.queueSyncOperation(
                SyncOperation.Update(
                    collection = "user_coin_balances",
                    documentId = userId,
                    data = mapOf(
                        "user_id" to userId,
                        "balance" to newBalance,
                        "updated_at" to java.util.Date()
                    )
                )
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Deduct coins from user's balance (debit)
     */
    suspend fun deductCoins(userId: String, amount: Int, transactionType: CoinTransactionType, relatedEntityId: String? = null): Result<Unit> {
        return try {
            val balanceResult = getCoinBalance(userId)
            if (balanceResult.isFailure) {
                return Result.failure(balanceResult.exceptionOrNull() ?: Exception("Failed to get balance"))
            }
            
            val currentBalance = balanceResult.getOrNull() ?: 0
            if (currentBalance < amount) {
                return Result.failure(IllegalStateException("Insufficient coin balance"))
            }
            
            // Create transaction
            val transaction = CoinTransaction(
                userId = userId,
                amount = -amount, // Negative for deduction
                transactionType = transactionType,
                relatedEntityId = relatedEntityId
            )
            
            val transactionEntity = transaction.toEntity()
            coinDao.insertTransaction(transactionEntity)
            
            // Update user balance
            val newBalance = currentBalance - amount
            val balanceEntity = UserCoinBalanceEntity(
                userId = userId,
                balance = newBalance,
                updatedAt = java.util.Date()
            )
            coinDao.updateUserBalance(balanceEntity)
            
            // Queue for sync
            syncManager.queueSyncOperation(
                SyncOperation.Create(
                    collection = "coin_transactions",
                    documentId = transactionEntity.id,
                    data = transactionEntity.toMap()
                )
            )
            
            syncManager.queueSyncOperation(
                SyncOperation.Update(
                    collection = "user_coin_balances",
                    documentId = userId,
                    data = mapOf(
                        "user_id" to userId,
                        "balance" to newBalance,
                        "updated_at" to java.util.Date()
                    )
                )
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Extension function to convert CoinTransaction model to CoinTransactionEntity
     */
    private fun CoinTransaction.toEntity(): CoinTransactionEntity {
        return CoinTransactionEntity(
            id = this.id,
            userId = this.userId,
            amount = this.amount,
            transactionType = this.transactionType.name,
            relatedEntityId = this.relatedEntityId,
            createdAt = this.createdAt,
            syncStatus = "pending",
            syncPriority = 1,
            isDeleted = false
        )
    }
    
    /**
     * Extension function to convert CoinTransactionEntity to CoinTransaction model
     */
    private fun CoinTransactionEntity.toModel(): CoinTransaction {
        return CoinTransaction(
            id = this.id,
            userId = this.userId,
            amount = this.amount,
            transactionType = try {
                CoinTransactionType.valueOf(this.transactionType)
            } catch (e: IllegalArgumentException) {
                CoinTransactionType.ADMIN_CREDIT
            },
            relatedEntityId = this.relatedEntityId,
            createdAt = this.createdAt
        )
    }
}
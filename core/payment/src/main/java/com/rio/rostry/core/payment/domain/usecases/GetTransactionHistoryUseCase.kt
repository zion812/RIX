package com.rio.rostry.core.payment.domain.usecases

import com.rio.rostry.core.data.repository.CoinRepositoryImpl
import com.rio.rostry.core.database.entities.CoinTransactionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionHistoryUseCase @Inject constructor(
    private val coinRepository: CoinRepositoryImpl
) {
    operator fun invoke(): Flow<List<CoinTransactionEntity>> {
        return coinRepository.getTransactionHistory()
    }
}

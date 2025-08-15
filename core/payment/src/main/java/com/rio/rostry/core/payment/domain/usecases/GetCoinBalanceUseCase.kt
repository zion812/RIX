package com.rio.rostry.core.payment.domain.usecases

import com.rio.rostry.core.data.repository.CoinRepositoryImpl
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCoinBalanceUseCase @Inject constructor(
    private val coinRepository: CoinRepositoryImpl
) {
    operator fun invoke(): Flow<Int> {
        return coinRepository.getCoinBalance()
    }
}

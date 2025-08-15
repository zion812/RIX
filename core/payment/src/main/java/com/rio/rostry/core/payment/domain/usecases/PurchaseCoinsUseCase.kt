package com.rio.rostry.core.payment.domain.usecases

import com.rio.rostry.core.data.repository.CoinRepositoryImpl
import com.rio.rostry.core.data.repository.CoinPackage
import com.rio.rostry.core.data.repository.PaymentMethod
import javax.inject.Inject

class PurchaseCoinsUseCase @Inject constructor(
    private val coinRepository: CoinRepositoryImpl
) {
    suspend operator fun invoke(coinPackage: CoinPackage, paymentMethod: PaymentMethod): Result<String> {
        return coinRepository.purchaseCoins(coinPackage, paymentMethod)
    }
}

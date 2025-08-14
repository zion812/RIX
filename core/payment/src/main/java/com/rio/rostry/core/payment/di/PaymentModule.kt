package com.rio.rostry.core.payment.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.rio.rostry.core.payment.SimplePaymentManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for payment dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object PaymentModule {

    @Provides
    @Singleton
    fun provideSimplePaymentManager(
        @ApplicationContext context: Context,
        auth: FirebaseAuth,
        functions: FirebaseFunctions
    ): SimplePaymentManager {
        return SimplePaymentManager(context, auth, functions)
    }
}

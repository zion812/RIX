package com.rio.rostry.core.payment.di;

/**
 * Hilt module for payment dependencies
 */
@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\"\u0010\u0003\u001a\u00020\u00042\b\b\u0001\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0007\u00a8\u0006\u000b"}, d2 = {"Lcom/rio/rostry/core/payment/di/PaymentModule;", "", "()V", "provideSimplePaymentManager", "Lcom/rio/rostry/core/payment/SimplePaymentManager;", "context", "Landroid/content/Context;", "auth", "Lcom/google/firebase/auth/FirebaseAuth;", "functions", "Lcom/google/firebase/functions/FirebaseFunctions;", "payment_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class PaymentModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.rio.rostry.core.payment.di.PaymentModule INSTANCE = null;
    
    private PaymentModule() {
        super();
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.rio.rostry.core.payment.SimplePaymentManager provideSimplePaymentManager(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.google.firebase.auth.FirebaseAuth auth, @org.jetbrains.annotations.NotNull()
    com.google.firebase.functions.FirebaseFunctions functions) {
        return null;
    }
}
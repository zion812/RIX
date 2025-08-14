package com.rio.rostry.core.payment;

/**
 * Simplified data classes and interfaces
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\bf\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&J \u0010\u0006\u001a\u00020\u00032\u0006\u0010\u0007\u001a\u00020\u00052\u0006\u0010\b\u001a\u00020\u00052\u0006\u0010\t\u001a\u00020\nH&\u00a8\u0006\u000b"}, d2 = {"Lcom/rio/rostry/core/payment/SimplePaymentCallback;", "", "onPaymentError", "", "error", "", "onPaymentSuccess", "paymentId", "orderId", "coinsAdded", "", "payment_debug"})
public abstract interface SimplePaymentCallback {
    
    public abstract void onPaymentSuccess(@org.jetbrains.annotations.NotNull()
    java.lang.String paymentId, @org.jetbrains.annotations.NotNull()
    java.lang.String orderId, int coinsAdded);
    
    public abstract void onPaymentError(@org.jetbrains.annotations.NotNull()
    java.lang.String error);
}
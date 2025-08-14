package com.rio.rostry.core.payment;

/**
 * Demo Payment Gateway - Minimal Stub Implementation
 * Provides basic payment simulation for testing and demonstration
 */
@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0006\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u0000 \u001c2\u00020\u0001:\u0001\u001cB\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J&\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010\u000bJ\u0010\u0010\f\u001a\u00020\b2\u0006\u0010\r\u001a\u00020\bH\u0002J\u0010\u0010\u000e\u001a\u00020\b2\u0006\u0010\r\u001a\u00020\bH\u0002J\u0016\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\u0012J&\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0011\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u0015\u001a\u00020\u0016H\u0086@\u00a2\u0006\u0002\u0010\u0017J\u0010\u0010\u0018\u001a\u00020\u0019*\u00060\u001aj\u0002`\u001bH\u0002\u00a8\u0006\u001d"}, d2 = {"Lcom/rio/rostry/core/payment/DemoPaymentGateway;", "", "()V", "createDemoOrder", "Lcom/rio/rostry/core/payment/DemoOrderResult;", "amount", "", "packageId", "", "paymentMethod", "Lcom/rio/rostry/core/payment/DemoPaymentMethod;", "(DLjava/lang/String;Lcom/rio/rostry/core/payment/DemoPaymentMethod;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "generateDemoPaymentId", "prefix", "generateDemoPaymentToken", "getDemoPaymentStatus", "Lcom/rio/rostry/core/payment/DemoPaymentStatus;", "orderId", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "processDemoPayment", "Lcom/rio/rostry/core/payment/DemoPaymentResult;", "paymentDetails", "Lcom/rio/rostry/core/payment/DemoPaymentDetails;", "(Ljava/lang/String;Lcom/rio/rostry/core/payment/DemoPaymentMethod;Lcom/rio/rostry/core/payment/DemoPaymentDetails;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "toSyncException", "Lcom/rio/rostry/core/common/exceptions/SyncException;", "Ljava/lang/Exception;", "Lkotlin/Exception;", "Companion", "payment_debug"})
public final class DemoPaymentGateway {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String DEMO_GATEWAY_ID = "DEMO_GATEWAY";
    private static final int MAX_DEMO_AMOUNT = 500000;
    @org.jetbrains.annotations.NotNull()
    public static final com.rio.rostry.core.payment.DemoPaymentGateway.Companion Companion = null;
    
    @javax.inject.Inject()
    public DemoPaymentGateway() {
        super();
    }
    
    /**
     * Create demo payment order
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object createDemoOrder(double amount, @org.jetbrains.annotations.NotNull()
    java.lang.String packageId, @org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.payment.DemoPaymentMethod paymentMethod, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.rio.rostry.core.payment.DemoOrderResult> $completion) {
        return null;
    }
    
    /**
     * Process demo payment with realistic simulation
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object processDemoPayment(@org.jetbrains.annotations.NotNull()
    java.lang.String orderId, @org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.payment.DemoPaymentMethod paymentMethod, @org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.payment.DemoPaymentDetails paymentDetails, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.rio.rostry.core.payment.DemoPaymentResult> $completion) {
        return null;
    }
    
    /**
     * Get demo payment status
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getDemoPaymentStatus(@org.jetbrains.annotations.NotNull()
    java.lang.String orderId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.rio.rostry.core.payment.DemoPaymentStatus> $completion) {
        return null;
    }
    
    private final java.lang.String generateDemoPaymentToken(java.lang.String prefix) {
        return null;
    }
    
    private final java.lang.String generateDemoPaymentId(java.lang.String prefix) {
        return null;
    }
    
    private final com.rio.rostry.core.common.exceptions.SyncException toSyncException(java.lang.Exception $this$toSyncException) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/rio/rostry/core/payment/DemoPaymentGateway$Companion;", "", "()V", "DEMO_GATEWAY_ID", "", "MAX_DEMO_AMOUNT", "", "payment_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}
package com.rio.rostry.core.payment;

/**
 * Data classes for demo payment gateway
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0007\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007\u00a8\u0006\b"}, d2 = {"Lcom/rio/rostry/core/payment/DemoPaymentMethod;", "", "(Ljava/lang/String;I)V", "UPI", "GOOGLE_PAY", "CARD", "NET_BANKING", "WALLET", "payment_debug"})
public enum DemoPaymentMethod {
    /*public static final*/ UPI /* = new UPI() */,
    /*public static final*/ GOOGLE_PAY /* = new GOOGLE_PAY() */,
    /*public static final*/ CARD /* = new CARD() */,
    /*public static final*/ NET_BANKING /* = new NET_BANKING() */,
    /*public static final*/ WALLET /* = new WALLET() */;
    
    DemoPaymentMethod() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.rio.rostry.core.payment.DemoPaymentMethod> getEntries() {
        return null;
    }
}
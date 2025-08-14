package com.rio.rostry.core.common.model;

/**
 * Payment methods supported
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\b\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\b\u00a8\u0006\t"}, d2 = {"Lcom/rio/rostry/core/common/model/PaymentMethod;", "", "(Ljava/lang/String;I)V", "RAZORPAY", "PAYU", "UPI", "NETBANKING", "CARD", "WALLET", "common_debug"})
public enum PaymentMethod {
    /*public static final*/ RAZORPAY /* = new RAZORPAY() */,
    /*public static final*/ PAYU /* = new PAYU() */,
    /*public static final*/ UPI /* = new UPI() */,
    /*public static final*/ NETBANKING /* = new NETBANKING() */,
    /*public static final*/ CARD /* = new CARD() */,
    /*public static final*/ WALLET /* = new WALLET() */;
    
    PaymentMethod() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.rio.rostry.core.common.model.PaymentMethod> getEntries() {
        return null;
    }
}
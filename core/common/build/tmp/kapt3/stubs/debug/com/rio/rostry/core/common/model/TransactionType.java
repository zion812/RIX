package com.rio.rostry.core.common.model;

/**
 * Types of coin transactions
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\t\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\t\u00a8\u0006\n"}, d2 = {"Lcom/rio/rostry/core/common/model/TransactionType;", "", "(Ljava/lang/String;I)V", "COIN_PURCHASE", "MARKETPLACE_LISTING", "PREMIUM_FEATURE", "TRANSFER_FEE", "REFUND", "BONUS", "PENALTY", "common_debug"})
public enum TransactionType {
    /*public static final*/ COIN_PURCHASE /* = new COIN_PURCHASE() */,
    /*public static final*/ MARKETPLACE_LISTING /* = new MARKETPLACE_LISTING() */,
    /*public static final*/ PREMIUM_FEATURE /* = new PREMIUM_FEATURE() */,
    /*public static final*/ TRANSFER_FEE /* = new TRANSFER_FEE() */,
    /*public static final*/ REFUND /* = new REFUND() */,
    /*public static final*/ BONUS /* = new BONUS() */,
    /*public static final*/ PENALTY /* = new PENALTY() */;
    
    TransactionType() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.rio.rostry.core.common.model.TransactionType> getEntries() {
        return null;
    }
}
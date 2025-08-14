package com.rio.rostry.shared.domain.model;

/**
 * Transfer status enumeration
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\b\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\b\u00a8\u0006\t"}, d2 = {"Lcom/rio/rostry/shared/domain/model/TransferStatus;", "", "(Ljava/lang/String;I)V", "PENDING", "APPROVED", "IN_TRANSIT", "COMPLETED", "CANCELLED", "DISPUTED", "common_debug"})
public enum TransferStatus {
    /*public static final*/ PENDING /* = new PENDING() */,
    /*public static final*/ APPROVED /* = new APPROVED() */,
    /*public static final*/ IN_TRANSIT /* = new IN_TRANSIT() */,
    /*public static final*/ COMPLETED /* = new COMPLETED() */,
    /*public static final*/ CANCELLED /* = new CANCELLED() */,
    /*public static final*/ DISPUTED /* = new DISPUTED() */;
    
    TransferStatus() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.rio.rostry.shared.domain.model.TransferStatus> getEntries() {
        return null;
    }
}
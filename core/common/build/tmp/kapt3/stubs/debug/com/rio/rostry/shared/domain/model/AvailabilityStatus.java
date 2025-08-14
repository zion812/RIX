package com.rio.rostry.shared.domain.model;

/**
 * Availability status enumeration
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0007\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007\u00a8\u0006\b"}, d2 = {"Lcom/rio/rostry/shared/domain/model/AvailabilityStatus;", "", "(Ljava/lang/String;I)V", "AVAILABLE", "RESERVED", "SOLD", "NOT_FOR_SALE", "BREEDING", "common_debug"})
public enum AvailabilityStatus {
    /*public static final*/ AVAILABLE /* = new AVAILABLE() */,
    /*public static final*/ RESERVED /* = new RESERVED() */,
    /*public static final*/ SOLD /* = new SOLD() */,
    /*public static final*/ NOT_FOR_SALE /* = new NOT_FOR_SALE() */,
    /*public static final*/ BREEDING /* = new BREEDING() */;
    
    AvailabilityStatus() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.rio.rostry.shared.domain.model.AvailabilityStatus> getEntries() {
        return null;
    }
}
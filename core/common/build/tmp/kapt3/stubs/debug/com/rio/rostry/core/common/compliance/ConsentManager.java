package com.rio.rostry.core.common.compliance;

/**
 * Minimal consent/data-retention manager for RBI/GDPR compliance flows.
 * Stores user decisions locally; upstream sync can be added via repository.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\u0018\u0000 \u000e2\u00020\u0001:\u0001\u000eB\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0007\u001a\u00020\bJ\u0006\u0010\t\u001a\u00020\bJ\u000e\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\bJ\u000e\u0010\r\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\bR\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/rio/rostry/core/common/compliance/ConsentManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "prefs", "Landroid/content/SharedPreferences;", "isConsentAccepted", "", "isDataRetentionAccepted", "setConsentAccepted", "", "accepted", "setDataRetentionAccepted", "Companion", "common_debug"})
public final class ConsentManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.SharedPreferences prefs = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS_NAME = "com.rio.rostry.compliance";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_CONSENT_ACCEPTED = "consent_accepted";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_DATA_RETENTION_ACCEPTED = "data_retention_accepted";
    @org.jetbrains.annotations.NotNull()
    public static final com.rio.rostry.core.common.compliance.ConsentManager.Companion Companion = null;
    
    public ConsentManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    public final boolean isConsentAccepted() {
        return false;
    }
    
    public final void setConsentAccepted(boolean accepted) {
    }
    
    public final boolean isDataRetentionAccepted() {
        return false;
    }
    
    public final void setDataRetentionAccepted(boolean accepted) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/rio/rostry/core/common/compliance/ConsentManager$Companion;", "", "()V", "KEY_CONSENT_ACCEPTED", "", "KEY_DATA_RETENTION_ACCEPTED", "PREFS_NAME", "common_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}
package com.rio.rostry.core.common.compliance

import android.content.Context
import android.content.SharedPreferences

/**
 * Minimal consent/data-retention manager for RBI/GDPR compliance flows.
 * Stores user decisions locally; upstream sync can be added via repository.
 */
class ConsentManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isConsentAccepted(): Boolean =
        prefs.getBoolean(KEY_CONSENT_ACCEPTED, false)

    fun setConsentAccepted(accepted: Boolean) {
        prefs.edit().putBoolean(KEY_CONSENT_ACCEPTED, accepted).apply()
    }

    fun isDataRetentionAccepted(): Boolean =
        prefs.getBoolean(KEY_DATA_RETENTION_ACCEPTED, false)

    fun setDataRetentionAccepted(accepted: Boolean) {
        prefs.edit().putBoolean(KEY_DATA_RETENTION_ACCEPTED, accepted).apply()
    }

    companion object {
        private const val PREFS_NAME = "com.rio.rostry.compliance"
        private const val KEY_CONSENT_ACCEPTED = "consent_accepted"
        private const val KEY_DATA_RETENTION_ACCEPTED = "data_retention_accepted"
    }
}

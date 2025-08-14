package com.rio.rostry.core.fieldtesting

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simplified Field Testing Manager for ROSTRY deployment
 * Handles basic analytics, configuration, and monitoring for rural field testing
 */
@Singleton
class FieldTestingManager @Inject constructor(
    private val context: Context
) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "rostry_field_testing", Context.MODE_PRIVATE
    )
    
    private val logFile = File(context.filesDir, "field_testing_log.txt")
    private val scope = CoroutineScope(Dispatchers.IO)
    
    companion object {
        private const val TAG = "FieldTesting"
        private const val PARTICIPANT_ID_KEY = "participant_id"
        private const val SESSION_START_KEY = "session_start"
        private const val FEATURE_USAGE_PREFIX = "feature_usage_"
        private const val ERROR_COUNT_KEY = "error_count"
    }
    
    init {
        initializeFieldTesting()
    }
    
    private fun initializeFieldTesting() {
        // Generate participant ID if not exists
        if (!prefs.contains(PARTICIPANT_ID_KEY)) {
            val participantId = "PILOT_${System.currentTimeMillis()}"
            prefs.edit().putString(PARTICIPANT_ID_KEY, participantId).apply()
            logEvent("FIELD_TESTING_INITIALIZED", "participant_id=$participantId")
        }
        
        // Mark session start
        prefs.edit().putLong(SESSION_START_KEY, System.currentTimeMillis()).apply()
        logEvent("SESSION_STARTED", "timestamp=${System.currentTimeMillis()}")
    }
    
    /**
     * Track user actions for field testing analysis
     */
    fun trackUserAction(action: String, details: String = "") {
        scope.launch {
            val timestamp = System.currentTimeMillis()
            val participantId = getParticipantId()
            
            // Log to file
            logEvent("USER_ACTION", "action=$action, details=$details, participant=$participantId")
            
            // Update feature usage counter
            val usageKey = "$FEATURE_USAGE_PREFIX$action"
            val currentCount = prefs.getInt(usageKey, 0)
            prefs.edit().putInt(usageKey, currentCount + 1).apply()
            
            // Log to Android Log for debugging
            Log.d(TAG, "User Action: $action - $details")
        }
    }
    
    /**
     * Track errors for debugging and improvement
     */
    fun trackError(error: String, context: String = "") {
        scope.launch {
            val timestamp = System.currentTimeMillis()
            val participantId = getParticipantId()
            
            // Log error
            logEvent("ERROR", "error=$error, context=$context, participant=$participantId")
            
            // Increment error count
            val errorCount = prefs.getInt(ERROR_COUNT_KEY, 0) + 1
            prefs.edit().putInt(ERROR_COUNT_KEY, errorCount).apply()
            
            // Log to Android Log
            Log.e(TAG, "Error tracked: $error in $context")
        }
    }
    
    /**
     * Track performance metrics
     */
    fun trackPerformance(metric: String, value: Long, unit: String = "ms") {
        scope.launch {
            val participantId = getParticipantId()
            logEvent("PERFORMANCE", "metric=$metric, value=$value$unit, participant=$participantId")
            Log.d(TAG, "Performance: $metric = $value$unit")
        }
    }
    
    /**
     * Track network events for rural optimization
     */
    fun trackNetworkEvent(event: String, details: String = "") {
        scope.launch {
            val participantId = getParticipantId()
            logEvent("NETWORK", "event=$event, details=$details, participant=$participantId")
            Log.d(TAG, "Network: $event - $details")
        }
    }
    
    /**
     * Get participant ID for this field testing session
     */
    fun getParticipantId(): String {
        return prefs.getString(PARTICIPANT_ID_KEY, "UNKNOWN") ?: "UNKNOWN"
    }
    
    /**
     * Get session duration in minutes
     */
    fun getSessionDuration(): Long {
        val sessionStart = prefs.getLong(SESSION_START_KEY, System.currentTimeMillis())
        return (System.currentTimeMillis() - sessionStart) / (1000 * 60) // Convert to minutes
    }
    
    /**
     * Get feature usage statistics
     */
    fun getFeatureUsageStats(): Map<String, Int> {
        val stats = mutableMapOf<String, Int>()
        prefs.all.forEach { (key, value) ->
            if (key.startsWith(FEATURE_USAGE_PREFIX) && value is Int) {
                val feature = key.removePrefix(FEATURE_USAGE_PREFIX)
                stats[feature] = value
            }
        }
        return stats
    }
    
    /**
     * Get total error count
     */
    fun getErrorCount(): Int {
        return prefs.getInt(ERROR_COUNT_KEY, 0)
    }
    
    /**
     * Export field testing data for analysis
     */
    fun exportFieldTestingData(): String {
        val participantId = getParticipantId()
        val sessionDuration = getSessionDuration()
        val featureUsage = getFeatureUsageStats()
        val errorCount = getErrorCount()
        
        val report = StringBuilder()
        report.appendLine("=== ROSTRY Field Testing Report ===")
        report.appendLine("Participant ID: $participantId")
        report.appendLine("Session Duration: $sessionDuration minutes")
        report.appendLine("Total Errors: $errorCount")
        report.appendLine("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
        report.appendLine()
        
        report.appendLine("Feature Usage:")
        featureUsage.forEach { (feature, count) ->
            report.appendLine("  $feature: $count times")
        }
        report.appendLine()
        
        // Add recent log entries
        report.appendLine("Recent Activity Log:")
        try {
            if (logFile.exists()) {
                val logLines = logFile.readLines().takeLast(50) // Last 50 entries
                logLines.forEach { line ->
                    report.appendLine("  $line")
                }
            }
        } catch (e: Exception) {
            report.appendLine("  Error reading log file: ${e.message}")
        }
        
        return report.toString()
    }
    
    /**
     * Clear all field testing data
     */
    fun clearFieldTestingData() {
        scope.launch {
            // Clear preferences
            prefs.edit().clear().apply()
            
            // Clear log file
            try {
                if (logFile.exists()) {
                    logFile.delete()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing log file", e)
            }
            
            // Reinitialize
            initializeFieldTesting()
            
            Log.d(TAG, "Field testing data cleared and reinitialized")
        }
    }
    
    /**
     * Check if field testing is active
     */
    fun isFieldTestingActive(): Boolean {
        return prefs.contains(PARTICIPANT_ID_KEY)
    }
    
    /**
     * Get field testing summary for quick overview
     */
    fun getFieldTestingSummary(): FieldTestingSummary {
        return FieldTestingSummary(
            participantId = getParticipantId(),
            sessionDurationMinutes = getSessionDuration(),
            totalFeatureUsage = getFeatureUsageStats().values.sum(),
            totalErrors = getErrorCount(),
            isActive = isFieldTestingActive()
        )
    }
    
    private fun logEvent(type: String, details: String) {
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val logEntry = "$timestamp [$type] $details\n"
            
            logFile.appendText(logEntry)
            
            // Keep log file size manageable (max 1MB)
            if (logFile.length() > 1024 * 1024) {
                val lines = logFile.readLines()
                val keepLines = lines.takeLast(1000) // Keep last 1000 lines
                logFile.writeText(keepLines.joinToString("\n") + "\n")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error writing to log file", e)
        }
    }
}

/**
 * Data class for field testing summary
 */
data class FieldTestingSummary(
    val participantId: String,
    val sessionDurationMinutes: Long,
    val totalFeatureUsage: Int,
    val totalErrors: Int,
    val isActive: Boolean
)

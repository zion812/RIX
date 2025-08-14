package com.rio.rostry

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for ROSTRY platform
 * Initializes Firebase and other global configurations
 */
@HiltAndroidApp
class ROSTRYApplication : Application() {

    companion object {
        private const val TAG = "ROSTRYApplication"
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        initializeFirebase()

        // Configure Firestore for offline persistence
        configureFirestore()

        // Initialize Firebase App Check (Play Integrity)
        initializeAppCheck()

        // Start startup performance trace
        startStartupTrace()

        Log.d(TAG, "ROSTRY Application initialized successfully")
    }

    private fun initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this)
            Log.d(TAG, "Firebase initialized successfully")

            // Log current Firebase project info (for debugging)
            val firebaseApp = FirebaseApp.getInstance()
            Log.d(TAG, "Firebase Project ID: ${firebaseApp.options.projectId}")
            Log.d(TAG, "Firebase App ID: ${firebaseApp.options.applicationId}")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase", e)
        }
    }

    private fun configureFirestore() {
        try {
            val firestore = FirebaseFirestore.getInstance()

            // Enable offline persistence for rural network conditions
            firestore.firestoreSettings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(com.google.firebase.firestore.FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()

            Log.d(TAG, "Firestore configured with offline persistence")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to configure Firestore", e)
        }
    }

    private fun initializeAppCheck() {
        try {
            val appCheck = FirebaseAppCheck.getInstance()
            appCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())
            Log.d(TAG, "Firebase App Check initialized (Play Integrity)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase App Check", e)
        }
    }

    private fun startStartupTrace() {
        try {
            val trace: Trace = FirebasePerformance.getInstance().newTrace("app_startup")
            trace.start()
            // Stop this trace in the first activity frame using a broadcast marker
            // We'll post a sticky intent that MainActivity will observe
            sendBroadcast(android.content.Intent("com.rio.rostry.STARTUP_TRACE_STARTED"))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start startup trace", e)
        }
    }
}

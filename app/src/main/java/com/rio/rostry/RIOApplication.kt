package com.rio.rostry

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Application class for RIO platform
 * Initializes Firebase and other global configurations
 */
class RIOApplication : Application() {

    companion object {
        private const val TAG = "RIOApplication"
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        initializeFirebase()

        // Configure Firestore for offline persistence
        configureFirestore()

        Log.d(TAG, "RIO Application initialized successfully")
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
}
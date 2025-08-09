# Firebase Console Setup Guide for RIO Rooster Community Platform

## Overview
This guide provides step-by-step instructions for setting up Firebase Authentication with custom claims for the RIO platform's 3-tier user hierarchy targeting rural farmers and urban enthusiasts in Andhra Pradesh/Telangana.

## Prerequisites
- Google account with access to Firebase Console
- Android Studio project (RIO) ready for integration
- Basic understanding of Firebase services

## Step 1: Create Firebase Project

1. **Navigate to Firebase Console**
   - Go to [https://console.firebase.google.com/](https://console.firebase.google.com/)
   - Sign in with your Google account

2. **Create New Project**
   - Click "Create a project"
   - Project name: `RIO-Rooster-Community`
   - Project ID: `rio-rooster-community` (or auto-generated)
   - Enable Google Analytics (recommended for user behavior insights)
   - Select Analytics location: India
   - Accept terms and create project

## Step 2: Configure Authentication

1. **Enable Authentication**
   - In Firebase Console, navigate to "Authentication"
   - Click "Get started"

2. **Configure Sign-in Methods**

   **Email/Password:**
   - Go to "Sign-in method" tab
   - Click "Email/Password"
   - Enable "Email/Password"
   - Enable "Email link (passwordless sign-in)" for better UX
   - Save

   **Phone Authentication:**
   - Click "Phone" in sign-in providers
   - Enable phone authentication
   - Add test phone numbers for development:
     - +91 9876543210 (verification code: 123456)
     - +91 8765432109 (verification code: 654321)
   - Configure reCAPTCHA settings for production
   - Save

3. **Configure Authorized Domains**
   - Add your production domain when ready
   - Keep localhost for development

## Step 3: Set Up Firestore Database

1. **Create Firestore Database**
   - Navigate to "Firestore Database"
   - Click "Create database"
   - Start in test mode (we'll add security rules later)
   - Choose location: asia-south1 (Mumbai) for better performance in India
   - Create database

2. **Initial Collections Structure**
   ```
   users/
   ├── {userId}/
   │   ├── profile: object
   │   ├── tier: string
   │   ├── verificationStatus: object
   │   └── createdAt: timestamp

   fowlListings/
   ├── {listingId}/
   │   ├── ownerId: string
   │   ├── details: object
   │   └── permissions: object

   verificationRequests/
   ├── {requestId}/
   │   ├── userId: string
   │   ├── requestedTier: string
   │   ├── documents: array
   │   └── status: string
   ```

## Step 4: Configure Cloud Functions

1. **Enable Cloud Functions**
   - Navigate to "Functions" in Firebase Console
   - Click "Get started"
   - Choose your preferred region: asia-south1

2. **Billing Setup**
   - Upgrade to Blaze plan (pay-as-you-go) for Cloud Functions
   - Set up billing alerts for cost management

## Step 5: Add Android App

1. **Register Android App**
   - Click "Add app" → Android icon
   - Android package name: `com.rio.rostry`
   - App nickname: `RIO Android`
   - Debug signing certificate SHA-1: (get from Android Studio)
   - Register app

2. **Download Configuration File**
   - Download `google-services.json`
   - Place in `app/` directory of your Android project

## Step 6: Configure Custom Claims

Custom claims will be managed through Cloud Functions. The structure will be:

```json
{
  "tier": "general|farmer|enthusiast",
  "permissions": {
    "canCreateListings": boolean,
    "canAccessAnalytics": boolean,
    "canManageBreedingRecords": boolean,
    "canAccessPremiumFeatures": boolean
  },
  "verificationLevel": "basic|enhanced|premium",
  "region": "andhra_pradesh|telangana",
  "language": "en|te|hi"
}
```

## Step 7: Security Configuration

1. **App Check (Recommended)**
   - Navigate to "App Check"
   - Register your Android app
   - Enable Play Integrity API for production
   - Use debug tokens for development

2. **Identity and Access Management (IAM)**
   - Set up service accounts for Cloud Functions
   - Configure appropriate permissions for user management

## Step 8: Regional Optimization

1. **Performance Monitoring**
   - Enable Performance Monitoring for network insights
   - Monitor performance in rural areas

2. **Crashlytics**
   - Enable Crashlytics for error tracking
   - Important for debugging issues in low-connectivity areas

## Next Steps

After completing this setup:
1. Implement custom claims Cloud Functions
2. Configure Firestore security rules
3. Integrate Firebase SDK in Android app
4. Implement verification workflows
5. Add multi-language support

## Important Notes for Rural Deployment

- **Offline Support**: Configure Firestore for offline persistence
- **Network Optimization**: Use Firebase Performance Monitoring to track network issues
- **Data Usage**: Implement data compression and caching strategies
- **Regional Compliance**: Ensure compliance with Indian data protection laws

## Support Resources

- Firebase Documentation: https://firebase.google.com/docs
- Android Integration Guide: https://firebase.google.com/docs/android/setup
- Custom Claims Documentation: https://firebase.google.com/docs/auth/admin/custom-claims
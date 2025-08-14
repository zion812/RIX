# RIO Android Application Flow (2025)

This document explains end-to-end user journeys, feature workflows, and system interactions in the current RIO Android app implementation.

## Overview
- Platform: Android (Kotlin + Jetpack Compose)
- Backend: Firebase (Auth, Firestore, Functions)
- Architecture: Modular Clean Architecture with MVVM; offline-first via Room + Firestore cache
- Current modules in use (settings.gradle.kts): app, core:common, core:data, core:database-simple, core:analytics

## Primary User Journeys

### 1) App Launch and Shell
- Entry points
  - Application: app/src/main/java/com/rio/rostry/RIOApplication.kt (initializes Firebase; enables Firestore offline cache)
  - Activity: app/src/main/java/com/rio/rostry/MainActivity.kt
- Main Composable: RIOPhase1App() inside ROSTRYTheme
- Shell UI: Phase5MainContent() offers navigation buttons to test flows and feature stubs

### 2) Authentication Flow
- Manager: app/src/main/java/com/rio/rostry/auth/FirebaseAuthManager.kt
  - Tracks currentUser (StateFlow), userClaims (custom claims), isLoading
  - Supports: signInWithEmailAndPassword, createUserWithEmailAndPassword, sendEmailVerification, password reset, signOut
  - Tier upgrade: requestTierUpgrade() via Firebase Functions callable
- UI Entrypoints
  - AuthenticationScreen + AuthScreen (Compose)
  - MainActivity toggles to AuthenticationScreen via button
- On successful login: claims refresh; Main UI shows authenticated state and allows logout

### 3) Tiered Onboarding (General → Farmer → Enthusiast)
- Custom claims encode tier and permissions (see firebase-functions/index.js → getDefaultClaims)
- Upgrade path (user): Authentication UI triggers Firebase Functions request
- Review path (admin): processVerificationRequest callable (server-side)
- Client gating
  - FirebaseAuthManager.hasPermission() helpers (e.g., canCreateListings)
  - ViewModels should read tier/permissions to enable/disable features

### 4) Fowl Registration & Management
- Repository: core/data/.../FowlRepositoryImpl.kt
  - Offline-first: Room cache (FowlDao) preferred, Firestore as source of truth
  - Methods: getFowlById, getFowlsByOwner(+Flow), getFowlsForSale(+Flow), getFowlsByLocation/Breed, save/update/delete
- Entities/DAO: core/database-simple/.../entities/FowlEntity.kt, dao/FowlDao.kt
- UI: app/src/main/java/com/rio/rostry/ui/fowl/SimpleFowlManagementScreen (test/simplified)
- Sync pattern
  - Write-through: local insert/update + best-effort Firestore sync
  - Read path: local-first, server fallback; cache on success

### 5) User Profile
- Repository: core/data/.../UserRepositoryImpl.kt
  - Holds currentUserId/tier StateFlows and auth status flags (client-side)
  - Offline-first getUserById, updateUserProfile, searchUsers, getUsersByTier, getUsersByLocation
- Entities/DAO: core/database-simple/.../entities/UserEntity.kt, dao/UserDao.kt
- UI: app/src/main/java/com/rio/rostry/ui/profile/UserProfileScreen (basic)

### 6) Marketplace (Scaffolded)
- Paths exist (features/marketplace module and app/ui/marketplace), but feature modules are disabled in settings for now; UI stubs may exist for demos
- Permissions are tier-gated via claims when enabled

### 7) Payments (Coin Economy – Scaffolded)
- Design: docs/coin-economy-design.md; demo guide in docs/demo-payment-gateway-guide.md
- Core/payment module exists but is currently disabled; coin purchase and spends are planned to use Functions with Razorpay/UPI

### 8) Navigation
- Current app uses a minimal button-based shell in MainActivity
- Navigation Compose dependencies are present; a fuller NavHost scaffold exists at app/ui/navigation/RIONavigation.kt.disabled (tier-based routing planned)

### 9) Offline-First Behavior
- Firestore configured with persistence enabled (RIOApplication)
- Room database used for user/fowl caching via core:database-simple
- Strategy per repository
  - Local-first reads; server fallback
  - Writes update local then attempt remote
  - Errors in remote sync do not block local UX; background reconciliation expected in future sync module

### 10) Error and Loading States
- BaseViewModel in core:common provides UiState and error handling helpers
- Note: BaseViewModel expects DI-injected UserRepository/ErrorHandler; app-level Hilt integration is disabled, so feature ViewModels should provide explicit constructors or use manual providers until Hilt is re-enabled

## System Interactions

- Firebase Auth ↔ Android: auth state listener updates StateFlows; ID token refresh to get latest claims
- Firebase Functions: callable endpoints for tier upgrades and counters (see firebase-functions/index.js)
- Firestore: collections users, fowls used by repositories; offline cache enabled; basic query patterns implemented
- Room: RIODatabase with UserEntity and FowlEntity; DAOs supply both suspend and Flow APIs

## Key Files Reference
- App shell: app/src/main/java/com/rio/rostry/MainActivity.kt
- Auth: app/src/main/java/com/rio/rostry/auth/FirebaseAuthManager.kt
- DB provider: core/database-simple/.../di/DatabaseModule.kt (DatabaseProvider)
- User repo: core/data/.../repository/UserRepositoryImpl.kt
- Fowl repo: core/data/.../repository/FowlRepositoryImpl.kt

## Known Gaps and Next Steps
- Hilt DI not enabled in app module; BaseViewModel has @Inject fields that won’t be populated → re-enable Hilt or refactor BaseViewModel dependencies
- Feature modules (:features:*) disabled; restore incrementally starting with :features:fowl
- Navigation: move from shell buttons to Navigation Compose NavHost (see RIONavigation.kt.disabled)
- Payments: wire up core:payment and Razorpay/UPI; ensure offline queue for spends
- Expand Room schema if moving beyond simplified database; plan migrations


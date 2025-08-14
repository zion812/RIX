# ROSTRY Android Technical Blueprint (2025)

This blueprint summarizes the architecture, components, data flows, and dependencies of the current ROSTRY Android app.

## 1. Architecture Overview
- Pattern: Clean Architecture + MVVM
- Layers
  - Presentation: Compose UI, Activities, ViewModels (some base helpers in core:common)
  - Data: Repositories (core:data) with Room (core:database-simple) + Firestore
  - Platform Services: Firebase (Auth, Firestore, Functions), Analytics/Crashlytics (enabled in app Gradle)
- DI: Hilt present in version catalog; app-level Hilt plugin currently disabled. Database provided via manual singleton (DatabaseProvider)

## 2. Modules and Responsibilities

- :app
  - MainActivity, ROSTRYApplication, Compose theme and UI stubs
  - Auth screens, profile, test/demo screens
  - Depends on core:common, core:data, core:database-simple, core:analytics

- :core:common
  - BaseViewModel, error handling, network/perf utilities, simple UI helpers
  - Note: BaseViewModel injects UserRepository and ErrorHandler via @Inject; requires Hilt if used directly

- :core:data
  - Firebase + Room repositories (UserRepositoryImpl, FowlRepositoryImpl)
  - Disabled implementations for other domains are stubbed (*.disabled)

- :core:database-simple
  - ROSTRYDatabase, DAOs (UserDao, FowlDao), entities, type converters
  - DatabaseProvider singleton for non-Hilt construction

- :core:analytics
  - Analytics scaffolding (Firebase Analytics dependencies configured in app)

- :features:* (present but disabled in settings)
  - Planned feature modules for fowl, marketplace, familytree, chat, user, etc.

## 3. Data Flow
- Offline-first
  - Reads: DAO local-first; fetch from Firestore if missing; cache results
  - Writes: persist locally; enqueue best-effort Firestore set/update/delete
- Authentication
  - FirebaseAuthManager tracks currentUser and custom claims (UserClaims)
  - Functions callable for requestTierUpgrade and admin processing
- Sync
  - Basic read-through/write-through present; advanced background sync via core:sync is planned but disabled

## 4. Navigation and UI
- Compose UI with Material 3
- Current shell: Phase5MainContent in MainActivity with buttons
- Planned Navigation Compose graph exists in app/src/main/java/.../navigation/ROSTRYNavigation.kt for tier-based routing

## 5. Dependencies and Build
- Gradle Version Catalog (gradle/libs.versions.toml)
  - Kotlin 2.0.21, AGP 8.11.1
  - Compose BOM 2024.09.00; Material3
  - Firebase BOM 33.7.0 (Auth, Firestore, Functions, Analytics, Crashlytics, Perf)
  - Room 2.6.1
  - Navigation Compose 2.8.5
- app/build.gradle.kts
  - compose=true, minSdk 24, target/compile 36
  - Firebase plugins (google-services, crashlytics, perf)
  - Hilt plugin commented out

## 6. Storage Schema (Simplified)
- Room
  - UserEntity(id, displayName, email, phoneNumber, tier, photoUrl, region, district, language, verification flags, createdAt/updatedAt/lastLoginAt)
  - FowlEntity(id, ownerId, name, breed, gender, dates, attributes, market flags, location, createdAt/updatedAt)
- Firestore
  - users collection; fowls collection; verificationRequests; counters; notifications (see firebase-functions/index.js)

## 7. Security & Permissions
- Custom claims encode: tier, permissions, verificationStatus, limits, profile metadata
- Firestore rules referenced in docs/firestore-security-rules.js
- Client gating via FirebaseAuthManager.hasPermission() and tier checks in ViewModels/UI

## 8. Observed Inconsistencies to Address
- Docs state Navigation Component (XML/ViewBinding) in some places; app uses Compose + Navigation Compose
- Some docs assume Hilt end-to-end; code currently disables Hilt in :app and uses manual providers
- Multiple modules in docs may not be enabled in settings.gradle.kts (e.g., core:network, core:payment)
- BaseViewModel depends on DI; ensure feature VMs wire dependencies without Hilt until re-enabled

## 9. Roadmap Recommendations
- Re-enable Hilt progressively (Application @HiltAndroidApp, module bindings for DAOs, Firestore, repositories)
- Migrate shell buttons to Navigation Compose NavHost using ROSTRYNavigation scaffold
- Re-introduce feature modules starting with :features:fowl; keep compile surface minimal
- Expand Room schema and add migrations when enabling more features; remove fallbackToDestructiveMigration for prod
- Add WorkManager-based sync (core:sync) once stabilized; condition by network quality for rural contexts

## 10. Testing
- Unit: repositories with in-memory Room, coroutine test, Truth/Mockito
- UI: Compose UI test (ui-test-junit4) for Auth, Profile, Fowl screens
- Commands: ./gradlew testDebugUnitTest, connectedDebugAndroidTest


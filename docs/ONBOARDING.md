> **Deprecated**: See the canonical onboarding at [developer-onboarding-guide.md](./developer-onboarding-guide.md).

# RIO Android Project – Developer Onboarding (2025)

## 1) Project Overview

- **Purpose**: Android app for rooster/fowl traceability and marketplace targeting rural farmers in Andhra Pradesh/Telangana
- **User Tiers**: General (free) → Farmer (₹500/year) → Enthusiast (₹2000/year) with progressive feature unlocking
- **Tech Stack**: Kotlin 2.0.21, Jetpack Compose + Material 3, Navigation Compose, Firebase (Auth/Firestore/Functions), Room (offline-first)
- **Architecture**: Clean Architecture + MVVM with progressive module enablement

## 2) Current Module Status

**Active Modules** (settings.gradle.kts):
- `:app` - Main application with Compose UI and auth screens
- `:core:common` - BaseViewModel, utilities, error handling
- `:core:data` - Repository implementations (User, Fowl)
- `:core:database-simple` - Room DB with simplified schema
- `:core:analytics` - Firebase Analytics integration

**Planned Modules** (disabled for progressive rollout):
- `:core:network`, `:core:payment`, `:core:notifications`, `:core:sync`, `:core:media`
- `:features:fowl`, `:features:marketplace`, `:features:familytree`, `:features:chat`, `:features:user`

**Key Entry Points**:
- Application: `app/src/main/java/com/rio/rostry/RIOApplication.kt`
- Main Activity: `app/src/main/java/com/rio/rostry/MainActivity.kt`
- Navigation: `app/src/main/java/com/rio/rostry/navigation/RIONavigation.kt.disabled` (scaffold ready)
- Auth Manager: `app/src/main/java/com/rio/rostry/auth/FirebaseAuthManager.kt`

## 3) Architecture & Patterns

- Clean Architecture (Presentation, Domain, Data). MVVM in features.
- Presentation: Compose UI + ViewModels (StateFlow for state, BaseViewModel for loading/error/tier checks).
- Domain: Use cases & domain models (e.g., features/fowl/domain/... ).
- Data: Repositories & data sources using Room + Firestore; Cloud Functions for server logic.
- Offline-first: Firestore offline persistence + Room as local cache; NetworkStateManager guides sync strategy, batching, timeouts, image quality.

## 4) Key Components

- RIOApplication (@HiltAndroidApp): initializes Firebase, configures Firestore offline cache.
- MainActivity: sets Compose content; toggles FirebaseTestScreen.
- RIONavigation: chooses start destination based on FirebaseAuthManager user + custom claims, routes to dashboards (tier-based) and feature screens (some TODOs).
- FirebaseAuthManager: wraps FirebaseAuth; StateFlows for currentUser, claims, isLoading; sign-in/up, email verification, reset; claims refresh via getIdToken(true); requestTierUpgrade via Functions; permission helpers.
- BaseViewModel (core/common): common coroutine handling, UI state, retries, tier checks.
- NetworkStateManager (core/network): monitors connectivity/quality; exposes sync strategy, batch size, request timeouts, image quality; indicates metered connections.
- Room DBs (core/database): RIODatabase (v1) and RIOLocalDatabase (v3) with extensive entities for fowls, marketplace, transfers, messages, notifications, coins, caches; converters and migrations.
- PaymentManager (core/payment): coin purchase with Razorpay (checkout), UPI/GPay/PhonePe/Paytm placeholders; offline queue for spends; local-first balance; Cloud Functions: createCoinPurchaseOrder, verifyPaymentAndCreditCoins, createUPIPaymentLink, processGooglePayPayment.
- FowlManagementViewModel (features/fowl): registration, search, update, photo upload (use cases), QR generation, pagination; multiple StateFlows for UI state.

## 5) Data Flow

- Auth: AuthScreen -> FirebaseAuthManager.signIn/create -> FirebaseAuth listener -> claims refresh -> RIONavigation picks tier dashboard.
- Feature flow (Fowl): UI events -> ViewModel (BaseViewModel utilities) -> Use cases -> Repos -> Room/Firestore; state flows update Compose UI.
- Offline-first: local Room used as source of truth; Firestore offline enabled; PaymentManager queues offline spends; NetworkStateManager adapts behavior.

## 6) Database

Room (core/database):
- RIODatabase (v1) entities include: UserEntity, CoinTransactionEntity, UserCoinBalanceEntity, FowlEntity, MarketplaceListingEntity, TransferEntity, MessageEntity, NotificationEntity, SyncQueueEntity; converters for Date, lists, enums.
- RIOLocalDatabase (v3): broader local/offline schema (coin_orders, coin_packages, refund_requests, disputes, notification tables, caches, offline actions) with explicit MIGRATION_1_2 and MIGRATION_2_3.
- Entities have indices/foreign keys (e.g., RoosterEntity with ownerId, fatherId, motherId FKs).

Firestore (docs/firestore-schema.md):
- Collections: users, fowls (+ health_records/photos/lineage), transfers (+ documents/verification_steps), marketplace (+ bids/watchers), breeding_records, verification_requests, notifications, conversations/messages/participants.

## 7) External Integrations

- Firebase: Auth, Firestore, Functions, Analytics, Crashlytics, Perf. Cloud Functions used for tier upgrades and payments.
- Razorpay: checkout integrated; demo key in code; replace with secure prod keys.
- Retrofit/OkHttp: available for external APIs where needed (core/network, core/payment).

## 8) Build & Dependencies

- Version catalog centralizes major libs (Compose BOM 2024.09.00, Firebase BOM 33.7.0, Navigation 2.8.5).
- app/build.gradle.kts: Compose enabled, Hilt, Firebase BOM libs, Navigation, Room, Gson; depends on core and feature modules.
- Note: Ensure settings.gradle.kts includes all modules referenced by app (e.g., :core:data if used).

## 9) Local Environment Setup

1) Prereqs: Android Studio Hedgehog+, JDK 17 (project modules target Java 11/1.8, Gradle JDK 17 ok), Android SDKs.
2) Firebase: follow docs/firebase-setup-guide.md; create project, register Android app (package: com.rio.rostry), place google-services.json in app/.
3) Gradle sync; verify module includes match dependencies.
4) Run app; use the FAB to open FirebaseTestScreen and validate Firebase connectivity (Auth + Firestore test calls).

## 10) Coding Standards

- Kotlin idiomatic, Compose best practices (state hoisting, previews optional).
- MVVM: ViewModels expose StateFlows; UI collects with collectAsState; no business logic in Composables.
- Clean Architecture: UI -> ViewModel -> Use Cases -> Repos -> Data sources (Room/Firestore/Network).
- DI via Hilt: annotate Application, ViewModels (@HiltViewModel), inject dependencies; provide modules per feature/core as needed.
- Offline-first: prefer Room as source-of-truth; reconcile with Firestore; handle conflicts and sync via strategies.
- Errors: use BaseViewModel.handleError; surface user-friendly messages.
- Security: validate custom claims on server; client uses for UI gating only.

## 11) Testing

- Unit tests: JUnit4, Mockito/MockK (payments module uses Mockito), kotlinx-coroutines-test, androidx arch core-testing.
- Instrumented: Espresso, Compose testing (ui-test-junit4).
- Suggested targets:
  - FirebaseAuthManager (mock Firebase services where feasible).
  - ViewModels (FowlManagementViewModel) with fake repos.
  - DAOs for critical entities (roosters, transactions).
  - PaymentManager with fake Functions and local DB.
- Commands: ./gradlew test; ./gradlew connectedAndroidTest.

## 12) Deployment

- Environments: demo Razorpay config; replace with prod keys via secure BuildConfig/Gradle properties or remote config.
- Firestore: deploy rules and indexes per docs; enable App Check/Play Integrity for prod.
- Versioning: adjust versionCode/versionName in app defaultConfig.
- Release: set signing configs, enable minify with proguard, verify Crashlytics/Perf.
- CI/CD: add pipelines for lint, tests, assembleRelease (recommended).

## 13) Current State & Next Steps

**✅ Working Now**:
- Firebase Auth with 3-tier custom claims system
- Room database with offline-first repositories
- Basic Compose UI with authentication flows
- Manual dependency injection via providers

**🔄 Next Enablement Steps**:
- Re-enable Hilt DI: Uncomment plugin in app/build.gradle.kts, add @HiltAndroidApp
- Activate Navigation Compose: Enable RIONavigation.kt.disabled for tier-based routing
- Enable core:payment: Activate coin-based economy with Razorpay/UPI integration
- Progressive feature modules: Start with :features:fowl for full management

**⚠️ Current Limitations**:
- BaseViewModel uses @Inject fields; requires manual wiring until Hilt enabled
- Navigation uses button shell; tier-based NavHost scaffold ready but disabled
- Advanced features (marketplace, chat, payments) implemented but not activated

## 14) Onboarding Checklist

- Clone, open in Android Studio, ensure JDK 17.
- Include all referenced modules in settings.gradle.kts.
- Configure Firebase and add google-services.json.
- Build & run; validate Firebase via FirebaseTestScreen.
- Read docs/android-architecture-overview.md and docs/firestore-schema.md.
- Explore core/database entities and DAOs; map to Firestore schema.
- Review NetworkStateManager for sync/timeout guidance.
- For payments, use demo flow; set up Cloud Functions stubs.
- Add/Run tests for modules you’ll change.

## 15) Quick Links (paths)

- Application: app/src/main/java/com/rio/rostry/RIOApplication.kt
- Navigation: app/src/main/java/com/rio/rostry/navigation/RIONavigation.kt
- Auth: app/src/main/java/com/rio/rostry/auth/FirebaseAuthManager.kt
- Network: core/network/src/main/java/com/rio/rostry/core/network/NetworkStateManager.kt
- Room DBs: core/database/src/main/java/com/rio/rostry/core/database/RIODatabase.kt; RIOLocalDatabase.kt
- Payment: core/payment/src/main/java/com/rio/rostry/core/payment/PaymentManager.kt
- Fowl domain: features/fowl/src/main/java/com/rio/rostry/fowl/domain/model/Fowl.kt
- Fowl VM: features/fowl/src/main/java/com/rio/rostry/fowl/ui/viewmodels/FowlManagementViewModel.kt
- Docs root: docs/


# RIO Android MVVM Modular Architecture

## Overview

Comprehensive Android architecture for the RIO rooster traceability and marketplace platform, designed for 600K+ users across rural and urban India with varying network conditions and device capabilities.

## Architecture Principles

### 1. **Clean Architecture**
- **Presentation Layer**: UI components (Activities, Fragments, ViewModels)
- **Domain Layer**: Business logic (Use Cases, Domain Models)
- **Data Layer**: Data sources (Repositories, Local/Remote data sources)

### 2. **MVVM Pattern**
- **Model**: Data layer with repositories and data sources
- **View**: UI components (Fragments, Activities)
- **ViewModel**: Presentation logic with LiveData/StateFlow

### 3. **Modular Design**
- Feature-based modules for scalability
- Shared modules for common functionality
- Clear module dependencies and boundaries

## Module Structure

```
RIO/
├── app/                           # Main application module
├── core/                          # Core infrastructure modules
│   ├── common/                    # Shared utilities and extensions
│   ├── network/                   # Network configuration and interceptors
│   ├── database/                  # Room database and DAOs
│   ├── firebase/                  # Firebase service configurations
│   ├── ui/                        # Shared UI components and themes
│   └── navigation/                # Navigation utilities and deep linking
├── features/                      # Feature modules
│   ├── user/                      # User authentication and profile management
│   ├── fowl/                      # Fowl registration and management
│   ├── marketplace/               # Marketplace and trading functionality
│   └── chat/                      # Real-time messaging system
├── shared/                        # Shared business logic
│   ├── domain/                    # Shared domain models and use cases
│   ├── data/                      # Shared data models and repositories
│   └── utils/                     # Utility classes and helpers
└── buildSrc/                      # Build configuration and dependencies
```

## Feature Module Architecture

Each feature module follows the same internal structure:

```
feature-module/
├── src/main/java/com/rio/rostry/feature/
│   ├── ui/                        # UI layer
│   │   ├── fragments/             # Fragment implementations
│   │   ├── adapters/              # RecyclerView adapters
│   │   ├── viewmodels/            # ViewModels for the feature
│   │   └── dialogs/               # Custom dialogs
│   ├── domain/                    # Domain layer
│   │   ├── models/                # Domain models
│   │   ├── usecases/              # Business logic use cases
│   │   └── repositories/          # Repository interfaces
│   ├── data/                      # Data layer
│   │   ├── repositories/          # Repository implementations
│   │   ├── datasources/           # Local and remote data sources
│   │   ├── mappers/               # Data model mappers
│   │   └── models/                # Data transfer objects
│   └── di/                        # Dependency injection modules
├── src/main/res/                  # Resources
│   ├── layout/                    # XML layouts
│   ├── values/                    # Strings, colors, dimensions
│   └── navigation/                # Navigation graphs
└── build.gradle.kts               # Module build configuration
```

## Core Modules

### 1. **core:common**
- Base classes (BaseFragment, BaseViewModel, BaseRepository)
- Extension functions and utility classes
- Constants and configuration
- Error handling and result wrappers

### 2. **core:network**
- Retrofit configuration with Firebase integration
- Network interceptors for authentication and logging
- Connectivity monitoring and adaptive loading
- Request/response models for Firebase APIs

### 3. **core:database**
- Room database configuration
- Entity definitions for offline caching
- DAOs for local data access
- Database migrations and versioning

### 4. **core:firebase**
- Firebase service initialization and configuration
- Authentication service with custom claims
- Firestore service with offline persistence
- Storage service for media uploads
- Realtime Database for chat functionality

### 5. **core:ui**
- Shared UI components and custom views
- Material Design 3 theming with regional customization
- Common layouts and styles
- Animation and transition utilities

### 6. **core:navigation**
- Navigation component setup and configuration
- Deep linking handlers and URL parsing
- Inter-module navigation utilities
- Safe Args extensions

## Dependency Flow

```
┌─────────────────┐
│       app       │ ← Main application module
└─────────────────┘
         │
    ┌────┴────┐
    │ features │ ← Feature modules
    └─────────┘
         │
    ┌────┴────┐
    │ shared  │ ← Shared business logic
    └─────────┘
         │
    ┌────┴────┐
    │  core   │ ← Core infrastructure
    └─────────┘
```

## Technology Stack

### **Core Technologies**
- **Language**: Kotlin 1.9.0+
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Hilt
- **Asynchronous Programming**: Coroutines + Flow

### **UI Framework**
- **UI Toolkit**: Android Views with ViewBinding
- **Navigation**: Navigation Component
- **Design System**: Material Design 3
- **Image Loading**: Coil with network-aware caching

### **Data & Storage**
- **Remote Database**: Firebase Firestore
- **Local Database**: Room
- **Authentication**: Firebase Auth with custom claims
- **File Storage**: Firebase Storage
- **Real-time Data**: Firebase Realtime Database

### **Background Processing**
- **Work Scheduling**: WorkManager
- **Background Sync**: Custom sync workers
- **Notifications**: Firebase Cloud Messaging

### **Testing**
- **Unit Testing**: JUnit 5, MockK
- **Integration Testing**: Hilt testing
- **UI Testing**: Espresso with Fragment scenarios

## Module Dependencies

### **App Module Dependencies**
```kotlin
dependencies {
    // Feature modules
    implementation(project(":features:user"))
    implementation(project(":features:fowl"))
    implementation(project(":features:marketplace"))
    implementation(project(":features:chat"))
    
    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:ui"))
    implementation(project(":core:navigation"))
    
    // Dependency injection
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
}
```

### **Feature Module Dependencies**
```kotlin
dependencies {
    // Shared modules
    implementation(project(":shared:domain"))
    implementation(project(":shared:data"))
    
    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:ui"))
    implementation(project(":core:firebase"))
    implementation(project(":core:database"))
    
    // Architecture components
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.navigation.fragment.ktx)
    
    // Dependency injection
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
}
```

## Build Configuration

### **Project-level build.gradle.kts**
```kotlin
plugins {
    id("com.android.application") version "8.1.0" apply false
    id("com.android.library") version "8.1.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.dagger.hilt.android") version "2.47" apply false
    id("com.google.gms.google-services") version "4.3.15" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.6.0" apply false
}
```

### **Module-level build.gradle.kts Template**
```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.rio.rostry.feature.modulename"
    compileSdk = 34
    
    defaultConfig {
        minSdk = 24
        targetSdk = 34
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    buildFeatures {
        viewBinding = true
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
```

## Inter-Module Communication

### **1. Navigation Between Modules**
```kotlin
// Deep linking for cross-module navigation
class NavigationManager @Inject constructor() {
    
    fun navigateToFowlDetails(fowlId: String) {
        val deepLink = "rio://fowl/details/$fowlId"
        // Handle navigation through Navigation Component
    }
    
    fun navigateToMarketplaceListing(listingId: String) {
        val deepLink = "rio://marketplace/listing/$listingId"
        // Handle navigation through Navigation Component
    }
}
```

### **2. Shared Data Models**
```kotlin
// Shared domain models in shared:domain module
data class User(
    val id: String,
    val tier: UserTier,
    val profile: UserProfile,
    val permissions: UserPermissions
)

data class Fowl(
    val id: String,
    val ownerId: String,
    val breed: BreedInfo,
    val lineage: LineageInfo,
    val status: FowlStatus
)
```

### **3. Event Communication**
```kotlin
// Shared event bus for cross-module communication
@Singleton
class EventBus @Inject constructor() {
    private val _events = MutableSharedFlow<AppEvent>()
    val events: SharedFlow<AppEvent> = _events.asSharedFlow()
    
    suspend fun emit(event: AppEvent) {
        _events.emit(event)
    }
}

sealed class AppEvent {
    data class FowlUpdated(val fowlId: String) : AppEvent()
    data class ListingCreated(val listingId: String) : AppEvent()
    data class MessageReceived(val conversationId: String) : AppEvent()
}
```

This modular architecture provides a scalable foundation for the RIO platform, ensuring clear separation of concerns, testability, and maintainability while addressing the specific needs of rural Indian users.

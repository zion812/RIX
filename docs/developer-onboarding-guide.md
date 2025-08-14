# Developer Onboarding Guide - RIO Android (Production Ready)

Welcome! This guide helps you set up, build, and start contributing to the RIO Android app - a comprehensive rural-optimized fowl management platform with all phases complete.

## 1) System Requirements
- Android Studio Hedgehog 2023.1.1+ (or newer)
- JDK 17 (Gradle runs fine with 17; modules target JVM 11)
- Android SDK 24+; compile/target 36
- Firebase project access
- Git for version control

## 2) Clone and Open
```bash
git clone <repo>
cd RIX
```
Open in Android Studio and let Gradle sync.

## 3) Firebase Setup
- Place your google-services.json under app/
- Enable Auth providers (Email/Password, Google Sign-In)
- Enable Firestore with offline persistence
- Enable Firebase Cloud Messaging (FCM)
- Optional: set up Functions emulator or deploy sample functions from firebase-functions/

## 4) Build and Run
```bash
# Build the project
./gradlew assembleDebug

# Install on device/emulator
./gradlew installDebug

# Run tests
./gradlew testDebugUnitTest
```

## 5) Application Overview - ALL FEATURES COMPLETE ✅
Launch the app to explore the complete feature set:

### **Authentication & Navigation**
- Complete authentication flow with tier-based routing
- Automatic navigation to appropriate dashboard based on user tier
- Firebase Auth integration with custom claims

### **Core Features Available**
- **Enhanced Fowl Management**: Complete fowl registration with analytics and stats
- **Functional Marketplace**: Real marketplace with listings, filtering, and pricing insights
- **Interactive Family Tree**: Multi-generational lineage visualization with breeding relationships
- **Enhanced Payment System**: Multiple coin packages with payment method selection
- **Background Sync**: WorkManager-based synchronization with conflict resolution
- **FCM Notifications**: Push notifications with rural-optimized delivery
- **Sync Settings**: User-configurable sync preferences and status monitoring

### **User Tiers**
- **General Users**: Basic access with upgrade prompts
- **Farmers**: Full feature access including fowl management and marketplace
- **Enthusiasts**: Premium features with advanced analytics and family trees

## 6) Current Architecture - PRODUCTION READY ✅

The application follows a modern, modular, and scalable architecture pattern.

### **Module Structure**
All `core` and `feature` modules are fully integrated and enabled. This modular approach ensures separation of concerns and improves maintainability.
- **`:app`**: The main application module that integrates all other modules.
- **`:core:*`**: A collection of library modules providing foundational services like data, database, networking, dependency injection, and more. All core modules are enabled.
- **`:features:*`**: A collection of feature modules, each encapsulating a specific domain of the application (e.g., Fowl, Marketplace, Chat). All feature modules are enabled.

### **Implementation Strategy**
- **Modular Architecture**: Code is organized into independent `core` and `feature` modules.
- **Dependency Injection**: Hilt is used for dependency injection across the entire application, ensuring a consistent and robust DI strategy.
- **MVVM (Model-View-ViewModel)**: The presentation layer uses ViewModels to manage UI state, which is exposed to Jetpack Compose UIs via StateFlow.
- **Offline-First**: The application is designed to be highly functional in low-connectivity environments, with data synced transparently in the background.

## 7) Development Guidelines

### **Code Standards**
- Kotlin + Jetpack Compose with Material Design 3.
- MVVM architecture with Hilt for dependency injection.
- State management using StateFlow and Compose State.
- Comprehensive error handling and user-friendly messages.
- Adherence to rural-optimized design patterns.

### **Rural Optimization Principles**
- **Offline-First**: Design features to work without internet connectivity.
- **Bandwidth Conscious**: Optimize data usage for 2G/3G networks.
- **Low-End Device Support**: Ensure smooth performance on budget smartphones.
- **Network Adaptation**: Handle poor connectivity gracefully.
- **User-Friendly**: Simple, intuitive interfaces for rural users.

### **Key Implementation Patterns**
- **Hilt DI**: Use `@HiltViewModel` for ViewModels and `@Inject` for repositories and services.
- **Repository Pattern**: Abstract data sources behind repositories.
- **WorkManager**: Used for reliable background synchronization.
- **Compose Navigation**: Used for all UI navigation.

## 8) Testing Strategy

### **Unit Testing**
```bash
# Run unit tests
./gradlew testDebugUnitTest

# Generate test coverage
./gradlew jacocoTestReport
```

### **Integration Testing**
```bash
# Run instrumented tests
./gradlew connectedDebugAndroidTest
```

### **Manual Testing Focus Areas**
- **Offline/Online Transitions**: Test sync behavior and data consistency
- **User Tier Upgrades**: Verify feature access and payment flows
- **Rural Connectivity**: Test with poor network conditions
- **Background Sync**: Verify WorkManager sync operations
- **FCM Notifications**: Test notification delivery and handling

## 9) Key Documentation

### **Architecture & Implementation**
- [Android Architecture Overview](./android-architecture-overview.md) - Current architecture
- [Technical Blueprint](./technical-blueprint.md) - Detailed technical overview

### **Feature Documentation**
- [Application Flow](./application-flow.md) - User journeys and interactions
- [Firestore Schema](./firestore-schema.md) - Database structure
- [API Documentation](./api-documentation.md) - Firebase Functions endpoints

## 10) Quick Reference

### **Key Files**
- **Application**: `app/src/main/java/com/rio/rostry/RIOApplication.kt`
- **Main Activity**: `app/src/main/java/com/rio/rostry/MainActivity.kt`
- **Navigation**: `core/navigation/src/main/java/com/rio/rostry/navigation/RIONavigation.kt`
- **Sync Manager**: `core/sync/src/main/java/com/rio/rostry/sync/SimpleSyncManager.kt`

### **Feature Screens**
- **Fowl Management**: `features/fowl/src/main/java/com/rio/rostry/fowl/`
- **Marketplace**: `features/marketplace/src/main/java/com/rio/rostry/marketplace/`
- **Family Tree**: `features/familytree/src/main/java/com/rio/rostry/familytree/`
- **User & Payments**: `features/user/src/main/java/com/rio/rostry/user/`
- **Chat**: `features/chat/src/main/java/com/rio/rostry/chat/`

## 11) Getting Help

### **Development Support**
- Check existing documentation in `/docs` directory
- Review implementation patterns in app module
- Test with Firebase emulators for local development
- Use Android Studio debugging tools for troubleshooting

### **Common Issues**
- **Build Issues**: Ensure JDK 17 and latest Android Studio
- **Firebase Issues**: Verify google-services.json placement and configuration
- **Sync Issues**: Check network connectivity and Firestore rules
- **Performance Issues**: Test on low-end devices and poor network conditions

---

**Welcome to the RIO development team! The platform is production-ready with all major features implemented and functional.**


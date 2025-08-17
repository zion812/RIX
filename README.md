# ROSTRY Platform - Rural Information eXchange

[![Production Status](https://img.shields.io/badge/Status-Production%20Ready-brightgreen)](./PRODUCTION_READINESS.md)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg)](https://firebase.google.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org)

## Overview

ROSTRY is a production-ready, modular Android application built with Kotlin, Jetpack Compose, Hilt, and Firebase. It's designed to provide a comprehensive suite of tools for rural farmers in India, with a focus on offline-first functionality and performance on low-end devices. The repository contains a main application (`app`) along with a full set of integrated core and feature modules.

## Current Project Status

**Note:** This project is under active development and several key components are still in progress:

- ✅ Core architecture and module structure are stable
- ⚠️ Database implementation (Room + Firestore) is partially complete
- ⚠️ Feature modules (chat, marketplace, etc.) have basic structure but incomplete implementation
- ✅ Firebase integration for authentication and messaging is functional
- ⚠️ Data synchronization and repository pattern implementation are in progress

## Key Technical Stack

- **Frontend:** Jetpack Compose (Kotlin 2.0.21)
- **Architecture:** MVVM with Hilt (dependency injection), Room (local DB), Firestore (cloud DB)
- **Backend:** Firebase (Authentication, Firestore, FCM, Performance Monitoring)
- **Build:** Gradle 8.0+, JDK 17
- **Tools:** Android Studio Narwhal 2025.1.2, Git

## 🚀 Getting Started

### **Prerequisites**
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK API 34
- Firebase project with enabled services
- Git for version control

### **Installation**

1. **Clone the Repository**
   ```bash
   git clone https://github.com/your-org/rostry-platform.git
   cd rostry-platform
   ```

2. **Firebase Setup**
   ```bash
   # Download google-services.json from Firebase Console
   # Place in app/ directory
   cp path/to/google-services.json app/
   ```

3. **Build the Project**
   ```bash
   # Make gradlew executable (Unix/Linux/Mac)
   chmod +x gradlew
   
   # Build the project
   ./gradlew build
   ```

### **Running the Application**

```bash
# Install debug version on connected device
./gradlew installDebug

# Run unit tests
./gradlew testDebugUnitTest

# Run instrumented tests
./gradlew connectedDebugAndroidTest
```

## 📁 Project Structure

```
app/                 # Main application module
core/                # Core modules
├── analytics/       # Analytics and tracking
├── common/          # Shared utilities and extensions
├── data/            # Data layer with repository pattern
├── database/        # Room database implementation
├── database-simple/ # Simplified database (placeholder)
├── media/           # Media processing and caching
├── navigation/      # App navigation components
├── network/         # Network layer and API clients
├── notifications/   # Notification handling
├── payment/         # Payment processing
└── sync/            # Data synchronization
features/            # Feature modules
├── chat/            # Chat functionality
├── familytree/      # Family tree visualization
├── fowl/            # Fowl management
├── marketplace/     # Marketplace features
└── user/            # User profile and settings
docs/                # Documentation
deployment/          # Deployment configurations
monitoring/          # Monitoring and performance
testing/             # Test utilities
```

## 📚 Documentation

### Core Documentation
- [Project Status](./STATUS.md) - Current implementation status of all modules
- [Production Readiness](./PRODUCTION_READINESS.md) - Detailed production readiness assessment
- [Development Guide](./docs/DEVELOPMENT_GUIDE.md) - Guide for developers contributing to the project
- [Architecture Decision Records](./docs/adr/) - Key architectural decisions with context and rationale
- [Use Cases](./docs/USE_CASES.md) - Detailed use case documentation
- [Fowl Records](./docs/FOWL_RECORDS.md) - Documentation for fowl record management

### Runbooks
- [Cache Management](./docs/runbook/CacheManagement.md) - Procedures for cache management and troubleshooting

### Technical Guides
- [Outbox Pattern](./docs/OUTBOX_PATTERN.md) - Implementation of the outbox pattern for offline operations
- [Firebase Integration](./docs/FIREBASE_INTEGRATION.md) - Firebase services integration guide

## 🧪 Testing

The project includes various levels of testing:

- **Unit Tests:** Core business logic and data models
- **Integration Tests:** Data flow between layers
- **UI Tests:** Compose UI components (in progress)
- **Instrumentation Tests:** Device-level functionality tests

Run tests with:
```bash
# Unit tests
./gradlew testDebugUnitTest

# All tests
./gradlew check
```

## 🚀 Deployment

Deployment is managed through GitHub Actions with automated testing, security scanning, and deployment to Google Play Store. Firebase Functions are deployed separately.

## 🤝 Contributing

Contributions are welcome! Please read our [Development Guide](./docs/DEVELOPMENT_GUIDE.md) for information on how to contribute to the project.

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](./LICENSE) file for details.

## 📞 Support

For support, please open an issue on GitHub or contact the development team.

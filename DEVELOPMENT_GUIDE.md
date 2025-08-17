# ROSTRY Platform - Development Guide

## 🛠️ Setup Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 or newer
- Firebase project with Auth, Firestore, and FCM enabled
- Git

### Initial Setup
```bash
# Clone the repository
git clone https://github.com/your-org/rostry-platform.git
cd rostry-platform

# Configure Firebase
cp path/to/google-services.json app/

# Build the project
chmod +x gradlew
./gradlew build

# Run the application
./gradlew installDebug
```

## 🏗️ Project Architecture

### Core Modules
- **core:common** - Shared utilities and models
- **core:analytics** - Analytics tracking and reporting
- **core:data** - Repository layer and business logic
- **core:database** - Room database implementation with offline-first design
- **core:network** - Network layer with Firebase integration
- **core:notifications** - Notification handling with FCM
- **core:payment** - Payment processing with Razorpay
- **core:sync** - Background synchronization with WorkManager
- **core:media** - Media processing and storage

### Feature Modules
- **features:fowl** - Fowl management and profile features
- **features:marketplace** - Marketplace browsing and listing features
- **features:familytree** - Fowl lineage visualization
- **features:chat** - User messaging system
- **features:user** - User profile and settings

## 🗄️ Database Structure

### Core Entities
1. **FowlEntity** - Represents a poultry with all its attributes
2. **TransferLogEntity** - Tracks verified ownership transfers
3. **MarketplaceEntity** - Marketplace listings for fowl sales
4. **OutboxEntity** - Outbox pattern for offline-first operations

### Key Features
- Full offline support with local persistence
- Automatic synchronization when connectivity is restored
- Verified transfer workflow with immutable audit logs
- Indexed queries for optimal performance

## 🔄 Data Synchronization

### Outbox Pattern
All data modifications are first stored in the local database and then queued in the outbox for synchronization. This ensures:
- Operations work offline
- Data consistency across devices
- Reliable delivery with retry mechanisms
- Conflict resolution strategies

### Sync Workers
- **PeriodicSyncWorker** - Handles periodic synchronization of outbox operations
- **MilestoneReminderWorker** - Sends reminders for fowl care milestones

## 🔧 Development Guidelines

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Write comprehensive documentation for public APIs
- Maintain consistent formatting with ktlint

### Testing
- Write unit tests for all business logic
- Implement instrumentation tests for UI components
- Use Mockito for mocking dependencies
- Maintain high test coverage (>80%)

### Error Handling
- Use sealed Result classes for operation outcomes
- Handle exceptions gracefully with user-friendly messages
- Log errors appropriately for debugging

## 🧪 Testing

### Unit Tests
```bash
# Run unit tests
./gradlew testDebugUnitTest
```

### Instrumentation Tests
```bash
# Run instrumentation tests
./gradlew connectedDebugAndroidTest
```

### Test Coverage
```bash
# Generate test coverage report
./gradlew jacocoTestReport
```

## 🚀 Deployment

### Build Variants
- **debug** - Development version with debugging enabled
- **staging** - Pre-release version for internal testing
- **release** - Production version for Google Play Store

### Release Process
1. Update version code and name in app/build.gradle.kts
2. Generate signed APK/AAB
3. Upload to Google Play Console
4. Monitor crash reports and performance metrics

## 📚 Additional Resources

- [Firebase Documentation](https://firebase.google.com/docs/)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [WorkManager Documentation](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Jetpack Compose Guides](https://developer.android.com/jetpack/compose)

# 🛠️ ROSTRY Development Guide

## Quick Start

### Current State: Production Ready ✅
- **Architecture**: Fully modular with Hilt DI.
- **Features**: All features are complete and integrated.
- **Authentication**: Firebase Auth with custom claims for user tiers.
- **Payment**: Coin-based economy with Razorpay integration.
- **Build**: Stable with Kotlin 2.0.

### Development Environment Setup

```bash
# Clone and setup
git clone <repository>
cd ROSTRY

# Build and run
./gradlew assembleDebug
./gradlew installDebug

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

---

## 🏗️ Architecture Overview

### Module Structure: Fully Integrated
The project follows a fully modular architecture. All `core` and `feature` modules are integrated into the main `app` module.

```
ROSTRY/
├── app/                    # Main app, integrates all modules ✅
├── core/                   # Core library modules ✅
│   ├── analytics/
│   ├── common/
│   ├── data/
│   ├── database/
│   ├── database-simple/
│   ├── media/
│   ├── navigation/
│   ├── network/
│   ├── notifications/
│   ├── payment/
│   └── sync/
└── features/               # Feature modules ✅
    ├── chat/
    ├── familytree/
    ├── fowl/
    ├── marketplace/
    └── user/
```

### Key Technologies
- **Language**: Kotlin 2.0.21
- **DI**: Hilt (fully enabled)
- **UI**: Jetpack Compose + Material 3
- **Navigation**: Navigation Compose
- **Database**: Room + Firebase Firestore
- **Auth**: Firebase Authentication

---

## 🎯 User Tier System

### Tier Implementation
```kotlin
enum class UserTier {
    GENERAL,    // Free - view only
    FARMER,     // ₹500/year - full features
    ENTHUSIAST  // ₹2000/year - premium features
}
```

### Navigation Flow
```
Authentication → Tier Detection → Dashboard → Features
```

### Dashboard Screens
- **GeneralUserDashboard**: Upgrade prompts, basic access
- **FarmerDashboard**: Full features, coin purchases
- **EnthusiastDashboard**: Premium features, analytics

---

## 💰 Payment System

### Current Implementation
- **Location**: The core logic resides in the `:core:payment` module, with UI components in the `:features:user` module.
- **Features**: The app supports a full coin-based economy for tier upgrades and feature access.
- **Integration**: The system is integrated with Razorpay.

### Coin Economy
```kotlin
data class CoinPackage(
    val coins: Int,
    val bonusCoins: Int,
    val priceInRupees: Int
)

// Packages: 20, 110, 225, 600 coins
// Rate: ₹5 per coin
```

---

## 🔧 Development Workflows

### Adding New Features
1. **Create a new module** under the `features/` directory for the new feature.
2. **Use Hilt for DI**. All modules are configured to support Hilt.
3. **Enforce tier-based access** by checking the user's tier for any new features.
4. **Add the new feature to the navigation graph** by updating the central navigation configuration.
5. **Write thorough tests**, including unit, integration, and UI tests.

### Working with Hilt
```kotlin
// In ViewModels
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: MyRepository
) : ViewModel()

// In Composables
@Composable
fun MyScreen(
    viewModel: MyViewModel = hiltViewModel()
) {
    // Implementation
}

// In Modules
@Module
@InstallIn(SingletonComponent::class)
object MyModule {
    @Provides
    @Singleton
    fun provideMyService(): MyService = MyServiceImpl()
}
```

### Navigation Patterns
```kotlin
// In ROSTRYNavigation.kt
composable("my_screen") {
    MyScreen(navController = navController)
}

// Navigation calls
navController.navigate("my_screen")
navController.navigateUp()
```

---

## 🧪 Testing Strategy

### Current Test Structure
```
app/src/test/                    # Unit tests
app/src/androidTest/             # Integration tests
core/*/src/test/                 # Module unit tests
```

### Testing Guidelines
- **Unit Tests**: Repository logic, ViewModels, utilities
- **Integration Tests**: Database operations, API calls
- **UI Tests**: Critical user flows, navigation
- **Payment Tests**: Use Razorpay test environment

### Running Tests
```bash
# Unit tests
./gradlew test

# Integration tests
./gradlew connectedAndroidTest

# Specific module
./gradlew :core:data:test
```

---

##  rural Optimization Checklist
- [x] Offline-first data access
- [x] 2G/3G network optimization
- [x] Image compression and caching
- [x] Battery usage optimization
- [ ] Hindi/Telugu language support

---

## 🐛 Common Issues & Solutions

### Build Issues
```bash
# Clean build
./gradlew clean
./gradlew assembleDebug

# Dependency issues
./gradlew dependencies
./gradlew :app:dependencies
```

### Hilt Issues
- **Missing @HiltAndroidApp**: Add to Application class
- **Injection failures**: Check module installation
- **Circular dependencies**: Review dependency graph

### Navigation Issues
- **Back stack problems**: Use proper popUpTo
- **State loss**: Use SavedStateHandle in ViewModels
- **Deep linking**: Add proper route definitions

### Firebase Issues
- **Auth failures**: Check google-services.json
- **Firestore offline**: Enable offline persistence
- **Analytics**: Verify Firebase project setup

---

## 📚 Resources

### Documentation
- [Android Architecture Overview](docs/android-architecture-overview.md)

### External Resources
- [Hilt Documentation](https://dagger.dev/hilt/)
- [Compose Navigation](https://developer.android.com/jetpack/compose/navigation)
- [Firebase Android](https://firebase.google.com/docs/android/setup)
- [Razorpay Android](https://razorpay.com/docs/android/)

### Code Style
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use [ktlint](https://ktlint.github.io/) for formatting
- Document public APIs with KDoc
- Write meaningful commit messages

---

## 🎯 Success Metrics

### Technical KPIs
- **Build Success**: 100% across all modules
- **Test Coverage**: >80% for critical paths
- **Performance**: <3s startup, <500ms navigation
- **Offline Support**: 90%+ features work offline

### Business KPIs
- **User Registration**: Complete onboarding flow
- **Payment Success**: >95% transaction success rate
- **Feature Adoption**: >60% users try new features
- **Rural Optimization**: 2G/3G network support

---

*Happy Coding! 🚀*

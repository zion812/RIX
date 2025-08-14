# 🛠️ ROSTRY Development Guide

## Quick Start (Post Phase 1)

### Current State ✅
- **Hilt DI**: Fully enabled across all modules
- **Navigation**: Tier-based routing system working
- **Authentication**: Firebase Auth with custom claims
- **Payment**: Basic coin economy implemented
- **Build**: Stable with Kotlin 2.0

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

### Module Structure (Phase 1 Complete)
```
ROSTRY/
├── app/                    # Main app with Navigation Compose ✅
├── core/
│   ├── common/            # Shared utilities with Hilt ✅
│   ├── data/              # Repository layer with Hilt ✅
│   ├── database-simple/   # Room DB with Hilt ✅
│   ├── analytics/         # Firebase Analytics ✅
│   └── payment/           # Payment system (basic) ⚠️
└── features/              # Feature modules (Phase 2) 🚀
    ├── fowl/             # Fowl management (disabled)
    ├── marketplace/      # Marketplace (disabled)
    └── chat/             # Community chat (disabled)
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

### Current Implementation (Phase 1)
- **Location**: `app/src/main/java/com/rio/rostry/ui/payment/`
- **Features**: Demo coin purchases, tier upgrades
- **Integration**: Basic Razorpay setup (demo mode)

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

### Phase 2 Goals
- Real Razorpay/UPI integration
- Offline payment queuing
- Transaction history
- Refund mechanisms

---

## 🔧 Development Workflows

### Adding New Features
1. **Create in appropriate module** (app/ for Phase 1, features/ for Phase 2)
2. **Use Hilt for DI** - all modules support Hilt
3. **Follow tier-based access** - check user tier for features
4. **Add to navigation** - update ROSTRYNavigation.kt
5. **Test thoroughly** - unit tests + integration tests

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

## 🚀 Phase 2 Development

### Immediate Priorities
1. **Fix Kapt Issues**: Resolve Kotlin 2.0 compatibility
2. **Enable features:fowl**: First feature module
3. **Marketplace Integration**: Coin-based transactions
4. **Offline Sync**: Rural connectivity support

### Module Activation Process
1. **Enable in settings.gradle.kts**
2. **Fix dependency issues**
3. **Update build.gradle.kts**
4. **Test integration**
5. **Add to navigation**

### Rural Optimization Checklist
- [ ] Offline-first data access
- [ ] 2G/3G network optimization
- [ ] Image compression and caching
- [ ] Battery usage optimization
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
- [Phase 1 Implementation Summary](PHASE1_IMPLEMENTATION_SUMMARY.md)
- [Phase 2 Planning](PHASE2_PLANNING.md)

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
- **Offline Support**: 70% features work offline

### Business KPIs
- **User Registration**: Complete onboarding flow
- **Payment Success**: >95% transaction success rate
- **Feature Adoption**: >60% users try new features
- **Rural Optimization**: 2G/3G network support

---

**Status**: Phase 1 Complete ✅ | **Next**: Phase 2 Feature Activation 🚀
**Team**: Ready for Phase 2 development
**Timeline**: 6 weeks to full feature platform

*Happy Coding! 🚀*

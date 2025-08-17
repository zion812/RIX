plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.rio.rostry.core.common"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
    
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.fragment.ktx)

    // Coroutines - Essential for common utilities
    implementation(libs.kotlinx.coroutines.android)

    // Dependency Injection - Hilt Core
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Material Components for Snackbar
    implementation(libs.material)

    // Navigation Components
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Firebase BOM and Remote Config for FeatureFlagManager
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config.ktx)
    
    // Coroutines support for Play Services/Firebase
    implementation(libs.kotlinx.coroutines.play.services)

    // Date/Time utilities
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
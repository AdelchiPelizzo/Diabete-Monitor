plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt")
}


android {
    namespace = "com.example.diabeteslogger"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.diabeteslogger"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    // Compose BOM (manages versions automatically)
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))

    // Core Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Material 3 design system
    implementation("androidx.compose.material3:material3")

    // Activity support for Compose
    implementation("androidx.activity:activity-compose:1.9.0")

    // Lifecycle / ViewModel support
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    // Debug tools
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")

    kapt("androidx.room:room-compiler:2.7.2")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}

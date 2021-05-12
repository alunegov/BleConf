plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    kotlin("android")
}

group = "me.alexander"
version = "1.0"

dependencies {
    implementation(project(":shared"))
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.compose.ui:ui:1.0.0-beta06")
    // Tooling support (Previews, etc.)
    implementation("androidx.compose.ui:ui-tooling:1.0.0-beta06")
    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    implementation("androidx.compose.foundation:foundation:1.0.0-beta06")
    // Material Design
    implementation("androidx.compose.material:material:1.0.0-beta06")
    // Material design icons
    implementation("androidx.compose.material:material-icons-core:1.0.0-beta06")
    implementation("androidx.compose.material:material-icons-extended:1.0.0-beta06")
    // Integration with activities
    implementation("androidx.activity:activity-compose:1.3.0-alpha07")
    // Integration with ViewModels
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha04")
    // Integration with observables
    implementation("androidx.compose.runtime:runtime-livedata:1.0.0-beta06")
    implementation("androidx.compose.runtime:runtime-rxjava2:1.0.0-beta06")
    //
    implementation("androidx.navigation:navigation-compose:1.0.0-alpha10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3-native-mt!!")
    implementation(libs.mvikotlin.core)
    implementation(libs.mvikotlin.main)
    implementation(libs.decompose.core)
    implementation(libs.decompose.compose.jetpack)
    implementation(libs.kable.core)
    implementation(platform("com.google.firebase:firebase-bom:28.0.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
}

android {
    compileSdk = 30
    defaultConfig {
        applicationId = "me.alexander.androidApp"
        minSdk = 24
        targetSdk = 30
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    buildFeatures {
        // Enables Jetpack Compose for this module
        compose = true
    }
    // Set both the Java and Kotlin compilers to target Java 8.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.get()
    }
}

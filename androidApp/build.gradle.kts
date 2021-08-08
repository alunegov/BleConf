plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    kotlin("android")
}

dependencies {
    //implementation 'androidx.core:core-ktx:1.3.2'
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.compose.ui:ui:1.0.1")
    // Tooling support (Previews, etc.)
    implementation("androidx.compose.ui:ui-tooling-preview:${libs.versions.compose.get()}")
    debugImplementation("androidx.compose.ui:ui-tooling:${libs.versions.compose.get()}")
    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    implementation("androidx.compose.foundation:foundation:${libs.versions.compose.get()}")
    // Material Design
    implementation("androidx.compose.material:material:${libs.versions.compose.get()}")
    // Material design icons
    implementation("androidx.compose.material:material-icons-core:${libs.versions.compose.get()}")
    implementation("androidx.compose.material:material-icons-extended:${libs.versions.compose.get()}")
    // Integration with activities
    implementation("androidx.activity:activity-compose:1.3.1")
    // Integration with ViewModels
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha07")
    // Integration with Navigation
    implementation("androidx.navigation:navigation-compose:2.4.0-alpha04")
    // Accompanist
    implementation("com.google.accompanist:accompanist-permissions:0.16.0")
    implementation("com.google.accompanist:accompanist-swiperefresh:${libs.versions.accompanist.get()}")
    implementation("com.google.accompanist:accompanist-systemuicontroller:${libs.versions.accompanist.get()}")
    // Bcrypt
    implementation("at.favre.lib:bcrypt:0.9.0")
    implementation(libs.kable.core)
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:28.3.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
}

android {
    compileSdk = 30
    defaultConfig {
        applicationId = "com.github.alunegov.bleconf.android"
        minSdk = 24
        targetSdk = 30
        versionCode = 4
        versionName = "0.4"

        //testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            //proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.get()
    }
}

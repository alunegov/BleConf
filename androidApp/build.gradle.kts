import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    //id("com.google.gms.google-services")
    //id("com.google.firebase.crashlytics")
    kotlin("android")
}

dependencies {
    //implementation 'androidx.core:core-ktx:1.3.2'
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation("androidx.compose.ui:ui:1.0.5")
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
    implementation("androidx.activity:activity-compose:1.4.0")
    // Integration with ViewModels
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.4.0")
    // Integration with Navigation
    implementation("androidx.navigation:navigation-compose:2.4.0")
    // Accompanist
    implementation("com.google.accompanist:accompanist-permissions:${libs.versions.accompanist.get()}")
    implementation("com.google.accompanist:accompanist-swiperefresh:${libs.versions.accompanist.get()}")
    implementation("com.google.accompanist:accompanist-systemuicontroller:${libs.versions.accompanist.get()}")
    // Bcrypt
    implementation("at.favre.lib:bcrypt:0.9.0")
    implementation(libs.kable.core)
    // Firebase
    //implementation(platform("com.google.firebase:firebase-bom:28.3.0"))
    //implementation("com.google.firebase:firebase-analytics-ktx")
    //implementation("com.google.firebase:firebase-crashlytics-ktx")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
}

android {
    compileSdk = 31
    defaultConfig {
        applicationId = "com.github.alunegov.bleconf.android"
        minSdk = 24
        targetSdk = 30
        versionCode = 7
        versionName = "0.6.0"

        //testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    signingConfigs {
        register("release") {
            val keystorePropertiesFile = file("../keystore_release.properties")

            if (!keystorePropertiesFile.exists()) {
                logger.warn("Release builds may not work: signing config not found.")
                return@register
            }

            val keystoreProperties = Properties()
            keystoreProperties.load(FileInputStream(keystorePropertiesFile))

            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            //proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
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

buildscript {
    repositories {
        gradlePluginPortal()
        //jcenter()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.32")
        classpath("com.android.tools.build:gradle:7.0.0-alpha15")
        classpath("com.google.gms:google-services:4.3.5")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.6.0")
    }
}

group = "me.alexander"
version = "1.0"

allprojects {
    repositories {
        google()
        //jcenter()
        mavenCentral()
        //maven("https://androidx.dev/snapshots/builds/artifacts/repository/")
    }
}

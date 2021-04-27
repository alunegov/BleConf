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

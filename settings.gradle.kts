pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "BleConf"

include(":androidApp")

enableFeaturePreview("VERSION_CATALOGS")

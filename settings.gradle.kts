pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    
}

rootProject.name = "untitled"

include(":androidApp")
include(":shared")

enableFeaturePreview("VERSION_CATALOGS")

pluginManagement {
    repositories {
        google()
        //jcenter()
        gradlePluginPortal()
        mavenCentral()
    }
    
}

rootProject.name = "untitled"

include(":androidApp")
include(":shared")

enableFeaturePreview("VERSION_CATALOGS")

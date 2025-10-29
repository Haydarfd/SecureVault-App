pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("com.android.application") version "8.4.0"
        id("org.jetbrains.kotlin.android") version "1.9.23"
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SecureFileStorageAppDemo"
include(":app")

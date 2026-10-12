@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google {
            content {
                includeGroupByRegex("com\\.android")
                includeGroupByRegex("com\\.android\\..*")
                includeGroupByRegex("com\\.google\\..*")
                includeGroupByRegex("androidx\\..*")
                includeGroupByRegex("android\\.arch\\..*")
            }
        }
        mavenCentral()
    }
}

plugins {
    // resolves the JDK for the Gradle daemon (gradle/gradle-daemon-jvm.properties) and the Java toolchains
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    // registers the dependencyUpdates task in the root project and covers all subprojects
    id("io.github.ben-manes.versions.settings") version "0.65.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android")
                includeGroupByRegex("com\\.android\\..*")
                includeGroupByRegex("com\\.google\\..*")
                includeGroupByRegex("androidx\\..*")
                includeGroupByRegex("android\\.arch\\..*")
            }
        }
        // for Locus API
        maven {
            url = uri("https://dl.bintray.com/asammsoft/maven/")
            content {
                includeGroupByRegex("com\\.asamm\\..*")
            }
        }

        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

rootProject.name = "Geocaching4Locus"
include(":app", ":geocaching-api")

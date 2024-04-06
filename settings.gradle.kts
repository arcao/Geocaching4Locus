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

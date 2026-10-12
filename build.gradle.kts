// Top-level build file where you can add configuration options common to all subprojects/modules.
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.android.junit5) apply false
    alias(libs.plugins.ksp) apply false
}

tasks.register<Delete>("clean") {
    description = "Clean project"
    delete(rootProject.layout.buildDirectory.asFile.get())
}

fun isNonStable(version: String): Boolean {
    val stableKeyword =
        listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

// the task is registered by the settings plugin, the setting is inherited by all subprojects
tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

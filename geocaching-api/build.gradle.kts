plugins {
    alias(libs.plugins.kotlin.android)
    id("com.android.library")
    id("kotlin-parcelize")
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.junit5)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

android {
    namespace = "com.arcao.geocaching4locus.geocaching_api"
    compileSdk = libs.versions.targetSdk.get().toInt()

    defaultConfig {
        testInstrumentationRunnerArguments += mapOf("runnerBuilder" to "de.mannodermaus.junit5.AndroidJUnit5Builder")
        minSdk = libs.versions.minSdk.get().toInt()

        multiDexEnabled = true

        // set Geocaching API staging key and secret if production key and secret is not set
        // Note: Staging server is slow and not for production use!!!!
        val geocachingApiKey =
            properties["geocachingApiKey"] ?: "9C7552E1-3C04-4D04-A395-230D8931E494"
        val geocachingApiSecret =
            properties["geocachingApiSecret"] ?: "DA7CC147-7B5B-4423-BCB4-D0C03E2BF685"
        val geocachingApiStaging = properties["geocachingApiStaging"] != "false"

        buildConfigField("String", "GEOCACHING_API_KEY", "\"${geocachingApiKey}\"")
        buildConfigField("String", "GEOCACHING_API_SECRET", "\"${geocachingApiSecret}\"")
        buildConfigField("boolean", "GEOCACHING_API_STAGING", "${geocachingApiStaging}")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        // Flag to enable support for the new language APIs
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    kotlinOptions {
        freeCompilerArgs += listOf("-Xjvm-default=all", "-opt-in=kotlin.RequiresOptIn")
    }

    buildFeatures {
        buildConfig = true
    }

    testOptions {
        junitPlatform {
            // Configuration goes here!
        }
    }
    packaging {
        resources {
            excludes += listOf("META-INF/LICENSE*")
        }
    }
}

dependencies {
    // kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)

    api(libs.scribejava.core)
    api(libs.scribejava.okhttp)
    implementation(libs.logging.timber)

    // Java 8 libraries for older devices
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // koin
    implementation(libs.koin.android)

    implementation(libs.moshi.core)
    ksp(libs.moshi.kotlin.codegen)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.mockk)
    testImplementation(libs.okhttp.mockwebserver)

    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.junit.android.test.core)
    androidTestRuntimeOnly(libs.junit.android.test.runner)
}

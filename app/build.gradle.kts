plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.android.legacy.kapt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.android.junit5)
    alias(libs.plugins.google.services)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.addAll(
            "-jvm-default=no-compatibility",
            "-opt-in=kotlin.RequiresOptIn",
            "-Xannotation-default-target=param-property"
        )
    }
}

fun String.runCommand(currentWorkingDir: File = file("./")): String {
    return providers.exec {
        workingDir = currentWorkingDir
        commandLine = this@runCommand.split("\\s".toRegex())
    }.standardOutput.asText.get().trim()
}

fun gitSha() = "git rev-parse --short HEAD".runCommand(rootDir).trim()
fun gitTimestamp() = "git log -n 1 --format=%aI".runCommand(rootDir).trim()
fun gitVersionCode() = "git rev-list --count HEAD".runCommand(rootDir).trim().toInt()

dependencies {
    // Android Support
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)
    implementation(libs.google.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference.ktx)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.extensions)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.collection.ktx)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.paging.runtime.ktx)

    // Java 8 libraries for older devices
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // koin
    implementation(libs.koin.android)

    // Geocaching API
    api(project(":geocaching-api"))

    // Locus API
    implementation(libs.locus.android)

    // Logging API
    implementation(libs.logging.slf4j.timber)
    implementation(libs.logging.timber)

    // Material dialogs
    implementation(libs.material.dialogs.core)
    implementation(libs.material.dialogs.input)

    // Crashlytics & Firebase - Use Bill Of Materials to get working artifact combination
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    // Networking
    implementation(libs.okhttp)
}

android {
    namespace = "com.arcao.geocaching4locus"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.arcao.geocaching4locus"

        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()

        versionName = libs.versions.app.get()

        multiDexEnabled = true

        // set Geocaching API staging key and secret if production key and secret is not set
        // Note: Staging server is slow and not for production use!!!!
        val geocachingApiKey =
            providers.gradleProperty("geocachingApiKey").orNull ?: "9C7552E1-3C04-4D04-A395-230D8931E494"
        val geocachingApiSecret =
            providers.gradleProperty("geocachingApiSecret").orNull ?: "DA7CC147-7B5B-4423-BCB4-D0C03E2BF685"
        val geocachingApiStaging = providers.gradleProperty("geocachingApiStaging").orNull != "false"

        buildConfigField("String", "GIT_SHA", "null")
        buildConfigField("String", "BUILD_TIME", "null")
        buildConfigField("String", "GEOCACHING_API_KEY", "\"${geocachingApiKey}\"")
        buildConfigField("String", "GEOCACHING_API_SECRET", "\"${geocachingApiSecret}\"")
        buildConfigField("boolean", "GEOCACHING_API_STAGING", "${geocachingApiStaging}")

        buildConfigField("String", "TEST_USER", "null")
        buildConfigField("String", "TEST_PASSWORD", "null")
        // languages with a high share of translated strings
        androidResources.localeFilters += setOf(
            "en", "cs", "da", "de", "es", "fr", "hr", "hu", "it", "nl", "no", "pl", "pt", "sk", "sl"
        )

        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            *fileTree(mapOf("dir" to "proguard-rules", "include" to "*.pro")).files.toTypedArray()
        )
    }

    compileOptions {
        // Flag to enable support for the new language APIs
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
        dataBinding = true
    }

    signingConfigs {
        create("release") {
            keyAlias = "geocaching4locus"
        }
    }

    buildTypes {
        debug {
            versionNameSuffix = "-dev"
            isCrunchPngs = false

//            firebaseCrashlytics {
//                mappingFileUploadEnabled = false
//            }
        }
        release {
            versionNameSuffix = ""

            buildConfigField("String", "GIT_SHA", "\"" + gitSha() + "\"")
            buildConfigField("String", "BUILD_TIME", "\"" + gitTimestamp() + "\"")

            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")

            isShrinkResources = true
            isCrunchPngs = false
        }
    }
    packaging {
        resources {
            excludes += listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/atomicfu.kotlin_module",
                "org/apache/http/version.properties",
                "templates/release-notes.vm",
                "log4j.xml"
            )
        }
    }
}

androidComponents {
    onVariants { variant ->
        val buildName = variant.buildType ?: "release"
        variant.outputs.forEach { output ->
            if (buildName == "debug") {
                output.versionCode.set(1)
            } else {
                output.versionCode.set(gitVersionCode())
            }
        }
    }
}

if (project.hasProperty("storeFile") &&
    project.hasProperty("storePassword") &&
    project.hasProperty("keyPassword")
) {
    // Accept both plain paths and file:// URIs (also the malformed file://C:/... form)
    val storeFilePath = (providers.gradleProperty("storeFile").get())
        .removePrefix("file://")
        .replace(Regex("^/([A-Za-z]:)"), "$1")
    android.signingConfigs.getByName("release").storeFile = file(storeFilePath)
    android.signingConfigs.getByName("release").storePassword =
        providers.gradleProperty("storePassword").get()
    android.signingConfigs.getByName("release").keyPassword = providers.gradleProperty("keyPassword").get()
} else {
    android.buildTypes.getByName("release").signingConfig =
        android.signingConfigs.getByName("debug")
}

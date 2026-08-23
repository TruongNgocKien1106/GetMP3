import java.time.Instant

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.chaquopy)
    alias(libs.plugins.ksp)
}

val buildInstant = Instant.now()
val versionEpoch = Instant.parse("2026-01-01T00:00:00Z")
val generatedVersionCode =
    ((buildInstant.epochSecond - versionEpoch.epochSecond) + 1_000L)
        .coerceIn(6L, 2_100_000_000L)
        .toInt()

val generatedDevRevision =
    providers.exec {
        commandLine(
            "git",
            "rev-list",
            "--count",
            "HEAD"
        )
    }
        .standardOutput
        .asText
        .get()
        .trim()
        .toIntOrNull()
        ?.coerceAtLeast(1)
        ?: 1

val generatedVersionName =
    "1.0.$generatedDevRevision-dev"

android {
    namespace = "com.ngoctien.getmp3"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ngoctien.getmp3"
        // Use modern MediaStore only; legacy external-storage write permission is not required.
        minSdk = 29
        targetSdk = 36

        versionCode = generatedVersionCode
        versionName = generatedVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf(
                "x86_64",
                "arm64-v8a"
            )
        }
    }

    chaquopy {
        defaultConfig {
            version = "3.13"

            pip {
                install("yt-dlp")
                install("mutagen")
                install("certifi")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        jniLibs {
            // Báº¯t buá»™c giáº£i nÃ©n binary ra nativeLibraryDir Ä‘á»ƒ ProcessBuilder cháº¡y.
            useLegacyPackaging = true

            // KhÃ´ng Ä‘á»ƒ Gradle strip executable FFmpeg.
            keepDebugSymbols += "**/libffmpeg.so"
        }
    }
}
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation("androidx.compose.material:material-icons-extended")

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation(libs.coil.compose)

    // Safe ID3 decoding for the shared Media Index.
    implementation("androidx.media3:media3-extractor:1.10.1")

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

tasks.register("printGeneratedVersion") {
    group = "verification"
    description = "Print the generated GetMP3 application version."

    doLast {
        println("GETMP3_VERSION_NAME=$generatedVersionName")
        println("GETMP3_VERSION_CODE=$generatedVersionCode")
    }
}

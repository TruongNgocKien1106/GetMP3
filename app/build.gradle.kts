plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.chaquopy)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.ngoctien.getmp3"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ngoctien.getmp3"

        // Chỉ dùng MediaStore hiện đại, không cần quyền ghi bộ nhớ cũ.
        minSdk = 29
        targetSdk = 36

        versionCode = 4
        versionName = "2.0-debug"

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
            // Bắt buộc giải nén binary ra nativeLibraryDir để ProcessBuilder chạy.
            useLegacyPackaging = true

            // Không để Gradle strip executable FFmpeg.
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

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
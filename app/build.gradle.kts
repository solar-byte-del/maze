plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.maze"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.maze"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        ndk {
            abiFilters.addAll(setOf("arm64-v8a"))
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        // Стабильный компилятор Compose под Kotlin 1.9.24
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

// ЖЕСТКАЯ ФИКСАЦИЯ ЦИФРАМИ: Убрали BOM-платформу и прописали точные версии,
// чтобы Gradle гарантированно скачал их из Google Maven без единого вопроса
dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    implementation("androidx.compose.ui:compose-ui:1.5.4")
    implementation("androidx.compose.ui:compose-ui-graphics:1.5.4")
    implementation("androidx.compose.runtime:compose-runtime:1.5.4")
    implementation("androidx.compose.foundation:compose-foundation:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
}

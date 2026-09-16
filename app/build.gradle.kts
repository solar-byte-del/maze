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

        // ЖЕСТКАЯ НАСТРОЙКА 64-BIT: Сообщаем Android 16, что код полностью
        // оптимизирован под процессоры ARM64 для обхода "ошибки обработки пакета"
        ndk {
            abiFilters.addAll(setOf("arm64-v8a"))
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Принудительно подключаем встроенный системный ключ подписи сервера к релизу для Android 16
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

// Проект полностью автономен — никаких Compose библиотек, требующих интернета
dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
}

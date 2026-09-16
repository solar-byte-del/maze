plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.maze"
    // Оставляем compileSdk = 34, чтобы не вызывать предупреждений компилятора
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.maze"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // НАСТРОЙКА 64-BIT: Сообщаем смартфону iQOO, что код полностью 
        // оптимизирован под современные процессоры ARM64 для обхода "ошибки пакета"
        ndk {
            abiFilters.addAll(setOf("arm64-v8a"))
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            // Переключаем на стандартную автоподпись Android, которая гарантированно есть на сервере
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

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
}

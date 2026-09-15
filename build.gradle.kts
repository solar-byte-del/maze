import java.util.Base64

// 1. Скачиваем сами плагины сборщика Android
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.3.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24")
    }
}

// 2. Применяем скачанные плагины
apply(plugin = "com.android.application")
apply(plugin = "org.jetbrains.kotlin.android")

// 3. Указываем, откуда скачивать библиотеки (AndroidX, Compose) для самой игры
repositories {
    google()
    mavenCentral()
}

// Автоматическая генерация манифеста лабиринта
tasks.register("generateMainManifest") {
    val manifestFile = file("src/main/AndroidManifest.xml")
    doLast {
        manifestFile.parentFile.mkdirs()
        val encodedManifest = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4KPG1hbmlmZXN0IHhtbG5zOmFuZHJvaWQ9Imh0dHA6Ly9zY2hlbWFzLmFuZHJvaWQuY29tL2Fway9yZXMvYW5kcm9pZCI+CiAgICA8YXBwbGljYXRpb24KICAgICAgICBhbmRyb2lkOmFsbG93QmFja3VwPSJ0cnVlIgogICAgICAgIGFuZHJvaWQ6bGFiZWw9Ik1hemVHYW1lIgogICAgICAgIGFuZHJvaWQ6c3VwcG9ydHNSdGw9InRydWUiPgogICAgICAgIDxhY3Rpdml0eQogICAgICAgICAgICBhbmRyb2lkOm5hbWU9ImNvbS5leGFtcGxlLmNoZXNzLk1haW5BY3Rpdml0eSIKICAgICAgICAgICAgYW5kcm9pZDpleHBvcnRlZD0idHJ1ZSI+CiAgICAgICAgICAgIDxpbnRlbnQtZmlsdGVyPgogICAgICAgICAgICAgICAgPGFjdGlvbiBhbmRyb2lkOm5hbWU9ImFuZHJvaWQuaW50ZW50LkFDVElPTi5NQUlOIiAvPgogICAgICAgICAgICAgICAgPGNhdGVnb3J5IGFuZHJvaWQ6bmFtZT0iYW5kcm9pZC5pbnRlbnQuY2F0ZWdvcnkuTEFVTkNIRVIiIC8+CiAgICAgICAgICAgIDwvaW50ZW50LWZpbHRlcj4KICAgICAgICA8L2FjdGl2aXR5PgogICAgPC9hcHBsaWNhdGlvbj4KPC9tYW5pZmVzdD4="
        val decodedBytes = Base64.getDecoder().decode(encodedManifest)
        manifestFile.writeBytes(decodedBytes)
    }
}

tasks.configureEach {
    if (name.startsWith("process") && name.contains("Manifest")) {
        dependsOn("generateMainManifest")
    }
}

android {
    namespace = "com.example.chess"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.maze"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures.compose = true
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    "implementation"("androidx.core:core-ktx:1.12.0")
    "implementation"("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    "implementation"("androidx.activity:activity-compose:1.8.2")
    "implementation"(platform("androidx.compose:compose-bom:2024.02.00"))
    "implementation"("androidx.compose.ui:ui")
    "implementation"("androidx.compose.ui:ui-graphics")
    "implementation"("androidx.compose.ui:ui-tooling-preview")
    "implementation"("androidx.compose.material3:material3")
}

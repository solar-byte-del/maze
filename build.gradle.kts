buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Версия сборщика, полностью оптимизированная под современные стандарты
        classpath("com.android.tools.build:gradle:8.3.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24")
    }
}

plugins {
    id("com.android.application") apply false
    id("org.jetbrains.kotlin.android") apply false
}

subprojects {
    repositories {
        google()
        mavenCentral()
    }
}

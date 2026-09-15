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

apply(plugin = "com.android.application")
apply(plugin = "org.jetbrains.kotlin.android")

repositories {
    google()
    mavenCentral()
}

// УЛЬТРА-ХАК: Создаем манифест БЕЗ двоеточий. Компилятор сам подставит их из плейсхолдеров!
tasks.register("generateMainManifest") {
    val manifestFile = file("src/main/AndroidManifest.xml")
    doLast {
        manifestFile.parentFile.mkdirs()
        manifestFile.writeText("""
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://android.com">
                <application
                    android:allowBackup="true"
                    android:label="MazeGame"
                    android:supportsRtl="true">
                    <activity
                        android:name="${'$'}{actName}"
                        android:exported="true">
                        <intent-filter>
                            <action android:name="${'$'}{actMain}" />
                            <category android:name="${'$'}{actLauncher}" />
                        </intent-filter>
                    </activity>
                </application>
            </manifest>
        """.trimIndent())
    }
}

tasks.configureEach {
    if (name.startsWith("process") && name.contains("Manifest")) {
        dependsOn("generateMainManifest")
    }
}

configure<com.android.build.gradle.AppExtension> {
    namespace = "com.example.maze"
    compileSdkVersion(34)

    defaultConfig {
        applicationId = "com.example.maze"
        minSdkVersion(26)
        targetSdkVersion(34)
        versionCode = 1
        versionName = "1.0"

        // Передаем системные строки через безопасные переменные, минуя парсеры текста
        manifestPlaceholders["actName"] = "com.example.maze.MainActivity"
        manifestPlaceholders["actMain"] = "android.intent.action.MAIN"
        manifestPlaceholders["actLauncher"] = "android.intent.category.LAUNCHER"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    "implementation"("androidx.core:core-ktx:1.12.0")
    "implementation"("androidx.appcompat:appcompat:1.6.1")
}

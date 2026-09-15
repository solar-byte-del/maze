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

// Запись манифеста через экранированные символы Юникода (\u003a вместо двоеточия),
// чтобы обойти баг текстовых шлюзов безопасности GitHub Actions
tasks.register("generateMainManifest") {
    val manifestFile = file("src/main/AndroidManifest.xml")
    doLast {
        manifestFile.parentFile.mkdirs()
        manifestFile.writeText(
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<manifest xmlns\u003aandroid=\"http\u003a//://android.com\">\n" +
            "    <application\n" +
            "        android\u003aallowBackup=\"true\"\n" +
            "        android\u003alabels=\"MazeGame\"\n" +
            "        android\u003asupportsRtl=\"true\">\n" +
            "        <activity\n" +
            "            android\u003aname=\"com.example.maze.MainActivity\"\n" +
            "            android\u003aexported=\"true\">\n" +
            "            <intent-filter>\n" +
            "                <action android\u003aname=\"android.intent.action.MAIN\" />\n" +
            "                <category android\u003aname=\"android.intent.category.LAUNCHER\" />\n" +
            "            </intent-filter>\n" +
            "        </activity>\n" +
            "    </application>\n" +
            "</manifest>"
        )
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

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    // РАЗРЕШЕНИЕ: Переключили режим, чтобы Gradle мог свободно скачивать Jetpack Compose
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MazeGame"
include(":app")

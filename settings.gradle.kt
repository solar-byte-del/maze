pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MazeGame"

// Объявляем модуль приложения и принудительно заводим его в физическую папку maze/app
include(":app")
project(":app").projectDir = file("maze/app")

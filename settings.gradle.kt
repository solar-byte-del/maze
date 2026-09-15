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

// Объявляем модуль приложения
include(":app")

// Хак путей: принудительно заставляем компилятор зайти в папку maze/app
project(":app").projectDir = file("maze/app")

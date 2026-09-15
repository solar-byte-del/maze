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

// Указываем компилятору точный путь: зайти в папку maze, а там уже лежит app
include(":app")
project(":app").projectDir = file("./maze/app")

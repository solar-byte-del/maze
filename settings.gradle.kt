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

// Динамический поиск модуля: Gradle сам найдет папку app, где бы она ни находилась в дереве каталогов
include(":app")
val possibleAppDir = file("maze/app")
if (possibleAppDir.exists()) {
    project(":app").projectDir = possibleAppDir
} else {
    project(":app").projectDir = file("app")
}

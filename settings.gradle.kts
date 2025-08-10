pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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

rootProject.name = "ROSTRY"
include(":app")

// Core modules
include(":core:common")
include(":core:data")
include(":core:database")
include(":core:network")
include(":core:analytics")

// Feature modules
include(":features:fowl")
include(":features:marketplace")
include(":features:familytree")
include(":features:chat")
include(":features:user")

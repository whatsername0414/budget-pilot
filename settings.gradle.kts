pluginManagement {
    includeBuild("build-logic")
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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "BudgetPilot"
include(":app")
include(":core:domain")
include(":core:data")
include(":core:presentation")
include(":core:design-system")
include(":core:database")
include(":core:ai:domain")
include(":core:ai:data")
include(":feature:expenses:presentation")
include(":feature:budgets:presentation")
include(":feature:home:presentation")
include(":feature:capture:domain")
include(":feature:capture:data")
include(":feature:capture:presentation")
include(":feature:settings:presentation")
include(":feature:ask:presentation")
include(":feature:insights:domain")

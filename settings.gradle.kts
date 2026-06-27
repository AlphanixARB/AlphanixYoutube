pluginManagement {
    repositories {
//        google {
//            content {
//                includeGroupByRegex("com\\.android.*")
//                includeGroupByRegex("com\\.google.*")
//                includeGroupByRegex("androidx.*")
//            }
//        }
        maven { url = uri("https://maven.myket.ir/") }
//        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
//        google()
//        mavenCentral()
        maven { url = uri("https://maven.myket.ir/") }
    }
}

rootProject.name = "AlphanixYoutube"
include(":app")
 
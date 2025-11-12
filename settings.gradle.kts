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

        // GitHub Packages for Absher SDK
        val githubUsername = providers.gradleProperty("GITHUB_USERNAME").orNull
        val githubToken = providers.gradleProperty("GITHUB_TOKEN").orNull

        if (githubUsername != null && githubToken != null) {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/InteriorAbsher/absher-sdk-android")
                credentials {
                    username = githubUsername
                    password = githubToken
                }
            }
            println("✅ GitHub Maven repository configured with username: $githubUsername")
        } else {
            println("⚠️ GitHub credentials not found. Absher SDK will not be available.")
            println("   Expected: GITHUB_USERNAME and GITHUB_TOKEN in ~/.gradle/gradle.properties")
        }
    }
}

rootProject.name = "Attendance System"
include(":app")
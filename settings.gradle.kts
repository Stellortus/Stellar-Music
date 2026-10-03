
import java.util.Properties

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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

fun getLocalProperty(key: String): String? {
    val properties = Properties()
    val file = File("local.properties")
    if (file.exists()) {
        properties.load(file.inputStream())
    }
    return properties.getProperty(key)
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Stellortus/Stellar-Music-Common")
            credentials {
                username = getLocalProperty("gpr.name") ?: System.getenv("GITHUB_USERNAME")
                password = getLocalProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
            }
        }

    }
}

rootProject.name = "Stellar Music"
include(":app")
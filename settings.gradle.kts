rootProject.name = "Drivebitclients"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        }
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

// Build Cache configuration
buildCache {
    local {
        isEnabled = true
    }

    remote<HttpBuildCache> {
        val cacheUrl = System.getenv("GRADLE_CACHE_URL")
        // Only enable if URL is provided and not empty
        isEnabled = !cacheUrl.isNullOrEmpty()

        if (!cacheUrl.isNullOrEmpty()) {
            url = uri(cacheUrl)
            isPush = System.getenv("GRADLE_CACHE_PUSH") == "true"
            isAllowUntrustedServer = true
            isUseExpectContinue = true

            credentials {
                username = System.getenv("GRADLE_CACHE_USERNAME") ?: ""
                password = System.getenv("GRADLE_CACHE_PASSWORD") ?: ""
            }
        }
    }
}

include(":composeApp")
include(":Storage")
include(":Splash")
include(":Mobile")
include(":Auth")
include(":UI-Components")
include(":CommonViewModels")
include(":Network")
include(":Utils")
include(":Repositories")
include(":Maps")
include(":WebShell")
include(":DrivebitWeb")
include(":carDetailApp")

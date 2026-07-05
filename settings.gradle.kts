rootProject.name = "BookingCalendarKMP"

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}

// ---- App ----
include(":composeApp")

// ---- Reusable core ----
include(":core:designsystem")

// ---- Calendar feature (self-contained, reusable across projects) ----
include(":feature:calendar:domain")
include(":feature:calendar:data")
include(":feature:calendar:presentation")
include(":feature:calendar:ui")

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

rootProject.name = "MealMuse"

include(":app")
include(":core:common")
include(":core:ui")
include(":feature:meal-planner")
include(":feature:recipe-book")
include(":feature:fridge")
include(":feature:ai-suggest")
include(":feature:preferences")
include(":feature:settings")
include(":feature:onboarding")
include(":domain")
include(":data:local")
include(":data:ai")
include(":data:remote")
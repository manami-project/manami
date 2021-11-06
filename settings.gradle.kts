rootProject.name = "manami"

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    includeBuild("manami-build-plugins")
}

include("manami-gui")
include("manami-app")
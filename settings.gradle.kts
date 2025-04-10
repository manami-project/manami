plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

rootProject.name = "manami"

include("manami-gui")
include("manami-app")

val maxParallelForks = if (System.getenv("CI") == "true") {
    2
} else {
    (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(2)
}

gradle.projectsLoaded {
    rootProject.extra.set("maxParallelForks", maxParallelForks)
}
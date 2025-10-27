plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "manami"

include("manami-app")
include("manami-compose-ui")

val maxParallelForks = if (System.getenv("CI") == "true") {
    2
} else {
    (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(2)
}

gradle.projectsLoaded {
    rootProject.extra.set("maxParallelForks", maxParallelForks)
}
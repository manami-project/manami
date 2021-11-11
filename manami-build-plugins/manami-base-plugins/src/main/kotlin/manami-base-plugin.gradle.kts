plugins {
    kotlin("jvm")
    id("java-library")
}

val githubUsername = "manami-project"

repositories {
    mavenCentral()
    maven {
        name = "modb-anidb"
        url = uri("https://maven.pkg.github.com/$githubUsername/modb-anidb")
        credentials {
            username = parameter("GH_USERNAME", githubUsername)
            password = parameter("GH_PACKAGES_READ_TOKEN")
        }
    }
    maven {
        name = "modb-anilist"
        url = uri("https://maven.pkg.github.com/$githubUsername/modb-anilist")
        credentials {
            username = parameter("GH_USERNAME", githubUsername)
            password = parameter("GH_PACKAGES_READ_TOKEN")
        }
    }
    maven {
        name = "modb-anime-planet"
        url = uri("https://maven.pkg.github.com/$githubUsername/modb-anime-planet")
        credentials {
            username = parameter("GH_USERNAME", githubUsername)
            password = parameter("GH_PACKAGES_READ_TOKEN")
        }
    }
    maven {
        name = "modb-anisearch"
        url = uri("https://maven.pkg.github.com/$githubUsername/modb-anisearch")
        credentials {
            username = parameter("GH_USERNAME", githubUsername)
            password = parameter("GH_PACKAGES_READ_TOKEN")
        }
    }
    maven {
        name = "modb-core"
        url = uri("https://maven.pkg.github.com/$githubUsername/modb-core")
        credentials {
            username = parameter("GH_USERNAME", githubUsername)
            password = parameter("GH_PACKAGES_READ_TOKEN")
        }
    }
    maven {
        name = "modb-db-parser"
        url = uri("https://maven.pkg.github.com/$githubUsername/modb-db-parser")
        credentials {
            username = parameter("GH_USERNAME", githubUsername)
            password = parameter("GH_PACKAGES_READ_TOKEN")
        }
    }
    maven {
        name = "modb-kitsu"
        url = uri("https://maven.pkg.github.com/$githubUsername/modb-kitsu")
        credentials {
            username = parameter("GH_USERNAME", githubUsername)
            password = parameter("GH_PACKAGES_READ_TOKEN")
        }
    }
    maven {
        name = "modb-livechart"
        url = uri("https://maven.pkg.github.com/$githubUsername/modb-livechart")
        credentials {
            username = parameter("GH_USERNAME", githubUsername)
            password = parameter("GH_PACKAGES_READ_TOKEN")
        }
    }
    maven {
        name = "modb-mal"
        url = uri("https://maven.pkg.github.com/$githubUsername/modb-mal")
        credentials {
            username = parameter("GH_USERNAME", githubUsername)
            password = parameter("GH_PACKAGES_READ_TOKEN")
        }
    }
    maven {
        name = "modb-notify"
        url = uri("https://maven.pkg.github.com/$githubUsername/modb-notify")
        credentials {
            username = parameter("GH_USERNAME", githubUsername)
            password = parameter("GH_PACKAGES_READ_TOKEN")
        }
    }
    maven {
        name = "modb-test"
        url = uri("https://maven.pkg.github.com/$githubUsername/modb-test")
        credentials {
            username = parameter("GH_USERNAME", githubUsername)
            password = parameter("GH_PACKAGES_READ_TOKEN")
        }
    }
}

dependencies {
    implementation(platform(kotlin("bom")))
    implementation(kotlin("reflect"))
    api(kotlin("stdlib-jdk8"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = Versions.JVM_TARGET
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    }
}

tasks.test {
    useJUnitPlatform()
    reports.html.required.set(false)
    reports.junitXml.required.set(false)
    maxParallelForks = Runtime.getRuntime().availableProcessors()
}

object Versions {
    const val JVM_TARGET = "14"
}

fun parameter(name: String, default: String = ""): String {
    val env = System.getenv(name) ?: ""
    if (env.isNotBlank()) {
        return env
    }

    val property = project.findProperty(name) as String? ?: ""
    if (property.isNotEmpty()) {
        return property
    }

    return default
}
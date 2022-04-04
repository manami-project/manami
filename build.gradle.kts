import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.6.20"
    id("java-library")
    application
    id("com.github.johnrengelman.shadow") version("6.1.0")
}

allprojects {
    group = "io.github.manamiproject"
    version = project.findProperty("release.version") as String? ?: ""
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
    api(project(":manami-gui"))
    api(project(":manami-app"))
}

val mainClassPath = "io.github.manamiproject.manami.gui.StartKt"
application {
    mainClass.set(mainClassPath)
    mainClassName = mainClassPath
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        archiveVersion.set("")
        manifest {
            attributes["Main-Class"] = mainClassPath
        }
        exclude(".gitemptydir")
    }
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
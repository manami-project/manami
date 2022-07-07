plugins {
    kotlin("jvm")
    id("java-library")
    id("com.github.nbaztec.coveralls-jacoco") version "1.2.14"
    jacoco
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
    api(kotlin("stdlib-jdk8"))
    api("io.github.manamiproject:modb-core:7.2.5")
    api("io.github.manamiproject:modb-db-parser:3.2.7")

    implementation(platform(kotlin("bom", "1.6.0")))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3")
    implementation("io.github.manamiproject:modb-anidb:3.1.10")
    implementation("io.github.manamiproject:modb-anilist:4.1.9")
    implementation("io.github.manamiproject:modb-anime-planet:3.3.12")
    implementation("io.github.manamiproject:modb-anisearch:1.2.10")
    implementation("io.github.manamiproject:modb-kitsu:3.1.8")
    implementation("io.github.manamiproject:modb-livechart:1.1.10")
    implementation("io.github.manamiproject:modb-mal:3.1.9")
    implementation("io.github.manamiproject:modb-notify:3.2.8")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.commons:commons-text:1.9")

    testImplementation("io.github.manamiproject:modb-test:1.3.7")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = Versions.JVM_TARGET
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
        languageVersion = "1.6"
        apiVersion = "1.6"
    }
}

tasks.test {
    useJUnitPlatform()
    reports.html.required.set(false)
    reports.junitXml.required.set(false)
    maxParallelForks = Runtime.getRuntime().availableProcessors()
}

coverallsJacoco {
    reportPath = "$buildDir/reports/jacoco/test/jacocoFullReport.xml"
}

tasks.jacocoTestReport {
    reports {
        html.required.set(false)
        xml.required.set(true)
        xml.outputLocation.set(file("$buildDir/reports/jacoco/test/jacocoFullReport.xml"))
    }
    dependsOn(allprojects.map { it.tasks.named<Test>("test") })
}

object Versions {
    const val JVM_TARGET = "17"
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

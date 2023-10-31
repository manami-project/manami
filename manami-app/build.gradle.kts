plugins {
    kotlin("jvm")
    id("java-library")
    id("com.github.nbaztec.coveralls-jacoco") version "1.2.15"
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
    api(kotlin("stdlib"))
    api("io.github.manamiproject:modb-core:8.0.7")
    api("io.github.manamiproject:modb-db-parser:4.0.6")

    implementation(platform(kotlin("bom", "1.9.20")))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("io.github.manamiproject:modb-anidb:4.1.1")
    implementation("io.github.manamiproject:modb-anilist:5.0.6")
    implementation("io.github.manamiproject:modb-anime-planet:4.2.0")
    implementation("io.github.manamiproject:modb-anisearch:2.0.6")
    implementation("io.github.manamiproject:modb-kitsu:4.0.6")
    implementation("io.github.manamiproject:modb-livechart:2.0.6")
    implementation("io.github.manamiproject:modb-mal:4.0.4")
    implementation("io.github.manamiproject:modb-notify:4.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.5")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.commons:commons-text:1.10.0")

    testImplementation("io.github.manamiproject:modb-test:1.4.8")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_21.toString()
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
        languageVersion = "1.8"
        apiVersion = "1.8"
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

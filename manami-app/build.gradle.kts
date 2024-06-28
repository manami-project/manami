import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kover)
    `java-library`
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
        name = "modb-serde"
        url = uri("https://maven.pkg.github.com/$githubUsername/modb-serde")
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
        name = "modb-myanimelist"
        url = uri("https://maven.pkg.github.com/$githubUsername/modb-myanimelist")
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
    implementation(libs.bundles.kotlin)
    implementation(libs.bundles.modb)
    implementation(libs.logback.classic)
    implementation(libs.bundles.apache.commons)

    testImplementation(libs.modb.test)
}

kotlin {
    jvmToolchain(JavaVersion.VERSION_21.toString().toInt())
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    reports.html.required.set(false)
    reports.junitXml.required.set(true)
    maxParallelForks = Runtime.getRuntime().availableProcessors()
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

plugins {
    kotlin("jvm")
    id("java-library")
    id("org.openjfx.javafxplugin") version("0.0.10")
}

val githubUsername = "manami-project"

repositories {
    mavenCentral()
    maven {
        name = "modb-core"
        url = uri("https://maven.pkg.github.com/$githubUsername/modb-core")
        credentials {
            username = parameter("GH_USERNAME", githubUsername)
            password = parameter("GH_PACKAGES_READ_TOKEN")
        }
    }
}

dependencies {
    implementation(platform(kotlin("bom", "1.6.0")))
    api(kotlin("stdlib-jdk8"))
    api(project(":manami-app"))
    api("no.tornado:tornadofx:1.7.20")
    api("no.tornado:tornadofx-controlsfx:0.1.1")
    api("eu.hansolo:tilesfx:17.0.11")
    api("org.openjfx:javafx-graphics:17.0.1:win")
    api("org.openjfx:javafx-graphics:17.0.1:linux")
    api("org.openjfx:javafx-graphics:17.0.1:mac")
}

javafx {
    version = "17"
    modules = listOf(
        "javafx.base",
        "javafx.controls",
        "javafx.graphics",
        "javafx.web",
    )
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
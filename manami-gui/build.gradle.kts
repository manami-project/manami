plugins {
    kotlin("jvm")
    id("java-library")
    id("org.openjfx.javafxplugin") version("0.0.14")
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
    implementation(platform(kotlin("bom", "1.9.20")))
    api(kotlin("stdlib"))
    api(project(":manami-app"))
    api("no.tornado:tornadofx:1.7.20")
    api("no.tornado:tornadofx-controlsfx:0.1.1")
    api("org.openjfx:javafx-graphics:22-ea+16:win")
    api("org.openjfx:javafx-graphics:22-ea+16:linux")
    api("org.openjfx:javafx-graphics:22-ea+16:mac")
}

javafx {
    version = "21"
    modules = listOf(
        "javafx.base",
        "javafx.controls",
        "javafx.graphics",
        "javafx.web",
    )
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_21.toString()
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
        languageVersion = "1.9"
        apiVersion = "1.9"
    }
}

tasks.test {
    useJUnitPlatform()
    reports.html.required.set(false)
    reports.junitXml.required.set(false)
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
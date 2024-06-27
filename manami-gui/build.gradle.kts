import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.javafxplugin)
    alias(libs.plugins.shadow)
    application
}

group = "io.github.manamiproject"
version = project.findProperty("release.version") as String? ?: ""

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
    api(project(":manami-app"))
    api(libs.kotlin.stdlib)
    api(libs.modb.core)
    api(libs.bundles.tornadofx)

    setOf("win", "linux", "mac").forEach { os ->
        libs.bundles.javafx.get().forEach { dependency ->
            implementation(dependency) {
                artifact {
                    classifier = os
                }
            }
        }
    }
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

val mainClassPath = "io.github.manamiproject.manami.gui.StartKt"
application {
    mainClass = mainClassPath
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        archiveVersion.set("")
        manifest {
            attributes["Main-Class"] = mainClassPath
        }
        exclude(".gitemptydir")
        archiveFileName = "manami.jar"
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
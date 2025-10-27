import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kover)
}

val githubUsername = "manami-project"
val kotlinVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2 // most recent stable kotlin version for language and std lib

repositories {
    google()
    mavenCentral()
    maven {
        name = "modb-app"
        url = uri("https://maven.pkg.github.com/$githubUsername/modb-app")
        credentials {
            username = parameter("GH_USERNAME", githubUsername)
            password = parameter("GH_PACKAGES_READ_TOKEN")
        }
    }
}

dependencies {
    implementation(project(":manami-app"))
    implementation(libs.bundles.modb)
    implementation(libs.jetbrains.compose.runtime.desktop)
    implementation(compose.desktop.currentOs)
    implementation(libs.jetbrains.compose.material3.desktop)
    implementation(libs.jetbrains.compose.material.icons.extended.desktop)
    implementation("org.jetbrains.compose.desktop:desktop-jvm-macos-arm64:1.9.1")
}

kotlin {
    jvmToolchain(JavaVersion.VERSION_21.toString().toInt())
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        apiVersion.set(kotlinVersion)
        languageVersion.set(kotlinVersion)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    reports.html.required.set(false)
    reports.junitXml.required.set(true)
    maxParallelForks = rootProject.extra["maxParallelForks"] as Int
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

compose.desktop {
    application {
        mainClass = "io.github.manamiproject.manami.gui.MainKt"

        nativeDistributions {
            targetFormats(
                TargetFormat.Deb,
                TargetFormat.Dmg,
                TargetFormat.Exe,
                TargetFormat.Rpm,
            )
            packageName = "manami"
            packageVersion = "4.0.0"
        }
    }
}

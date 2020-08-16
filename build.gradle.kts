plugins {
    kotlin("jvm") version "1.3.72" // Kotlin JVM plugin to add support for Kotlin.
    application // Apply the application plugin to add support for building a CLI application.
}

repositories {
    jcenter()
    maven {
        url = uri("https://dl.bintray.com/manami-project/maven")
    }
}

group = "io.github.manamiproject"
version = project.findProperty("releaseVersion") as String? ?: ""
val projectName = "manami"

dependencies {
    api(kotlin("stdlib-jdk8"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")
    api("io.github.manamiproject:modb-core:2.1.0")
    api("io.github.manamiproject:modb-anidb:1.0.5")
    api("io.github.manamiproject:modb-anime-planet:1.0.1")
    api("io.github.manamiproject:modb-kitsu:1.0.5")
    api("io.github.manamiproject:modb-mal:1.0.5")
    api("io.github.manamiproject:modb-notify:1.0.5")
    api("io.github.manamiproject:modb-db-parser:1.0.0")

    testImplementation("io.github.manamiproject:modb-test:1.0.1")
}

val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = Versions.JVM_TARGET
    freeCompilerArgs = listOf("-Xinline-classes")
}

val compileTestKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = Versions.JVM_TARGET
}

tasks.withType<Test> {
    useJUnitPlatform()
    reports.html.isEnabled = false
    reports.junitXml.isEnabled = false
    maxParallelForks = Runtime.getRuntime().availableProcessors()
}

object Versions {
    const val JVM_TARGET = "11"
}

//application {
    // Define the main class for the application.
//    mainClassName = "io.github.manamiproject.manami.AppKt"
//}

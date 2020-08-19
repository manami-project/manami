plugins {
    kotlin("jvm")
    id("org.openjfx.javafxplugin") version("0.0.9")
}

repositories {
    jcenter()
    maven {
        url = uri("https://dl.bintray.com/manami-project/maven")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":manami-app"))
    implementation("no.tornado:tornadofx:1.7.20")
}

javafx {
    version = "14"
    modules = listOf(
            "javafx.controls"
    )
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
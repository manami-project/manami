plugins {
    kotlin("jvm") version "1.4.31"
    application
}

allprojects {
    repositories {
        jcenter()
        maven {
            url = uri("https://dl.bintray.com/manami-project/maven")
        }
    }

    group = "io.github.manamiproject"
    version = "1.0"
}

subprojects {
    apply {
        plugin("application")
        plugin("org.jetbrains.kotlin.jvm")
    }

    dependencies {
        implementation(platform(kotlin("bom")))
        implementation(kotlin("reflect"))

        api(kotlin("stdlib-jdk8"))
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
}

object Versions {
    const val JVM_TARGET = "14"
}
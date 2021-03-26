import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.4.31"
    application
    id("com.github.johnrengelman.shadow") version("6.1.0")
}

allprojects {
    apply {
        plugin("java-library")
        plugin("org.jetbrains.kotlin.jvm")
    }

    repositories {
        jcenter()
        maven {
            url = uri("https://dl.bintray.com/manami-project/maven")
        }
    }

    group = "io.github.manamiproject"
    version = project.findProperty("releaseVersion") as String? ?: ""
}

subprojects {
    apply {
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
        freeCompilerArgs = listOf("-Xinline-classes", "-Xopt-in=kotlin.RequiresOptIn")
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

dependencies {
    api(project(":manami-app"))
    api(project(":manami-gui"))
}

val mainClassPath = "io.github.manamiproject.manami.gui.StartKt"
application {
    mainClass.set(mainClassPath)
    mainClassName = mainClassPath
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        archiveVersion.set("")
        manifest {
            attributes["Main-Class"] = mainClassPath
        }
        exclude(".gitemptydir")
    }
}

object Versions {
    const val JVM_TARGET = "14"
}
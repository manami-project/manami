import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.5.10"
    application
    id("com.github.johnrengelman.shadow") version("6.1.0")
}
val githubUsername = "manami-project"
allprojects {
    apply {
        plugin("java-library")
        plugin("org.jetbrains.kotlin.jvm")
    }

    repositories {
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/$githubUsername/modb-anidb")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: githubUsername
                password = project.findProperty("gpr.key") as String? ?: ""
            }
        }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/$githubUsername/modb-anilist")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: githubUsername
                password = project.findProperty("gpr.key") as String? ?: ""
            }
        }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/$githubUsername/modb-anime-planet")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: githubUsername
                password = project.findProperty("gpr.key") as String? ?: ""
            }
        }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/$githubUsername/modb-kitsu")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: githubUsername
                password = project.findProperty("gpr.key") as String? ?: ""
            }
        }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/$githubUsername/modb-mal")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: githubUsername
                password = project.findProperty("gpr.key") as String? ?: ""
            }
        }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/$githubUsername/modb-notify")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: githubUsername
                password = project.findProperty("gpr.key") as String? ?: ""
            }
        }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/$githubUsername/modb-db-parser")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: githubUsername
                password = project.findProperty("gpr.key") as String? ?: ""
            }
        }
    }

    group = "io.github.manamiproject"
    version = project.findProperty("release.version") as String? ?: ""
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
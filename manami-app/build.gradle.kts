import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kover)
    `java-library`
}

val githubUsername = "manami-project"
val kotlinVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2 // most recent stable kotlin version for language and std lib

repositories {
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
    dependsOn("generateBuildConfig")
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

kover {
    reports {
        filters {
            excludes {
                annotatedBy("io.github.manamiproject.modb.core.coverage.KoverIgnore")
            }
        }
    }
}

fun parameter(name: String, default: String = ""): String {
    val env = System.getenv(name) ?: ""
    if (env.isNotBlank()) {
        return env
    }

    val property = project.findProperty(name) as String? ?: ""
    if (property.isNotBlank()) {
        return property
    }

    return default
}

// The following code is needed to generate the build version in the code and make it available at runtime
val generatedDir = layout.projectDirectory.dir("src/gen/kotlin")

tasks.register("generateBuildConfig") {
    val outputDir = generatedDir.dir("io/github/manamiproject/manami/app/versioning").asFile
    outputs.dir(outputDir)

    doLast {
        val version = parameter("manami.release.version", "0.0.0")
        val file = File(outputDir, "BuildVersion.kt")
        file.parentFile.mkdirs()
        file.writeText(
            """
            package io.github.manamiproject.manami.app.versioning

            import io.github.manamiproject.modb.core.coverage.KoverIgnore

            @KoverIgnore
            internal object BuildVersion {
                const val VERSION = "$version"
            }
            """.trimIndent()
        )
    }
}

sourceSets.main {
    kotlin.srcDir(generatedDir)
}
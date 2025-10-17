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

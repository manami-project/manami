plugins {
    kotlin("jvm")
}

repositories {
    jcenter()
    maven {
        url = uri("https://dl.bintray.com/manami-project/maven")
    }
}

dependencies {
    api(kotlin("stdlib-jdk8"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("io.github.manamiproject:modb-core:2.2.1")
    api("io.github.manamiproject:modb-anidb:1.0.8")
    api("io.github.manamiproject:modb-anime-planet:1.0.4")
    api("io.github.manamiproject:modb-kitsu:1.0.8")
    api("io.github.manamiproject:modb-mal:1.0.8")
    api("io.github.manamiproject:modb-notify:1.0.8")
    api("io.github.manamiproject:modb-db-parser:1.0.3")

    testImplementation("io.github.manamiproject:modb-test:1.0.3")
}

object Versions {
    const val JVM_TARGET = "11"
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
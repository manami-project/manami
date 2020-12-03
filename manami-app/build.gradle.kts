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
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    api("io.github.manamiproject:modb-core:3.0.1")
    api("io.github.manamiproject:modb-db-parser:2.0.1")

    implementation(kotlin("reflect"))
    implementation("io.github.manamiproject:modb-anidb:2.0.1")
    implementation("io.github.manamiproject:modb-anilist:3.0.1")
    implementation("io.github.manamiproject:modb-anime-planet:2.0.1")
    implementation("io.github.manamiproject:modb-kitsu:2.0.1")
    implementation("io.github.manamiproject:modb-mal:2.0.1")
    implementation("io.github.manamiproject:modb-notify:2.0.1")

    testImplementation("io.github.manamiproject:modb-test:1.2.0")
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
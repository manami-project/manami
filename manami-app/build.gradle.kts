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
    api("io.github.manamiproject:modb-core:2.1.2")
    api("io.github.manamiproject:modb-anidb:1.0.7")
    api("io.github.manamiproject:modb-anime-planet:1.0.3")
    api("io.github.manamiproject:modb-kitsu:1.0.7")
    api("io.github.manamiproject:modb-mal:1.0.7")
    api("io.github.manamiproject:modb-notify:1.0.7")
    api("io.github.manamiproject:modb-db-parser:1.0.2")

    testImplementation("io.github.manamiproject:modb-test:1.0.2")
}
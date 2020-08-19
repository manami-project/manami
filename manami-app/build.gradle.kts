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
repositories {
    maven {
        url = uri("https://dl.bintray.com/manami-project/maven")
    }
}

dependencies {
    api("io.github.manamiproject:modb-core:3.2.3")
    api("io.github.manamiproject:modb-db-parser:2.0.5")
    api("org.slf4j:slf4j-api:1.7.30")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
    implementation("io.github.manamiproject:modb-anidb:2.1.2")
    implementation("io.github.manamiproject:modb-anilist:3.0.5")
    implementation("io.github.manamiproject:modb-anime-planet:2.0.5")
    implementation("io.github.manamiproject:modb-kitsu:2.0.5")
    implementation("io.github.manamiproject:modb-mal:2.0.6")
    implementation("io.github.manamiproject:modb-notify:2.0.5")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.commons:commons-text:1.9")

    testImplementation("io.github.manamiproject:modb-test:1.2.3")
}
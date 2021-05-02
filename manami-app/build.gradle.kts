dependencies {
    api("io.github.manamiproject:modb-core:4.0.0")
    api("io.github.manamiproject:modb-db-parser:2.0.6")
    api("org.slf4j:slf4j-api:1.7.30")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
    implementation("io.github.manamiproject:modb-anidb:2.1.3")
    implementation("io.github.manamiproject:modb-anilist:3.0.6")
    implementation("io.github.manamiproject:modb-anime-planet:2.0.6")
    implementation("io.github.manamiproject:modb-kitsu:2.0.6")
    implementation("io.github.manamiproject:modb-mal:2.0.8")
    implementation("io.github.manamiproject:modb-notify:2.0.6")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.commons:commons-text:1.9")

    testImplementation("io.github.manamiproject:modb-test:1.2.4")
}
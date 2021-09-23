dependencies {
    api("io.github.manamiproject:modb-core:7.0.2")
    api("io.github.manamiproject:modb-db-parser:3.1.2")
    api("org.slf4j:slf4j-api:1.7.32")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2-native-mt")
    implementation("io.github.manamiproject:modb-anidb:3.0.7")
    implementation("io.github.manamiproject:modb-anilist:4.0.6")
    implementation("io.github.manamiproject:modb-anime-planet:3.1.7")
    implementation("io.github.manamiproject:modb-anisearch:1.0.7")
    implementation("io.github.manamiproject:modb-kitsu:3.0.5")
    implementation("io.github.manamiproject:modb-livechart:1.0.6")
    implementation("io.github.manamiproject:modb-mal:3.0.6")
    implementation("io.github.manamiproject:modb-notify:3.0.6")
    implementation("ch.qos.logback:logback-classic:1.2.6")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.commons:commons-text:1.9")

    testImplementation("io.github.manamiproject:modb-test:1.2.10")
}
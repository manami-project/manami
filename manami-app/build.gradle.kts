plugins {
    id("manami-base-plugin")
    id("com.github.nbaztec.coveralls-jacoco") version "1.2.13"
    jacoco
}

dependencies {
    api(kotlin("stdlib-jdk8"))
    api("io.github.manamiproject:modb-core:7.1.0")
    api("io.github.manamiproject:modb-db-parser:3.1.2")

    implementation(platform(kotlin("bom")))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2-native-mt")
    implementation("io.github.manamiproject:modb-anidb:3.0.7")
    implementation("io.github.manamiproject:modb-anilist:4.0.6")
    implementation("io.github.manamiproject:modb-anime-planet:3.1.7")
    implementation("io.github.manamiproject:modb-anisearch:1.0.7")
    implementation("io.github.manamiproject:modb-kitsu:3.0.6")
    implementation("io.github.manamiproject:modb-livechart:1.0.7")
    implementation("io.github.manamiproject:modb-mal:3.0.6")
    implementation("io.github.manamiproject:modb-notify:3.0.6")
    implementation("ch.qos.logback:logback-classic:1.2.7")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.commons:commons-text:1.9")

    testImplementation("io.github.manamiproject:modb-test:1.2.11")
}

coverallsJacoco {
    reportPath = "$buildDir/reports/jacoco/test/jacocoFullReport.xml"
}

tasks.jacocoTestReport {
    reports {
        html.required.set(false)
        xml.required.set(true)
        xml.outputLocation.set(file("$buildDir/reports/jacoco/test/jacocoFullReport.xml"))
    }
    dependsOn(allprojects.map { it.tasks.named<Test>("test") })
}
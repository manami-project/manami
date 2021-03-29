plugins {
    id("org.openjfx.javafxplugin") version("0.0.9")
}

dependencies {
    implementation(project(":manami-app"))
    implementation("no.tornado:tornadofx:1.7.20")
    implementation("no.tornado:tornadofx-controlsfx:0.1.1")
    implementation("eu.hansolo:tilesfx:11.48")
}

javafx {
    version = "14"
    modules = listOf(
        "javafx.controls"
    )
}
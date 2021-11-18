plugins {
    id("manami-base-plugin")
    id("org.openjfx.javafxplugin") version("0.0.10")
}

dependencies {
    implementation(project(":manami-app"))
    implementation("no.tornado:tornadofx:1.7.20")
    implementation("no.tornado:tornadofx-controlsfx:0.1.1")
    implementation("eu.hansolo:tilesfx:17.0.7")
    implementation("org.openjfx:javafx-graphics:14:win")
    implementation("org.openjfx:javafx-graphics:14:linux")
    implementation("org.openjfx:javafx-graphics:14:mac")
}

javafx {
    version = "14"
    modules = listOf(
        "javafx.base",
        "javafx.controls",
        "javafx.graphics",
        "javafx.web",
    )
}
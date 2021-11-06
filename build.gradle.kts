import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("manami-base-plugin")
    application
    id("com.github.johnrengelman.shadow") version("6.1.0")
}

allprojects {
    group = "io.github.manamiproject"
    version = project.findProperty("release.version") as String? ?: ""
}

dependencies {
    api(project(":manami-gui"))
    api(project(":manami-app"))
}

val mainClassPath = "io.github.manamiproject.manami.gui.StartKt"
application {
    mainClass.set(mainClassPath)
    mainClassName = mainClassPath
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        archiveVersion.set("")
        manifest {
            attributes["Main-Class"] = mainClassPath
        }
        exclude(".gitemptydir")
    }
}
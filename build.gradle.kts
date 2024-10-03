// Resolves "The Kotlin Gradle plugin was loaded multiple times" message
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}
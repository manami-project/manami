package io.github.manamiproject.manami.app.import.parser.manami

internal data class SemanticVersion(private val version: String = "0.0.0") {

    init {
        require(Regex("[0-9]+\\.[0-9]+\\.[0-9]+").matches(version)) { "Version must be of format NUMBER.NUMBER.NUMBER" }
    }

    fun isNewerThan(other: SemanticVersion): Boolean {
        if (major() < other.major()) return false
        if (major() > other.major()) return true

        if (minor() < other.minor()) return false
        if (minor() > other.minor()) return true

        if (patch() < other.patch()) return false
        if (patch() > other.patch()) return true

        return false
    }

    fun isOlderThan(other: SemanticVersion): Boolean {
        if (this == other) return false

        return !isNewerThan(other)
    }

    fun major() = version.split('.')[0].toInt()

    fun minor() = version.split('.')[1].toInt()

    fun patch() = version.split('.')[2].toInt()

    override fun toString(): String = version
}
package io.github.manamiproject.manami.app.versioning

interface VersionProvider {

    fun version(): SemanticVersion
}


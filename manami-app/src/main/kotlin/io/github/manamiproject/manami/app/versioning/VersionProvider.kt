package io.github.manamiproject.manami.app.versioning

internal interface VersionProvider {

    fun version(): SemanticVersion
}


package io.github.manamiproject.manami.app.versioning

internal interface VersionProvider {

    suspend fun version(): SemanticVersion
}


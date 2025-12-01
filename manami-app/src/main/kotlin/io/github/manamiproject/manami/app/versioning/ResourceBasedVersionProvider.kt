package io.github.manamiproject.manami.app.versioning

object ResourceBasedVersionProvider: VersionProvider {

    override suspend fun version(): SemanticVersion = SemanticVersion(BuildVersion.VERSION)
}
package io.github.manamiproject.manami.app.versioning

import io.github.manamiproject.modb.core.loadResource

object ResourceBasedVersionProvider: VersionProvider {

    override suspend fun version(): SemanticVersion = SemanticVersion(loadResource("manami.version"))
}
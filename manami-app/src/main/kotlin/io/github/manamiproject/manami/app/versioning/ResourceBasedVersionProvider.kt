package io.github.manamiproject.manami.app.versioning

import io.github.manamiproject.modb.core.loadResource
import kotlinx.coroutines.runBlocking

object ResourceBasedVersionProvider: VersionProvider {

    override fun version(): SemanticVersion = SemanticVersion(runBlocking { loadResource("manami.version") })
}
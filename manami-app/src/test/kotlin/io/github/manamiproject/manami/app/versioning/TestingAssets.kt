package io.github.manamiproject.manami.app.versioning

import io.github.manamiproject.modb.test.shouldNotBeInvoked

object TestVersionProvider: VersionProvider {
    override fun version(): SemanticVersion = shouldNotBeInvoked()
}
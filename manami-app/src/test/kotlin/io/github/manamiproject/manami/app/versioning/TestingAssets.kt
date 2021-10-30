package io.github.manamiproject.manami.app.versioning

import io.github.manamiproject.modb.test.shouldNotBeInvoked

internal object TestVersionProvider: VersionProvider {
    override fun version(): SemanticVersion = shouldNotBeInvoked()
}
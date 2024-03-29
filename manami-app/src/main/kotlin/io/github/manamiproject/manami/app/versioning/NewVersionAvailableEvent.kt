package io.github.manamiproject.manami.app.versioning

import io.github.manamiproject.manami.app.events.Event

data class NewVersionAvailableEvent(val version: SemanticVersion) : Event
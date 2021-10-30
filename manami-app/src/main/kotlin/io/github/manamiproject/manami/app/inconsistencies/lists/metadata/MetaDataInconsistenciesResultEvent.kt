package io.github.manamiproject.manami.app.inconsistencies.lists.metadata

import io.github.manamiproject.manami.app.events.Event

data class MetaDataInconsistenciesResultEvent(val numberOfAffectedEntries: Int): Event
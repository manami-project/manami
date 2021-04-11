package io.github.manamiproject.manami.app.inconsistencies.metadata

import io.github.manamiproject.manami.app.state.events.Event

data class MetaDataInconsistenciesResultEvent(val numberOfAffectedEntries: Int): Event
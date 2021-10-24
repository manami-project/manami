package io.github.manamiproject.manami.app.inconsistencies.lists.deadentries

import io.github.manamiproject.manami.app.state.events.Event

data class DeadEntriesInconsistenciesResultEvent(val numberOfAffectedEntries: Int): Event
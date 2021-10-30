package io.github.manamiproject.manami.app.inconsistencies.lists.deadentries

import io.github.manamiproject.manami.app.events.Event

data class DeadEntriesInconsistenciesResultEvent(val numberOfAffectedEntries: Int): Event
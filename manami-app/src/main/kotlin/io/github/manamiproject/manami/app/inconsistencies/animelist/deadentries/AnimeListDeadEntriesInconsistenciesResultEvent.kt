package io.github.manamiproject.manami.app.inconsistencies.animelist.deadentries

import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.state.events.Event

data class AnimeListDeadEntriesInconsistenciesResultEvent(
    val entries: List<AnimeListEntry>
): Event
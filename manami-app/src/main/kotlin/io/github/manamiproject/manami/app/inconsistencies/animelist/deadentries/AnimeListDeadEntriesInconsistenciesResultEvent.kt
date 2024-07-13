package io.github.manamiproject.manami.app.inconsistencies.animelist.deadentries

import io.github.manamiproject.manami.app.events.Event
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry

data class AnimeListDeadEntriesInconsistenciesResultEvent(
    val entries: List<AnimeListEntry>,
): Event
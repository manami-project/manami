package io.github.manamiproject.manami.app.inconsistencies

import io.github.manamiproject.manami.app.inconsistencies.animelist.metadata.AnimeListMetaDataDiff
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry

interface InconsistenciesHandler {

    fun fixAnimeListEntryMetaDataInconsistencies(currentEntry: AnimeListEntry, replacementEntry: AnimeListEntry)

    suspend fun findInconsistencies()

    fun setForEdit(diff: AnimeListMetaDataDiff)
}
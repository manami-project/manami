package io.github.manamiproject.manami.app.inconsistencies

import io.github.manamiproject.manami.app.inconsistencies.animelist.metadata.AnimeListMetaDataDiff

interface InconsistenciesHandler {

    fun fixAnimeListEntryMetaDataInconsistencies(diff: AnimeListMetaDataDiff)

    fun findInconsistencies(config: InconsistenciesSearchConfig)

    fun fixMetaDataInconsistencies()

    fun fixDeadEntryInconsistencies()
}
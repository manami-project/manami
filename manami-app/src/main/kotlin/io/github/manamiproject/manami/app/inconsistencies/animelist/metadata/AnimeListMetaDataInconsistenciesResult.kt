package io.github.manamiproject.manami.app.inconsistencies.animelist.metadata

import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry

data class AnimeListMetaDataInconsistenciesResult(
    val entries: List<AnimeListMetaDataDiff> = emptyList(),
)

data class AnimeListMetaDataDiff(
    val currentEntry: AnimeListEntry,
    val replacementEntry: AnimeListEntry,
)
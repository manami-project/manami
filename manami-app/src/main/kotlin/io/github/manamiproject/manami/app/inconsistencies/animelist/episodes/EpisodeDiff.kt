package io.github.manamiproject.manami.app.inconsistencies.animelist.episodes

import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.modb.core.models.Episodes

data class EpisodeDiff(
    val animeListEntry: AnimeListEntry,
    val numberOfFiles: Episodes,
)

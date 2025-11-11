package io.github.manamiproject.manami.app.events

import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.modb.core.anime.Anime

data class AnimeListModificationState(
    val isAddAnimeEntryDataRunning: Boolean = false,
    val addAnimeEntryData: Anime? = null,
    val editAnimeEntryData: AnimeListEntry? = null,
)

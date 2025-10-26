package io.github.manamiproject.manami.app.events

import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry

/**
 * @since 4.0.0
 */
data class AnimeListState(
    val entries: Collection<AnimeListEntry> = emptyList(),
)
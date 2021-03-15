package io.github.manamiproject.manami.app.import.parser

import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import java.net.URI

data class ParsedFile(
    val animeListEntries: Set<AnimeListEntry> = emptySet(),
    val watchListEntries: Set<URI> = emptySet(),
    val ignoreListEntries:  Set<URI> = emptySet(),
)
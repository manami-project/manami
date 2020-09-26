package io.github.manamiproject.manami.app.fileimport.parser

import io.github.manamiproject.manami.app.models.AnimeListEntry
import java.net.URL

data class ParsedFile(
    val animeListEntries: Set<AnimeListEntry> = emptySet(),
    val watchListEntries: Set<URL> = emptySet(),
    val ignoreListEntries:  Set<URL> = emptySet(),
)
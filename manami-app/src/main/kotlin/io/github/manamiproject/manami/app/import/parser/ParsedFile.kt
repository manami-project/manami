package io.github.manamiproject.manami.app.import.parser

import java.net.URL

data class ParsedFile(
    val watchListEntries: List<URL> = emptyList(),
    val ignoreListEntries:  List<URL> = emptyList()
)
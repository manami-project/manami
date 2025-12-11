package io.github.manamiproject.manami.app.file

data class SerializableManamiFile(
    val version: String,
    val animeListEntries: List<SerializableAnimeListEntry>,
    val watchListEntries: List<String>,
    val ignoreListEntries:  List<String>,
)

data class SerializableAnimeListEntry(
    val link: String,
    val title: String,
    val thumbnail: String,
    val episodes: String,
    val type: String,
    val location: String,
)
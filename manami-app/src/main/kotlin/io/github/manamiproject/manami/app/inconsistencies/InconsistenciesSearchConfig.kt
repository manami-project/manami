package io.github.manamiproject.manami.app.inconsistencies

data class InconsistenciesSearchConfig(
    val checkAnimeListMetaData: Boolean = false,
    val checkAnimeListDeadEntries: Boolean = false,
    val checkAnimeListEpisodes: Boolean = false,
    val checkMetaData: Boolean = true,
    val checkDeadEntries: Boolean = true,
)
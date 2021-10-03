package io.github.manamiproject.manami.app.inconsistencies

data class InconsistenciesSearchConfig(
    val checkAnimeListMetaData: Boolean = false,
    val checkMetaData: Boolean = true,
    val checkDeadEntries: Boolean = true,
)
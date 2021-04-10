package io.github.manamiproject.manami.app.inconsistencies

data class InconsistenciesSearchConfig(
    val checkAnimeListMetaData: Boolean = true,
    val checkMetaData: Boolean = true,
    val checkDeadEntries: Boolean = true,
    val checkCrc: Boolean = false
)
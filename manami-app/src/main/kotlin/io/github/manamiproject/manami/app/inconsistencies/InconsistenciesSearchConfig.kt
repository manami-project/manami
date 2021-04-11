package io.github.manamiproject.manami.app.inconsistencies

data class InconsistenciesSearchConfig(
    val checkMetaData: Boolean = true,
    val checkDeadEntries: Boolean = true,
)
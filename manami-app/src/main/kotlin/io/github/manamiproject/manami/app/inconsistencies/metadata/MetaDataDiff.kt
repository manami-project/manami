package io.github.manamiproject.manami.app.inconsistencies.metadata

data class MetaDataDiff<T>(
    val currentEntry: T,
    val newEntry: T,
)
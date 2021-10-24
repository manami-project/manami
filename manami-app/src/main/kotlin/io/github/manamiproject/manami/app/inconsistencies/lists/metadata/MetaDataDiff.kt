package io.github.manamiproject.manami.app.inconsistencies.lists.metadata

data class MetaDataDiff<T>(
    val currentEntry: T,
    val newEntry: T,
)
package io.github.manamiproject.manami.app.migration

import io.github.manamiproject.manami.app.events.Event

data class MetaDataMigrationProgressEvent(
    val finishedTasks: Int,
    val numberOfTasks: Int,
): Event
package io.github.manamiproject.manami.app.inconsistencies

import io.github.manamiproject.manami.app.state.events.Event

data class InconsistenciesProgressEvent(
    val finishedTasks: Int,
    val numberOfTasks: Int,
): Event

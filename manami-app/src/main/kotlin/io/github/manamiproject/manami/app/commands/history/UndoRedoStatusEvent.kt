package io.github.manamiproject.manami.app.commands.history

import io.github.manamiproject.manami.app.events.Event

data class UndoRedoStatusEvent(
    val isUndoPossible: Boolean,
    val isRedoPossible: Boolean,
): Event
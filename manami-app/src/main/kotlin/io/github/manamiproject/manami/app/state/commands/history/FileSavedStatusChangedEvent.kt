package io.github.manamiproject.manami.app.state.commands.history

import io.github.manamiproject.manami.app.state.events.Event

data class FileSavedStatusChangedEvent(val isFileSaved: Boolean) : Event
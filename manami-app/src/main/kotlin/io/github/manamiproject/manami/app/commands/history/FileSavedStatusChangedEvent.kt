package io.github.manamiproject.manami.app.commands.history

import io.github.manamiproject.manami.app.events.Event

data class FileSavedStatusChangedEvent(val isFileSaved: Boolean) : Event
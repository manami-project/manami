package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.events.Event

data class FileOpenedEvent(val fileName: String): Event
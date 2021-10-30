package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.events.Event

data class SavedAsFileEvent(val fileName: String): Event
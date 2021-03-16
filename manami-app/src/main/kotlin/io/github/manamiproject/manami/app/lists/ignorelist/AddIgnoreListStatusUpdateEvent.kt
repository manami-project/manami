package io.github.manamiproject.manami.app.lists.ignorelist

import io.github.manamiproject.manami.app.state.events.Event

data class AddIgnoreListStatusUpdateEvent(val finishedTasks: Int, val tasks: Int): Event
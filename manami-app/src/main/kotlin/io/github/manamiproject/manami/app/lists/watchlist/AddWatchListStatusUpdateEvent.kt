package io.github.manamiproject.manami.app.lists.watchlist

import io.github.manamiproject.manami.app.events.Event

data class AddWatchListStatusUpdateEvent(val finishedTasks: Int, val tasks: Int): Event
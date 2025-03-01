package io.github.manamiproject.manami.app.search

import io.github.manamiproject.manami.app.events.Event
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry

data class FileSearchAnimeListResultsEvent(val anime: Collection<AnimeListEntry>): Event
data class FileSearchWatchListResultsEvent(val anime: Collection<WatchListEntry>): Event
data class FileSearchIgnoreListResultsEvent(val anime: Collection<IgnoreListEntry>): Event
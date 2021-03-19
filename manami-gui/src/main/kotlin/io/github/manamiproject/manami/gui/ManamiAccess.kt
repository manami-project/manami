package io.github.manamiproject.manami.gui

import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.app.ManamiApp
import io.github.manamiproject.manami.app.extensions.castToSet
import io.github.manamiproject.manami.app.import.ImportFinishedEvent
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.AddIgnoreListStatusUpdateEvent
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.AddWatchListStatusUpdateEvent
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.commands.history.FileSavedStatusChangedEvent
import io.github.manamiproject.manami.app.state.events.ListChangedEvent
import io.github.manamiproject.manami.app.state.events.ListChangedEvent.EventType.ADDED
import io.github.manamiproject.manami.app.state.events.ListChangedEvent.EventType.REMOVED
import io.github.manamiproject.manami.app.state.events.ListChangedEvent.ListType.*
import tornadofx.Controller
import tornadofx.FXEvent

class ManamiAccess(private val manami: ManamiApp = manamiInstance) : Controller(), ManamiApp by manami {

    init {
        (manami as Manami).eventMapping {
            fire(
                when(this) {
                    is ListChangedEvent<*> -> mapListChangeEvent(this)
                    is AddWatchListStatusUpdateEvent -> AddWatchListStatusUpdateGuiEvent(this.finishedTasks, this.tasks)
                    is AddIgnoreListStatusUpdateEvent -> AddIgnoreListStatusUpdateGuiEvent(this.finishedTasks, this.tasks)
                    is FileSavedStatusChangedEvent -> FileSavedStatusChangedGuiEvent(this.isFileSaved)
                    is ImportFinishedEvent -> ImportFinishedGuiEvent
                    else -> throw IllegalStateException("Unmapped event: [${this::class.simpleName}]")
                }
            )
        }
    }

    private fun mapListChangeEvent(listChangedEvent: ListChangedEvent<*>): GuiEvent {
        return when(listChangedEvent.list) {
            ANIME_LIST -> createAnimeListEvent(listChangedEvent)
            WATCH_LIST -> createWatchListEvent(listChangedEvent)
            IGNORE_LIST -> createIgnoreListEvent(listChangedEvent)
        }
    }

    private fun createAnimeListEvent(listChangedEvent: ListChangedEvent<*>): GuiEvent {
        return when(listChangedEvent.type) {
            ADDED -> AddAnimeListEntryGuiEvent(listChangedEvent.obj.castToSet())
            REMOVED -> RemoveAnimeListEntryGuiEvent(listChangedEvent.obj.castToSet())
        }
    }

    private fun createWatchListEvent(listChangedEvent: ListChangedEvent<*>): GuiEvent {
        return when(listChangedEvent.type) {
            ADDED -> AddWatchListEntryGuiEvent(listChangedEvent.obj.castToSet())
            REMOVED -> RemoveWatchListEntryGuiEvent(listChangedEvent.obj.castToSet())
        }
    }

    private fun createIgnoreListEvent(listChangedEvent: ListChangedEvent<*>): GuiEvent {
        return when(listChangedEvent.type) {
            ADDED -> AddIgnoreListEntryGuiEvent(listChangedEvent.obj.castToSet())
            REMOVED -> RemoveIgnoreListEntryGuiEvent(listChangedEvent.obj.castToSet())
        }
    }
}

sealed class GuiEvent : FXEvent()

data class AddAnimeListEntryGuiEvent(val entry: Set<AnimeListEntry>) : GuiEvent()
data class RemoveAnimeListEntryGuiEvent(val entry: Set<AnimeListEntry>) : GuiEvent()

data class AddWatchListEntryGuiEvent(val entry: Set<WatchListEntry>) : GuiEvent()
data class RemoveWatchListEntryGuiEvent(val entry: Set<WatchListEntry>) : GuiEvent()
data class AddWatchListStatusUpdateGuiEvent(val finishedTasks: Int, val tasks: Int): GuiEvent()

data class AddIgnoreListEntryGuiEvent(val entry: Set<IgnoreListEntry>) : GuiEvent()
data class RemoveIgnoreListEntryGuiEvent(val entry: Set<IgnoreListEntry>) : GuiEvent()
data class AddIgnoreListStatusUpdateGuiEvent(val finishedTasks: Int, val tasks: Int): GuiEvent()

data class FileSavedStatusChangedGuiEvent(val isFileSaved: Boolean): GuiEvent()

object ImportFinishedGuiEvent: GuiEvent()
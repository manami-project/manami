package io.github.manamiproject.manami.gui

import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.app.ManamiApp
import io.github.manamiproject.manami.app.models.AnimeListEntry
import io.github.manamiproject.manami.app.models.IgnoreListEntry
import io.github.manamiproject.manami.app.models.WatchListEntry
import io.github.manamiproject.manami.app.state.events.ListChangedEvent
import io.github.manamiproject.manami.app.state.events.ListChangedEvent.EventType.ADDED
import io.github.manamiproject.manami.app.state.events.ListChangedEvent.EventType.REMOVED
import io.github.manamiproject.manami.app.state.events.ListChangedEvent.ListType.*
import io.github.manamiproject.manami.app.extensions.castToSet
import tornadofx.Controller
import tornadofx.FXEvent

class ManamiAccess(private val manami: ManamiApp = manamiInstance) : Controller(), ManamiApp by manami {

    init {
        (manami as Manami).eventMapping {
            fire(
                when(this) {
                    is ListChangedEvent<*> -> mapListChangeEvent(this)
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
            ADDED -> AddAnimeListEntry(listChangedEvent.obj.castToSet())
            REMOVED -> RemoveAnimeListEntry(listChangedEvent.obj.castToSet())
        }
    }

    private fun createWatchListEvent(listChangedEvent: ListChangedEvent<*>): GuiEvent {
        return when(listChangedEvent.type) {
            ADDED -> AddWatchListEntry(listChangedEvent.obj.castToSet())
            REMOVED -> RemoveWatchListEntry(listChangedEvent.obj.castToSet())
        }
    }

    private fun createIgnoreListEvent(listChangedEvent: ListChangedEvent<*>): GuiEvent {
        return when(listChangedEvent.type) {
            ADDED -> AddIgnoreListEntry(listChangedEvent.obj.castToSet())
            REMOVED -> RemoveIgnoreListEntry(listChangedEvent.obj.castToSet())
        }
    }
}

sealed class GuiEvent : FXEvent()

data class AddAnimeListEntry(val entry: Set<AnimeListEntry>) : GuiEvent()
data class RemoveAnimeListEntry(val entry: Set<AnimeListEntry>) : GuiEvent()

data class AddWatchListEntry(val entry: Set<WatchListEntry>) : GuiEvent()
data class RemoveWatchListEntry(val entry: Set<WatchListEntry>) : GuiEvent()

data class AddIgnoreListEntry(val entry: Set<IgnoreListEntry>) : GuiEvent()
data class RemoveIgnoreListEntry(val entry: Set<IgnoreListEntry>) : GuiEvent()
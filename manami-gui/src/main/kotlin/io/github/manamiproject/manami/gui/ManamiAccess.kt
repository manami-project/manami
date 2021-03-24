package io.github.manamiproject.manami.gui

import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.app.ManamiApp
import io.github.manamiproject.manami.app.cache.populator.CachePopulatorFinishedEvent
import io.github.manamiproject.manami.app.extensions.castToSet
import io.github.manamiproject.manami.app.file.FileOpenedEvent
import io.github.manamiproject.manami.app.file.SavedAsFileEvent
import io.github.manamiproject.manami.app.import.ImportFinishedEvent
import io.github.manamiproject.manami.app.lists.ListChangedEvent
import io.github.manamiproject.manami.app.lists.ListChangedEvent.EventType.ADDED
import io.github.manamiproject.manami.app.lists.ListChangedEvent.EventType.REMOVED
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.AddIgnoreListStatusUpdateEvent
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.AddWatchListStatusUpdateEvent
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.relatedanime.RelatedAnimeFinishedEvent
import io.github.manamiproject.manami.app.relatedanime.RelatedAnimeFoundEvent
import io.github.manamiproject.manami.app.relatedanime.RelatedAnimeStatusEvent
import io.github.manamiproject.manami.app.search.*
import io.github.manamiproject.manami.app.state.commands.history.FileSavedStatusChangedEvent
import io.github.manamiproject.manami.app.state.commands.history.UndoRedoStatusEvent
import io.github.manamiproject.manami.app.state.events.EventListType.*
import io.github.manamiproject.manami.app.state.events.Subscribe
import io.github.manamiproject.modb.core.models.Anime
import tornadofx.Controller
import tornadofx.FXEvent

class ManamiAccess(private val manami: ManamiApp = manamiInstance) : Controller(), ManamiApp by manami {

    init {
        (manami as Manami).eventMapping {
            fire(
                when(this) {
                    is FileOpenedEvent -> FileOpenedGuiEvent(this.fileName)
                    is SavedAsFileEvent -> SavedAsFileGuiEvent(this.fileName)
                    is ListChangedEvent<*> -> mapListChangeEvent(this)
                    is AddWatchListStatusUpdateEvent -> AddWatchListStatusUpdateGuiEvent(this.finishedTasks, this.tasks)
                    is AddIgnoreListStatusUpdateEvent -> AddIgnoreListStatusUpdateGuiEvent(this.finishedTasks, this.tasks)
                    is FileSavedStatusChangedEvent -> FileSavedStatusChangedGuiEvent(this.isFileSaved)
                    is ImportFinishedEvent -> ImportFinishedGuiEvent
                    is UndoRedoStatusEvent -> UndoRedoStatusGuiEvent(this.isUndoPossible, this.isRedoPossible)
                    is RelatedAnimeFoundEvent -> mapRelatedAnimeFoundEvent(this)
                    is RelatedAnimeStatusEvent -> mapRelatedAnimeStatusEvent(this)
                    is RelatedAnimeFinishedEvent -> mapRelatedAnimeFinsihedEvent(this)
                    is AnimeSeasonEntryFoundEvent -> AnimeSeasonEntryFoundGuiEvent(this.anime)
                    is AnimeSeasonSearchFinishedEvent -> AnimeSeasonSearchFinishedGuiEvent
                    is CachePopulatorFinishedEvent -> CachePopulatorFinishedGuiEvent
                    is FileSearchAnimeListResultsEvent -> FileSearchAnimeListResultsGuiEvent(this.anime)
                    is FileSearchWatchListResultsEvent -> FileSearchWatchListResultsGuiEvent(this.anime)
                    is FileSearchIgnoreListResultsEvent -> FileSearchIgnoreListResultsGuiEvent(this.anime)
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

    private fun mapRelatedAnimeFoundEvent(event: RelatedAnimeFoundEvent): GuiEvent {
        return when(event.listType) {
            ANIME_LIST -> AnimeListRelatedAnimeFoundGuiEvent(event.anime)
            WATCH_LIST -> throw IllegalStateException("Unsupported list type")
            IGNORE_LIST -> IgnoreListRelatedAnimeFoundGuiEvent(event.anime)
        }
    }

    private fun mapRelatedAnimeStatusEvent(event: RelatedAnimeStatusEvent): GuiEvent {
        return when(event.listType) {
            ANIME_LIST -> AnimeListRelatedAnimeStatusGuiEvent(event.finishedChecking, event.toBeChecked)
            WATCH_LIST -> throw IllegalStateException("Unsupported list type")
            IGNORE_LIST -> IgnoreListRelatedAnimeStatusGuiEvent(event.finishedChecking, event.toBeChecked)
        }
    }

    private fun mapRelatedAnimeFinsihedEvent(event: RelatedAnimeFinishedEvent): GuiEvent {
        return when(event.listType) {
            ANIME_LIST -> AnimeListRelatedAnimeFinishedGuiEvent
            WATCH_LIST -> throw IllegalStateException("Unsupported list type")
            IGNORE_LIST -> IgnoreListRelatedAnimeFinishedGuiEvent
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

data class FileOpenedGuiEvent(val fileName: String): GuiEvent()
data class SavedAsFileGuiEvent(val fileName: String): GuiEvent()

data class AddAnimeListEntryGuiEvent(val entries: Set<AnimeListEntry>) : GuiEvent()
data class RemoveAnimeListEntryGuiEvent(val entries: Set<AnimeListEntry>) : GuiEvent()

data class AddWatchListEntryGuiEvent(val entries: Set<WatchListEntry>) : GuiEvent()
data class RemoveWatchListEntryGuiEvent(val entries: Set<WatchListEntry>) : GuiEvent()
data class AddWatchListStatusUpdateGuiEvent(val finishedTasks: Int, val tasks: Int): GuiEvent()

data class AddIgnoreListEntryGuiEvent(val entries: Set<IgnoreListEntry>) : GuiEvent()
data class RemoveIgnoreListEntryGuiEvent(val entries: Set<IgnoreListEntry>) : GuiEvent()
data class AddIgnoreListStatusUpdateGuiEvent(val finishedTasks: Int, val tasks: Int): GuiEvent()

data class FileSavedStatusChangedGuiEvent(val isFileSaved: Boolean): GuiEvent()
data class UndoRedoStatusGuiEvent(val isUndoPossible: Boolean, val isRedoPossible: Boolean): GuiEvent()

object ImportFinishedGuiEvent: GuiEvent()

data class AnimeListRelatedAnimeFoundGuiEvent(val anime: Anime): GuiEvent()
data class AnimeListRelatedAnimeStatusGuiEvent(val finishedChecking: Int, val toBeChecked: Int): GuiEvent()
object AnimeListRelatedAnimeFinishedGuiEvent: GuiEvent()

data class IgnoreListRelatedAnimeFoundGuiEvent(val anime: Anime): GuiEvent()
data class IgnoreListRelatedAnimeStatusGuiEvent(val finishedChecking: Int, val toBeChecked: Int): GuiEvent()
object IgnoreListRelatedAnimeFinishedGuiEvent: GuiEvent()

data class AnimeSeasonEntryFoundGuiEvent(val anime: Anime): GuiEvent()
object AnimeSeasonSearchFinishedGuiEvent: GuiEvent()

object CachePopulatorFinishedGuiEvent: GuiEvent()

data class FileSearchAnimeListResultsGuiEvent(val anime: Collection<AnimeListEntry>): GuiEvent()
data class FileSearchWatchListResultsGuiEvent(val anime: Collection<WatchListEntry>): GuiEvent()
data class FileSearchIgnoreListResultsGuiEvent(val anime: Collection<IgnoreListEntry>): GuiEvent()
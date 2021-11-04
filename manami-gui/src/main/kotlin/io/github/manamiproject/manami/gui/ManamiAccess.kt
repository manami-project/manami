package io.github.manamiproject.manami.gui

import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.app.ManamiApp
import io.github.manamiproject.manami.app.cache.populator.CachePopulatorFinishedEvent
import io.github.manamiproject.manami.app.cache.populator.NumberOfEntriesPerMetaDataProviderEvent
import io.github.manamiproject.manami.app.extensions.castToSet
import io.github.manamiproject.manami.app.file.FileOpenedEvent
import io.github.manamiproject.manami.app.file.SavedAsFileEvent
import io.github.manamiproject.manami.app.inconsistencies.InconsistenciesCheckFinishedEvent
import io.github.manamiproject.manami.app.inconsistencies.InconsistenciesProgressEvent
import io.github.manamiproject.manami.app.inconsistencies.animelist.deadentries.AnimeListDeadEntriesInconsistenciesResultEvent
import io.github.manamiproject.manami.app.inconsistencies.animelist.metadata.AnimeListMetaDataInconsistenciesResultEvent
import io.github.manamiproject.manami.app.inconsistencies.lists.deadentries.DeadEntriesInconsistenciesResultEvent
import io.github.manamiproject.manami.app.inconsistencies.lists.metadata.MetaDataInconsistenciesResultEvent
import io.github.manamiproject.manami.app.lists.ListChangedEvent
import io.github.manamiproject.manami.app.lists.ListChangedEvent.EventType.ADDED
import io.github.manamiproject.manami.app.lists.ListChangedEvent.EventType.REMOVED
import io.github.manamiproject.manami.app.lists.ignorelist.AddIgnoreListStatusUpdateEvent
import io.github.manamiproject.manami.app.lists.watchlist.AddWatchListStatusUpdateEvent
import io.github.manamiproject.manami.app.relatedanime.RelatedAnimeFinishedEvent
import io.github.manamiproject.manami.app.relatedanime.RelatedAnimeFoundEvent
import io.github.manamiproject.manami.app.relatedanime.RelatedAnimeStatusEvent
import io.github.manamiproject.manami.app.search.FileSearchAnimeListResultsEvent
import io.github.manamiproject.manami.app.search.FileSearchIgnoreListResultsEvent
import io.github.manamiproject.manami.app.search.FileSearchWatchListResultsEvent
import io.github.manamiproject.manami.app.search.anime.AnimeEntryFinishedEvent
import io.github.manamiproject.manami.app.search.anime.AnimeEntryFoundEvent
import io.github.manamiproject.manami.app.search.anime.AnimeSearchEntryFoundEvent
import io.github.manamiproject.manami.app.search.anime.AnimeSearchFinishedEvent
import io.github.manamiproject.manami.app.search.season.AnimeSeasonEntryFoundEvent
import io.github.manamiproject.manami.app.search.season.AnimeSeasonSearchFinishedEvent
import io.github.manamiproject.manami.app.commands.history.FileSavedStatusChangedEvent
import io.github.manamiproject.manami.app.commands.history.UndoRedoStatusEvent
import io.github.manamiproject.manami.app.events.EventListType.*
import io.github.manamiproject.manami.app.inconsistencies.animelist.episodes.AnimeListEpisodesInconsistenciesResultEvent
import io.github.manamiproject.manami.app.versioning.NewVersionAvailableEvent
import io.github.manamiproject.manami.gui.events.*
import tornadofx.Controller

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
                    is AnimeSearchEntryFoundEvent -> AnimeSearchEntryFoundGuiEvent(this.anime)
                    is AnimeSearchFinishedEvent -> AnimeSearchFinishedGuiEvent
                    is AnimeEntryFoundEvent -> AnimeEntryFoundGuiEvent(this.anime)
                    is AnimeEntryFinishedEvent -> AnimeEntryFinishedGuiEvent
                    is NumberOfEntriesPerMetaDataProviderEvent -> NumberOfEntriesPerMetaDataProviderGuiEvent(this.entries)
                    is InconsistenciesProgressEvent -> InconsistenciesProgressGuiEvent(this.finishedTasks, this.numberOfTasks)
                    is InconsistenciesCheckFinishedEvent -> InconsistenciesCheckFinishedGuiEvent
                    is MetaDataInconsistenciesResultEvent -> MetaDataInconsistenciesResultGuiEvent(this.numberOfAffectedEntries)
                    is DeadEntriesInconsistenciesResultEvent -> DeadEntriesInconsistenciesResultGuiEvent(this.numberOfAffectedEntries)
                    is AnimeListMetaDataInconsistenciesResultEvent -> AnimeListMetaDataInconsistenciesResultGuiEvent(this.diff)
                    is AnimeListDeadEntriesInconsistenciesResultEvent -> AnimeListDeadEntriesInconsistenciesResultGuiEvent(this.entries)
                    is AnimeListEpisodesInconsistenciesResultEvent -> AnimeListEpisodesInconsistenciesResultGuiEvent(this.entries)
                    is NewVersionAvailableEvent -> NewVersionAvailableGuiEvent(this.version)
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
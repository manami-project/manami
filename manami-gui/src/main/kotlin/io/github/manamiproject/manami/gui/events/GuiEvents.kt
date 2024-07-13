package io.github.manamiproject.manami.gui.events

import io.github.manamiproject.manami.app.inconsistencies.animelist.episodes.EpisodeDiff
import io.github.manamiproject.manami.app.inconsistencies.animelist.metadata.AnimeListMetaDataDiff
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.versioning.SemanticVersion
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.models.Anime
import tornadofx.FXEvent

sealed class GuiEvent : FXEvent()

data class FileOpenedGuiEvent(val fileName: String): GuiEvent()
data class SavedAsFileGuiEvent(val fileName: String): GuiEvent()
data class FileSavedStatusChangedGuiEvent(val isFileSaved: Boolean): GuiEvent()
data object CachePopulatorFinishedGuiEvent: GuiEvent()
data class UndoRedoStatusGuiEvent(val isUndoPossible: Boolean, val isRedoPossible: Boolean): GuiEvent()
data class NewVersionAvailableGuiEvent(val version: SemanticVersion): GuiEvent()

// Dashboard
data class NumberOfEntriesPerMetaDataProviderGuiEvent(val entries: Map<Hostname, Int>): GuiEvent()

// AnimeList
data class AddAnimeListEntryGuiEvent(val entries: Set<AnimeListEntry>) : GuiEvent()
data class RemoveAnimeListEntryGuiEvent(val entries: Set<AnimeListEntry>) : GuiEvent()

// WatchList
data class AddWatchListEntryGuiEvent(val entries: Set<WatchListEntry>) : GuiEvent()
data class RemoveWatchListEntryGuiEvent(val entries: Set<WatchListEntry>) : GuiEvent()
data class AddWatchListStatusUpdateGuiEvent(val finishedTasks: Int, val tasks: Int): GuiEvent()
data object AnimeEntryFinishedGuiEvent: GuiEvent()

// IgnoreList
data class AddIgnoreListEntryGuiEvent(val entries: Set<IgnoreListEntry>) : GuiEvent()
data class RemoveIgnoreListEntryGuiEvent(val entries: Set<IgnoreListEntry>) : GuiEvent()
data class AddIgnoreListStatusUpdateGuiEvent(val finishedTasks: Int, val tasks: Int): GuiEvent()

// Related Anime
data class AnimeListRelatedAnimeFoundGuiEvent(val anime: Anime): GuiEvent()
data class AnimeListRelatedAnimeStatusGuiEvent(val finishedChecking: Int, val toBeChecked: Int): GuiEvent()
data object AnimeListRelatedAnimeFinishedGuiEvent: GuiEvent()

// IgnoreList Search
data class IgnoreListRelatedAnimeFoundGuiEvent(val anime: Anime): GuiEvent()
data class IgnoreListRelatedAnimeStatusGuiEvent(val finishedChecking: Int, val toBeChecked: Int): GuiEvent()
data object IgnoreListRelatedAnimeFinishedGuiEvent: GuiEvent()

// Anime Season
data class AnimeSeasonEntryFoundGuiEvent(val anime: Anime): GuiEvent()
data object AnimeSeasonSearchFinishedGuiEvent: GuiEvent()

// File Search
data class FileSearchAnimeListResultsGuiEvent(val anime: Collection<AnimeListEntry>): GuiEvent()
data class FileSearchWatchListResultsGuiEvent(val anime: Collection<WatchListEntry>): GuiEvent()
data class FileSearchIgnoreListResultsGuiEvent(val anime: Collection<IgnoreListEntry>): GuiEvent()

// Anime Search
data class AnimeSearchEntryFoundGuiEvent(val anime: Anime): GuiEvent()
data object AnimeSearchFinishedGuiEvent: GuiEvent()
data class AnimeEntryFoundGuiEvent(val anime: Anime): GuiEvent()

// Similar Anime Search
data object SimilarAnimeSearchFinishedGuiEvent: GuiEvent()
data class SimilarAnimeFoundGuiEvent(val entries: List<Anime>) : GuiEvent()

// Inconsistencies
data class InconsistenciesProgressGuiEvent(val finishedTasks: Int, val numberOfTasks: Int): GuiEvent()
data class MetaDataInconsistenciesResultGuiEvent(val numberOfAffectedEntries: Int): GuiEvent()
data class DeadEntriesInconsistenciesResultGuiEvent(val numberOfAffectedEntries: Int): GuiEvent()
data class AnimeListMetaDataInconsistenciesResultGuiEvent(val diff: AnimeListMetaDataDiff): GuiEvent()
data class AnimeListDeadEntriesInconsistenciesResultGuiEvent(val entries: Collection<AnimeListEntry>): GuiEvent()
data class AnimeListEpisodesInconsistenciesResultGuiEvent(val entries: Collection<EpisodeDiff>): GuiEvent()
data object InconsistenciesCheckFinishedGuiEvent: GuiEvent()

// Meta Data Provider Migration
data class MetaDataProviderMigrationGuiEvent(val finishedTasks: Int, val tasks: Int): GuiEvent()
data class MetaDataMigrationResultGuiEvent(
    val animeListEntriesWithoutMapping: Collection<AnimeListEntry>,
    val animeListEntiresMultipleMappings: Map<AnimeListEntry, Set<Link>>,
    val animeListMappings: Map<AnimeListEntry, Link>,
    val watchListEntriesWithoutMapping: Collection<WatchListEntry>,
    val watchListEntiresMultipleMappings: Map<WatchListEntry, Set<Link>>,
    val watchListMappings: Map<WatchListEntry, Link>,
    val ignoreListEntriesWithoutMapping: Collection<IgnoreListEntry>,
    val ignoreListEntiresMultipleMappings: Map<IgnoreListEntry, Set<Link>>,
    val ignoreListMappings: Map<IgnoreListEntry, Link>,
) : GuiEvent()
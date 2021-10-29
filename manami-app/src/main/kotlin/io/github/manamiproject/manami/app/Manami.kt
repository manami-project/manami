package io.github.manamiproject.manami.app

import io.github.manamiproject.manami.app.cache.Caches
import io.github.manamiproject.manami.app.cache.populator.AnimeCachePopulator
import io.github.manamiproject.manami.app.cache.populator.CachePopulatorFinishedEvent
import io.github.manamiproject.manami.app.cache.populator.DeadEntriesCachePopulator
import io.github.manamiproject.manami.app.cache.populator.NumberOfEntriesPerMetaDataProviderEvent
import io.github.manamiproject.manami.app.file.DefaultFileHandler
import io.github.manamiproject.manami.app.file.FileHandler
import io.github.manamiproject.manami.app.file.FileOpenedEvent
import io.github.manamiproject.manami.app.file.SavedAsFileEvent
import io.github.manamiproject.manami.app.inconsistencies.DefaultInconsistenciesHandler
import io.github.manamiproject.manami.app.inconsistencies.InconsistenciesCheckFinishedEvent
import io.github.manamiproject.manami.app.inconsistencies.InconsistenciesHandler
import io.github.manamiproject.manami.app.inconsistencies.InconsistenciesProgressEvent
import io.github.manamiproject.manami.app.inconsistencies.animelist.deadentries.AnimeListDeadEntriesInconsistenciesResultEvent
import io.github.manamiproject.manami.app.inconsistencies.animelist.metadata.AnimeListMetaDataInconsistenciesResultEvent
import io.github.manamiproject.manami.app.inconsistencies.lists.deadentries.DeadEntriesInconsistenciesResultEvent
import io.github.manamiproject.manami.app.inconsistencies.lists.metadata.MetaDataInconsistenciesResultEvent
import io.github.manamiproject.manami.app.lists.DefaultListHandler
import io.github.manamiproject.manami.app.lists.ListChangedEvent
import io.github.manamiproject.manami.app.lists.ListHandler
import io.github.manamiproject.manami.app.lists.ignorelist.AddIgnoreListStatusUpdateEvent
import io.github.manamiproject.manami.app.lists.watchlist.AddWatchListStatusUpdateEvent
import io.github.manamiproject.manami.app.relatedanime.*
import io.github.manamiproject.manami.app.search.*
import io.github.manamiproject.manami.app.search.anime.AnimeEntryFinishedEvent
import io.github.manamiproject.manami.app.search.anime.AnimeEntryFoundEvent
import io.github.manamiproject.manami.app.search.anime.AnimeSearchEntryFoundEvent
import io.github.manamiproject.manami.app.search.anime.AnimeSearchFinishedEvent
import io.github.manamiproject.manami.app.search.season.AnimeSeasonEntryFoundEvent
import io.github.manamiproject.manami.app.search.season.AnimeSeasonSearchFinishedEvent
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.commands.history.FileSavedStatusChangedEvent
import io.github.manamiproject.manami.app.state.commands.history.UndoRedoStatusEvent
import io.github.manamiproject.manami.app.state.events.Event
import io.github.manamiproject.manami.app.state.events.SimpleEventBus
import io.github.manamiproject.manami.app.state.events.Subscribe
import io.github.manamiproject.manami.app.versioning.DefaultLatestVersionChecker
import io.github.manamiproject.manami.app.versioning.NewVersionAvailableEvent
import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.anilist.AnilistConfig
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.kitsu.KitsuConfig
import io.github.manamiproject.modb.livechart.LivechartConfig
import io.github.manamiproject.modb.mal.MalConfig
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.exitProcess

class Manami(
    private val fileHandler: FileHandler = DefaultFileHandler(),
    private val searchHandler: SearchHandler = DefaultSearchHandler(),
    private val listHandler: ListHandler = DefaultListHandler(),
    private val relatedAnimeHandler: RelatedAnimeHandler = DefaultRelatedAnimeHandler(),
    private val inconsistenciesHandler: InconsistenciesHandler = DefaultInconsistenciesHandler(),
) : ManamiApp,
    SearchHandler by searchHandler,
    FileHandler by fileHandler,
    ListHandler by listHandler,
    RelatedAnimeHandler by relatedAnimeHandler,
    InconsistenciesHandler by inconsistenciesHandler {

    private var eventMapper = AtomicReference<Event.() -> Unit>()

    init {
        log.info {"Starting manami" }
        SimpleEventBus.subscribe(this)
        runInBackground {
            DefaultLatestVersionChecker().checkLatestVersion()
            AnimeCachePopulator().populate(Caches.animeCache)
            DeadEntriesCachePopulator(config = AnidbConfig, url = URL("$DEAD_ENTRIES_BASE_URL/anidb.json")).populate(Caches.animeCache)
            DeadEntriesCachePopulator(config = AnilistConfig, url = URL("$DEAD_ENTRIES_BASE_URL/anilist.json")).populate(Caches.animeCache)
            DeadEntriesCachePopulator(config = KitsuConfig, url = URL("$DEAD_ENTRIES_BASE_URL/kitsu.json")).populate(Caches.animeCache)
            DeadEntriesCachePopulator(config = LivechartConfig, url = URL("$DEAD_ENTRIES_BASE_URL/livechart.json")).populate(Caches.animeCache)
            DeadEntriesCachePopulator(config = MalConfig, url = URL("$DEAD_ENTRIES_BASE_URL/myanimelist.json")).populate(Caches.animeCache)
        }
    }

    override fun quit(ignoreUnsavedChanged: Boolean) {
        if (!ignoreUnsavedChanged) {
            check(isSaved()) { "Cannot quit app, because there are unsaved changes." }
        }

        log.info { "Terminating manami" }

        backgroundTasks.shutdown()
        exitProcess(0)
    }

    fun eventMapping(mapper: Event.() -> Unit = {}) {
        eventMapper.set(mapper)
    }

    @Subscribe (
        FileOpenedEvent::class,
        SavedAsFileEvent::class,
        ListChangedEvent::class,
        AddWatchListStatusUpdateEvent::class,
        AddIgnoreListStatusUpdateEvent::class,
        FileSavedStatusChangedEvent::class,
        UndoRedoStatusEvent::class,
        RelatedAnimeFoundEvent::class,
        RelatedAnimeStatusEvent::class,
        RelatedAnimeFinishedEvent::class,
        AnimeSeasonEntryFoundEvent::class,
        AnimeSeasonSearchFinishedEvent::class,
        CachePopulatorFinishedEvent::class,
        FileSearchAnimeListResultsEvent::class,
        FileSearchWatchListResultsEvent::class,
        FileSearchIgnoreListResultsEvent::class,
        AnimeSearchEntryFoundEvent::class,
        AnimeSearchFinishedEvent::class,
        NumberOfEntriesPerMetaDataProviderEvent::class,
        AnimeEntryFoundEvent::class,
        AnimeEntryFinishedEvent::class,
        InconsistenciesProgressEvent::class,
        InconsistenciesCheckFinishedEvent::class,
        MetaDataInconsistenciesResultEvent::class,
        DeadEntriesInconsistenciesResultEvent::class,
        AnimeListMetaDataInconsistenciesResultEvent::class,
        AnimeListDeadEntriesInconsistenciesResultEvent::class,
        NewVersionAvailableEvent::class,
    )
    fun subscribe(e: Event) = eventMapper.get().invoke(e)

    companion object {
        private val log by LoggerDelegate()
        private const val DEAD_ENTRIES_BASE_URL = "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/dead-entries"
    }
}

private val backgroundTasks = Executors.newCachedThreadPool()

internal fun runInBackground(action: () -> Unit) {
    backgroundTasks.submit {
        action.invoke()
    }
}
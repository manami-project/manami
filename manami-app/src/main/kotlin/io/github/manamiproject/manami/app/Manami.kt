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
import io.github.manamiproject.manami.app.import.DefaultImportHandler
import io.github.manamiproject.manami.app.import.ImportFinishedEvent
import io.github.manamiproject.manami.app.import.ImportHandler
import io.github.manamiproject.manami.app.inconsistencies.DefaultInconsistenciesHandler
import io.github.manamiproject.manami.app.inconsistencies.InconsistenciesCheckFinishedEvent
import io.github.manamiproject.manami.app.inconsistencies.InconsistenciesHandler
import io.github.manamiproject.manami.app.inconsistencies.InconsistenciesProgressEvent
import io.github.manamiproject.manami.app.inconsistencies.deadentries.DeadEntriesInconsistenciesResultEvent
import io.github.manamiproject.manami.app.inconsistencies.metadata.MetaDataInconsistenciesResultEvent
import io.github.manamiproject.manami.app.lists.DefaultListHandler
import io.github.manamiproject.manami.app.lists.ListChangedEvent
import io.github.manamiproject.manami.app.lists.ListHandler
import io.github.manamiproject.manami.app.lists.ignorelist.AddIgnoreListStatusUpdateEvent
import io.github.manamiproject.manami.app.lists.watchlist.AddWatchListStatusUpdateEvent
import io.github.manamiproject.manami.app.relatedanime.*
import io.github.manamiproject.manami.app.relatedanime.DefaultRelatedAnimeHandler
import io.github.manamiproject.manami.app.search.*
import io.github.manamiproject.manami.app.search.DefaultSearchHandler
import io.github.manamiproject.manami.app.search.anime.AnimeEntryFinishedEvent
import io.github.manamiproject.manami.app.search.anime.AnimeEntryFoundEvent
import io.github.manamiproject.manami.app.search.anime.AnimeSearchEntryFoundEvent
import io.github.manamiproject.manami.app.search.anime.AnimeSearchFinishedEvent
import io.github.manamiproject.manami.app.search.season.AnimeSeasonEntryFoundEvent
import io.github.manamiproject.manami.app.search.season.AnimeSeasonSearchFinishedEvent
import io.github.manamiproject.manami.app.state.commands.history.FileSavedStatusChangedEvent
import io.github.manamiproject.manami.app.state.commands.history.UndoRedoStatusEvent
import io.github.manamiproject.manami.app.state.events.Event
import io.github.manamiproject.manami.app.state.events.SimpleEventBus
import io.github.manamiproject.manami.app.state.events.Subscribe
import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.anilist.AnilistConfig
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.kitsu.KitsuConfig
import io.github.manamiproject.modb.mal.MalConfig
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.exitProcess

class Manami(
    private val fileHandler: FileHandler = DefaultFileHandler(),
    private val searchHandler: SearchHandler = DefaultSearchHandler(),
    private val importHandler: ImportHandler = DefaultImportHandler(),
    private val listHandler: ListHandler = DefaultListHandler(),
    private val relatedAnimeHandler: RelatedAnimeHandler = DefaultRelatedAnimeHandler(),
    private val inconsistenciesHandler: InconsistenciesHandler = DefaultInconsistenciesHandler(),
) : ManamiApp,
    SearchHandler by searchHandler,
    FileHandler by fileHandler,
    ImportHandler by importHandler,
    ListHandler by listHandler,
    RelatedAnimeHandler by relatedAnimeHandler,
    InconsistenciesHandler by inconsistenciesHandler {

    private var eventMapper = AtomicReference<Event.() -> Unit>()

    init {
        log.info("Starting manami")
        SimpleEventBus.subscribe(this)
        runInBackground {
            AnimeCachePopulator().populate(Caches.animeCache)
            DeadEntriesCachePopulator(config = MalConfig, url = URL("$CACHE_URL/myanimelist.json")).populate(Caches.animeCache)
            DeadEntriesCachePopulator(config = AnidbConfig, url = URL("$CACHE_URL/anidb.json")).populate(Caches.animeCache)
            DeadEntriesCachePopulator(config = AnilistConfig, url = URL("$CACHE_URL/anilist.json")).populate(Caches.animeCache)
            DeadEntriesCachePopulator(config = KitsuConfig, url = URL("$CACHE_URL/kitsu.json")).populate(Caches.animeCache)
        }
    }

    override fun quit(ignoreUnsavedChanged: Boolean) {
        if (!ignoreUnsavedChanged) {
            check(isSaved()) { "Cannot quit app, because there are unsaved changes." }
        }

        log.info("Terminating manami")

        backgroundTasks.shutdown()
        exitProcess(0)
    }

    fun eventMapping(mapper: Event.() -> Unit = {}) {
        eventMapper.set(mapper)
    }

    @Subscribe
    fun subscribe(e: FileOpenedEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: SavedAsFileEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: ListChangedEvent<*>) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: AddWatchListStatusUpdateEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: AddIgnoreListStatusUpdateEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: FileSavedStatusChangedEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: UndoRedoStatusEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: ImportFinishedEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: RelatedAnimeFoundEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: RelatedAnimeStatusEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: RelatedAnimeFinishedEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: AnimeSeasonEntryFoundEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: AnimeSeasonSearchFinishedEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: CachePopulatorFinishedEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: FileSearchAnimeListResultsEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: FileSearchWatchListResultsEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: FileSearchIgnoreListResultsEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: AnimeSearchEntryFoundEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: AnimeSearchFinishedEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: NumberOfEntriesPerMetaDataProviderEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: AnimeEntryFoundEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: AnimeEntryFinishedEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: InconsistenciesProgressEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: InconsistenciesCheckFinishedEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: MetaDataInconsistenciesResultEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: DeadEntriesInconsistenciesResultEvent) = eventMapper.get().invoke(e)

    companion object {
        private val log by LoggerDelegate()
        private const val CACHE_URL = "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/dead-entries"
    }
}

private val backgroundTasks = Executors.newCachedThreadPool()

internal fun runInBackground(action: () -> Unit) {
    backgroundTasks.submit {
        action.invoke()
    }
}
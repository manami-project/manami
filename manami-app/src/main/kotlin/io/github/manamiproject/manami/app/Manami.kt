package io.github.manamiproject.manami.app

import io.github.manamiproject.manami.app.cache.Caches
import io.github.manamiproject.manami.app.cache.populator.AnimeCachePopulator
import io.github.manamiproject.manami.app.cache.populator.CachePopulatorFinishedEvent
import io.github.manamiproject.manami.app.cache.populator.DeadEntriesCachePopulator
import io.github.manamiproject.manami.app.file.DefaultFileHandler
import io.github.manamiproject.manami.app.file.FileHandler
import io.github.manamiproject.manami.app.file.FileOpenedEvent
import io.github.manamiproject.manami.app.file.SavedAsFileEvent
import io.github.manamiproject.manami.app.import.DefaultImportHandler
import io.github.manamiproject.manami.app.import.ImportFinishedEvent
import io.github.manamiproject.manami.app.import.ImportHandler
import io.github.manamiproject.manami.app.lists.DefaultListHandler
import io.github.manamiproject.manami.app.lists.ListChangedEvent
import io.github.manamiproject.manami.app.lists.ListHandler
import io.github.manamiproject.manami.app.lists.ignorelist.AddIgnoreListStatusUpdateEvent
import io.github.manamiproject.manami.app.lists.watchlist.AddWatchListStatusUpdateEvent
import io.github.manamiproject.manami.app.relatedanime.DefaultRelatedAnimeHandler
import io.github.manamiproject.manami.app.relatedanime.RelatedAnimeFoundEvent
import io.github.manamiproject.manami.app.relatedanime.RelatedAnimeHandler
import io.github.manamiproject.manami.app.relatedanime.RelatedAnimeStatusEvent
import io.github.manamiproject.manami.app.search.AnimeSeasonEntryFoundEvent
import io.github.manamiproject.manami.app.search.AnimeSeasonSearchFinishedEvent
import io.github.manamiproject.manami.app.search.DefaultSearchHandler
import io.github.manamiproject.manami.app.search.SearchHandler
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
) : ManamiApp,
    SearchHandler by searchHandler,
    FileHandler by fileHandler,
    ImportHandler by importHandler,
    ListHandler by listHandler,
    RelatedAnimeHandler by relatedAnimeHandler {

    init {
        log.info("Starting manami")
        SimpleEventBus.subscribe(this)
        runInBackground {
            AnimeCachePopulator().populate(Caches.animeCache)
            DeadEntriesCachePopulator(config = MalConfig, url = URL("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/dead-entries/myanimelist.json")).populate(Caches.animeCache)
            DeadEntriesCachePopulator(config = AnidbConfig, url = URL("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/dead-entries/anidb.json")).populate(Caches.animeCache)
            DeadEntriesCachePopulator(config = AnilistConfig, url = URL("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/dead-entries/anilist.json")).populate(Caches.animeCache)
            DeadEntriesCachePopulator(config = KitsuConfig, url = URL("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/dead-entries/kitsu.json")).populate(Caches.animeCache)
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

    private var eventMapper = AtomicReference<Event.() -> Unit>()
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
    fun subscribe(e: AnimeSeasonEntryFoundEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: AnimeSeasonSearchFinishedEvent) = eventMapper.get().invoke(e)

    @Subscribe
    fun subscribe(e: CachePopulatorFinishedEvent) = eventMapper.get().invoke(e)

    companion object {
        private val log by LoggerDelegate()
    }
}

private val backgroundTasks = Executors.newCachedThreadPool()

internal fun runInBackground(action: () -> Unit) {
    backgroundTasks.submit {
        action.invoke()
    }
}
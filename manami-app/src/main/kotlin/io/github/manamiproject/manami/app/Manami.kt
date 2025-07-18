package io.github.manamiproject.manami.app

import io.github.manamiproject.AnimenewsnetworkConfig
import io.github.manamiproject.manami.app.cache.DefaultAnimeCache
import io.github.manamiproject.manami.app.cache.populator.AnimeCachePopulator
import io.github.manamiproject.manami.app.cache.populator.DeadEntriesCachePopulator
import io.github.manamiproject.manami.app.events.Event
import io.github.manamiproject.manami.app.events.SimpleEventBus
import io.github.manamiproject.manami.app.events.Subscribe
import io.github.manamiproject.manami.app.file.DefaultFileHandler
import io.github.manamiproject.manami.app.file.FileHandler
import io.github.manamiproject.manami.app.inconsistencies.DefaultInconsistenciesHandler
import io.github.manamiproject.manami.app.inconsistencies.InconsistenciesHandler
import io.github.manamiproject.manami.app.lists.DefaultListHandler
import io.github.manamiproject.manami.app.lists.ListHandler
import io.github.manamiproject.manami.app.migration.DefaultMetaDataMigrationHandler
import io.github.manamiproject.manami.app.migration.MetaDataMigrationHandler
import io.github.manamiproject.manami.app.relatedanime.DefaultRelatedAnimeHandler
import io.github.manamiproject.manami.app.relatedanime.RelatedAnimeHandler
import io.github.manamiproject.manami.app.search.DefaultSearchHandler
import io.github.manamiproject.manami.app.search.SearchHandler
import io.github.manamiproject.manami.app.versioning.DefaultLatestVersionChecker
import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.anilist.AnilistConfig
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.kitsu.KitsuConfig
import io.github.manamiproject.modb.myanimelist.MyanimelistConfig
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.URI
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.exitProcess

class Manami(
    private val fileHandler: FileHandler = DefaultFileHandler(),
    private val searchHandler: SearchHandler = DefaultSearchHandler(),
    private val listHandler: ListHandler = DefaultListHandler(),
    private val relatedAnimeHandler: RelatedAnimeHandler = DefaultRelatedAnimeHandler(),
    private val inconsistenciesHandler: InconsistenciesHandler = DefaultInconsistenciesHandler(),
    private val metaDataMigrationHandler: MetaDataMigrationHandler = DefaultMetaDataMigrationHandler(),
) : ManamiApp,
    SearchHandler by searchHandler,
    FileHandler by fileHandler,
    ListHandler by listHandler,
    RelatedAnimeHandler by relatedAnimeHandler,
    InconsistenciesHandler by inconsistenciesHandler,
    MetaDataMigrationHandler by metaDataMigrationHandler {

    private var eventMapper = AtomicReference<Event.() -> Unit>()

    init {
        log.info {"Starting manami" }
        SimpleEventBus.subscribe(this)
        runInBackground {
            withContext(ModbDispatchers.LIMITED_CPU) {
                launch { DefaultLatestVersionChecker().checkLatestVersion() }
                launch { AnimeCachePopulator().populate(DefaultAnimeCache.instance) }
                launch { DeadEntriesCachePopulator(config = AnidbConfig, url = URI("$DEAD_ENTRIES_BASE_URL/anidb-minified.json.zst").toURL()).populate(DefaultAnimeCache.instance) }
                launch { DeadEntriesCachePopulator(config = AnilistConfig, url = URI("$DEAD_ENTRIES_BASE_URL/anilist-minified.json.zst").toURL()).populate(DefaultAnimeCache.instance) }
                launch { DeadEntriesCachePopulator(config = AnimenewsnetworkConfig, url = URI("$DEAD_ENTRIES_BASE_URL/animenewsnetwork-minified.json.zst").toURL()).populate(DefaultAnimeCache.instance) }
                launch { DeadEntriesCachePopulator(config = KitsuConfig, url = URI("$DEAD_ENTRIES_BASE_URL/kitsu-minified.json.zst").toURL()).populate(DefaultAnimeCache.instance) }
                launch { DeadEntriesCachePopulator(config = MyanimelistConfig, url = URI("$DEAD_ENTRIES_BASE_URL/myanimelist-minified.json.zst").toURL()).populate(DefaultAnimeCache.instance) }
            }
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

    @Subscribe
    fun subscribe(e: Event) = eventMapper.get().invoke(e)

    companion object {
        private val log by LoggerDelegate()
        private const val DEAD_ENTRIES_BASE_URL = "https://github.com/manami-project/anime-offline-database/releases/download/latest"
    }
}

private val backgroundTasks = Executors.newCachedThreadPool()

internal fun runInBackground(action: suspend () -> Unit) {
    backgroundTasks.submit {
        runBlocking {
            action.invoke()
        }
    }
}
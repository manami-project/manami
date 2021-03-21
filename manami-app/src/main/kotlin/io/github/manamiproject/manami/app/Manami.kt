package io.github.manamiproject.manami.app

import io.github.manamiproject.manami.app.cache.Caches
import io.github.manamiproject.manami.app.cache.populator.AnimeCachePopulator
import io.github.manamiproject.manami.app.file.DefaultFileHandler
import io.github.manamiproject.manami.app.file.FileHandler
import io.github.manamiproject.manami.app.import.DefaultImportHandler
import io.github.manamiproject.manami.app.import.ImportFinishedEvent
import io.github.manamiproject.manami.app.import.ImportHandler
import io.github.manamiproject.manami.app.lists.DefaultListHandler
import io.github.manamiproject.manami.app.lists.ListHandler
import io.github.manamiproject.manami.app.lists.ignorelist.AddIgnoreListStatusUpdateEvent
import io.github.manamiproject.manami.app.lists.watchlist.AddWatchListStatusUpdateEvent
import io.github.manamiproject.manami.app.search.DefaultSearchHandler
import io.github.manamiproject.manami.app.search.SearchHandler
import io.github.manamiproject.manami.app.state.commands.history.FileSavedStatusChangedEvent
import io.github.manamiproject.manami.app.state.commands.history.UndoRedoStatusEvent
import io.github.manamiproject.manami.app.state.events.Event
import io.github.manamiproject.manami.app.state.events.ListChangedEvent
import io.github.manamiproject.manami.app.state.events.SimpleEventBus
import io.github.manamiproject.manami.app.state.events.Subscribe
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import java.util.concurrent.Executors
import kotlin.system.exitProcess

class Manami(
    private val fileHandler: FileHandler = DefaultFileHandler(),
    private val searchHandler: SearchHandler = DefaultSearchHandler(),
    private val importHandler: ImportHandler = DefaultImportHandler(),
    private val listHandler: ListHandler = DefaultListHandler(),
) : ManamiApp,
    SearchHandler by searchHandler,
    FileHandler by fileHandler,
    ImportHandler by importHandler,
    ListHandler by listHandler {

    init {
        log.info("Starting manami")
        SimpleEventBus.subscribe(this)
        runInBackground {
            AnimeCachePopulator().populate(Caches.animeCache)
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

    private var eventMapper: Event.() -> Unit = {}

    fun eventMapping(mapper: Event.() -> Unit = {}) {
        eventMapper = mapper
    }

    @Subscribe
    fun subscribe(e: ListChangedEvent<*>) = eventMapper.invoke(e)

    @Subscribe
    fun subscribe(e: AddWatchListStatusUpdateEvent) = eventMapper.invoke(e)

    @Subscribe
    fun subscribe(e: AddIgnoreListStatusUpdateEvent) = eventMapper.invoke(e)

    @Subscribe
    fun subscribe(e: FileSavedStatusChangedEvent) = eventMapper.invoke(e)

    @Subscribe
    fun subscribe(e: UndoRedoStatusEvent) = eventMapper.invoke(e)

    @Subscribe
    fun subscribe(e: ImportFinishedEvent) = eventMapper.invoke(e)

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
package io.github.manamiproject.manami.app

import io.github.manamiproject.manami.app.fileexport.DefaultExportHandler
import io.github.manamiproject.manami.app.fileexport.ExportHandler
import io.github.manamiproject.manami.app.file.DefaultFileHandler
import io.github.manamiproject.manami.app.file.FileHandler
import io.github.manamiproject.manami.app.fileimport.DefaultImportHandler
import io.github.manamiproject.manami.app.fileimport.ImportHandler
import io.github.manamiproject.manami.app.search.DefaultSearchHandler
import io.github.manamiproject.manami.app.search.SearchHandler
import io.github.manamiproject.manami.app.state.events.AnimeListChangedEvent
import io.github.manamiproject.manami.app.state.events.Event
import io.github.manamiproject.manami.app.state.events.EventBus
import io.github.manamiproject.manami.app.state.events.Subscribe

class Manami(
        private val fileHandler: FileHandler = DefaultFileHandler(),
        private val searchHandler: SearchHandler = DefaultSearchHandler(),
        private val importHandler: ImportHandler = DefaultImportHandler(),
        private val exportHandler: ExportHandler = DefaultExportHandler(),
) : ManamiApp,
    SearchHandler by searchHandler,
    FileHandler by fileHandler,
    ImportHandler by importHandler,
    ExportHandler by exportHandler {

    init {
        EventBus.subscribe(this)
    }

    private var eventMapper: Event.() -> Unit = {}

    fun eventMapping(mapper: Event.() -> Unit = {}) {
        eventMapper = mapper
    }

    @Subscribe
    fun subscribe(e: AnimeListChangedEvent) = eventMapper.invoke(e)
}
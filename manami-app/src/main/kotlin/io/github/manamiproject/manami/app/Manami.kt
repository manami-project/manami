package io.github.manamiproject.manami.app

import io.github.manamiproject.manami.app.export.DefaultExportHandler
import io.github.manamiproject.manami.app.export.ExportHandler
import io.github.manamiproject.manami.app.file.DefaultFileHandler
import io.github.manamiproject.manami.app.file.FileHandler
import io.github.manamiproject.manami.app.import.DefaultImportHandler
import io.github.manamiproject.manami.app.import.ImportHandler
import io.github.manamiproject.manami.app.search.DefaultSearchHandler
import io.github.manamiproject.manami.app.search.SearchHandler
import io.github.manamiproject.manami.app.state.DefaultStateHandler
import io.github.manamiproject.manami.app.state.StateHandler

class Manami(
        private val fileHandler: FileHandler = DefaultFileHandler(),
        private val searchHandler: SearchHandler = DefaultSearchHandler(),
        private val importHandler: ImportHandler = DefaultImportHandler(),
        private val exportHandler: ExportHandler = DefaultExportHandler(),
        private val stateHandler: StateHandler = DefaultStateHandler()
) : ManamiApp,
    SearchHandler by searchHandler,
    FileHandler by fileHandler,
    ImportHandler by importHandler,
    ExportHandler by exportHandler,
    StateHandler by stateHandler
package io.github.manamiproject.manami.app

import io.github.manamiproject.manami.app.export.ExportHandler
import io.github.manamiproject.manami.app.file.FileHandler
import io.github.manamiproject.manami.app.import.ImportHandler
import io.github.manamiproject.manami.app.search.SearchHandler
import io.github.manamiproject.manami.app.state.StateHandler

interface ManamiApp: SearchHandler,
        FileHandler,
        ImportHandler,
        ExportHandler,
        StateHandler
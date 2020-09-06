package io.github.manamiproject.manami.app

import io.github.manamiproject.manami.app.export.ExportHandler
import io.github.manamiproject.manami.app.file.FileHandler
import io.github.manamiproject.manami.app.import.ImportHandler
import io.github.manamiproject.manami.app.search.SearchHandler

interface ManamiApp: SearchHandler,
        FileHandler,
        ImportHandler,
        ExportHandler
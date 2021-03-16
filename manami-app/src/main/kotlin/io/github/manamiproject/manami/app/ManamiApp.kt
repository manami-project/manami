package io.github.manamiproject.manami.app

import io.github.manamiproject.manami.app.file.FileHandler
import io.github.manamiproject.manami.app.import.ImportHandler
import io.github.manamiproject.manami.app.lists.ListHandler
import io.github.manamiproject.manami.app.search.SearchHandler

interface ManamiApp: SearchHandler, FileHandler, ImportHandler, ListHandler
package io.github.manamiproject.manami.app

import io.github.manamiproject.manami.app.file.FileHandler
import io.github.manamiproject.manami.app.inconsistencies.InconsistenciesHandler
import io.github.manamiproject.manami.app.lists.ListHandler
import io.github.manamiproject.manami.app.relatedanime.RelatedAnimeHandler
import io.github.manamiproject.manami.app.search.SearchHandler

interface ManamiApp: SearchHandler, FileHandler, ListHandler, RelatedAnimeHandler, InconsistenciesHandler {

    fun quit(ignoreUnsavedChanged: Boolean = false)
}
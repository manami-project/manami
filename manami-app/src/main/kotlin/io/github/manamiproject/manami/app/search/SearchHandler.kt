package io.github.manamiproject.manami.app.search

import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.models.AnimeSeason

interface SearchHandler {

    fun findInLists(searchString: String)

    fun findSeason(season: AnimeSeason, metaDataProvider: Hostname)

    fun availableMetaDataProviders(): Set<Hostname>
}
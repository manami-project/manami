package io.github.manamiproject.manami.app.search

import io.github.manamiproject.manami.app.search.SearchType.AND
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.AnimeSeason
import io.github.manamiproject.modb.core.models.Tag
import java.net.URI

interface SearchHandler {

    fun findInLists(searchString: String)

    fun findSeason(season: AnimeSeason, metaDataProvider: Hostname)

    fun findByTag(tags: Set<Tag>, metaDataProvider: Hostname, searchType: SearchType = AND, status: Set<Anime.Status> = Anime.Status.values().toSet())

    fun findSimilarAnime(uri: URI)

    fun find(uri: URI)

    fun availableMetaDataProviders(): Set<Hostname>

    fun availableTags(): Set<Tag>
}
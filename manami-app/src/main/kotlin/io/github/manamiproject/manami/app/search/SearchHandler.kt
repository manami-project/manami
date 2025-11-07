package io.github.manamiproject.manami.app.search

import io.github.manamiproject.manami.app.search.SearchType.AND
import io.github.manamiproject.modb.core.anime.AnimeSeason
import io.github.manamiproject.modb.core.anime.AnimeStatus
import io.github.manamiproject.modb.core.anime.Tag
import io.github.manamiproject.modb.core.config.Hostname
import java.net.URI

interface SearchHandler {

    suspend fun findInLists(searchString: String)

    suspend fun findSeason(season: AnimeSeason, metaDataProvider: Hostname)

    suspend fun findByTag(tags: Set<Tag>, metaDataProvider: Hostname, searchType: SearchType = AND, status: Set<AnimeStatus> = AnimeStatus.entries.toSet())

    suspend fun findSimilarAnime(uri: URI)

    suspend fun findAnimeDetails(uri: URI)

    fun availableMetaDataProviders(): Set<Hostname>

    fun availableTags(): Set<Tag>
}
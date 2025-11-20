package io.github.manamiproject.manami.app.search

import io.github.manamiproject.modb.core.anime.AnimeSeason
import io.github.manamiproject.modb.core.anime.Producer
import io.github.manamiproject.modb.core.anime.Studio
import io.github.manamiproject.modb.core.anime.Tag
import io.github.manamiproject.modb.core.config.Hostname
import java.net.URI

interface SearchHandler {

    suspend fun findByTitle(metaDataProvider: Hostname, searchString: String)

    suspend fun findSeason(season: AnimeSeason, metaDataProvider: Hostname)

    suspend fun findByCriteria(config: FindByCriteriaConfig)

    suspend fun findSimilarAnime(uri: URI)

    suspend fun findAnimeDetails(uri: URI)

    fun availableMetaDataProviders(): Set<Hostname>

    fun availableTags(): Set<Tag>

    fun availableStudios(): Set<Studio>

    fun availableProducers(): Set<Producer>
}
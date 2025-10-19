package io.github.manamiproject.manami.app.cache

import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.Tag
import io.github.manamiproject.modb.core.config.Hostname
import java.net.URI

interface AnimeCache : Cache<URI, CacheEntry<Anime>> {

    val availableMetaDataProvider: Set<Hostname>
    val availableTags: Set<Tag>

    fun allEntries(metaDataProvider: Hostname): Sequence<Anime>

    fun mapToMetaDataProvider(uri: URI, metaDataProvider: Hostname): Set<URI>
}
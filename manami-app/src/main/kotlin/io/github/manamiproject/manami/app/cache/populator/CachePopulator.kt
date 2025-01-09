package io.github.manamiproject.manami.app.cache.populator

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.CacheEntry

internal interface CachePopulator<KEY, VALUE: CacheEntry<*>> {
    suspend fun populate(cache: Cache<KEY, VALUE>)
}
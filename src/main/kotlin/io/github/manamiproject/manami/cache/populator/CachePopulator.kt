package io.github.manamiproject.manami.cache.populator

import io.github.manamiproject.manami.cache.Cache

internal interface CachePopulator<KEY, VALUE> {
    fun populate(cache: Cache<KEY, VALUE>)
}
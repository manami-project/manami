package io.github.manamiproject.manami.app.cache.populator

import io.github.manamiproject.manami.app.cache.Cache

internal interface CachePopulator<KEY, VALUE> {
    fun populate(cache: Cache<KEY, VALUE>)
}
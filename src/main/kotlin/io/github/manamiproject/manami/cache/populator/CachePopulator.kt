package io.github.manamiproject.manami.cache.populator

import io.github.manamiproject.manami.cache.Cache

interface CachePopulator<KEY, VALUE> {

    fun populate(cache: Cache<KEY, VALUE>)
}
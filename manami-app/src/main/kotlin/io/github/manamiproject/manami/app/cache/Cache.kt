package io.github.manamiproject.manami.app.cache

interface Cache<KEY, VALUE: CacheEntry<*>> {

    suspend fun fetch(key: KEY): VALUE

    fun populate(key: KEY, value: VALUE)

    fun clear()
}
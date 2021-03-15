package io.github.manamiproject.manami.app.cache

internal interface Cache<KEY, VALUE: CacheEntry<*>> {

    fun fetch(key: KEY): VALUE

    fun populate(key: KEY, value: VALUE)

    fun clear()
}

sealed class CacheEntry<T>
class PresentValue<T>(val value: T) : CacheEntry<T>()
class Empty<T>: CacheEntry<T>()
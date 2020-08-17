package io.github.manamiproject.manami.cache

internal interface Cache<KEY, VALUE> {

    fun fetch(key: KEY): VALUE?

    fun populate(key: KEY, value: VALUE)

    fun clear()
}
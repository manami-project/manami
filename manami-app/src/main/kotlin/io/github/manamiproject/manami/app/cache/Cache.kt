package io.github.manamiproject.manami.app.cache

internal interface Cache<KEY, VALUE> {

    fun fetch(key: KEY): VALUE?

    fun populate(key: KEY, value: VALUE)

    fun clear()
}


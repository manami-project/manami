package io.github.manamiproject.manami.app.cache

sealed class CacheEntry<T>
class DeadEntry<T>: CacheEntry<T>()
class PresentValue<T>(val value: T) : CacheEntry<T>()
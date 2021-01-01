package io.github.manamiproject.manami.app.extensions

inline fun <reified T: Any> Collection<*>.castToSet(): Set<T> {
    require(this.all { it!!::class == T::class }) { "Not all items are of type [${T::class}]" }
    return this.asSequence().map { it as T }.toSet()
}
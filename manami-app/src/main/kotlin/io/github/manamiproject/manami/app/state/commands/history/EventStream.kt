package io.github.manamiproject.manami.app.state.commands.history

internal interface EventStream<T> {

    fun add(element: T)

    fun element(): T

    fun hasPrevious(): Boolean

    fun previous(): Boolean

    fun hasNext(): Boolean

    fun next(): Boolean

    fun crop()

    fun clear()
}
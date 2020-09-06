package io.github.manamiproject.manami.app.commands.history

internal class SimpleEventStream<T> : EventStream<T> {

    private var cursorIndex: Int = 0
    private val elements: MutableList<Events> = mutableListOf(InitialState)

    override fun add(element: T) {
        val position = cursorIndex + 1
        elements.add(position, Event(element))
        cursorIndex++

        val positionOfLastElement = elements.size - 1

        if (cursorIndex != positionOfLastElement) {
            for (index in positionOfLastElement downTo cursorIndex + 1) {
                elements.removeAt(index)
            }
        }
    }

    override fun element(): T {
        return when(val element = elements[cursorIndex]) {
            InitialState -> throw IllegalStateException("Cannot retrieve element from empty EventStream.")
            is Event<*> -> element.element as T
        }
    }

    override fun hasPrevious(): Boolean = cursorIndex > 0

    override fun previous(): Boolean {
        return if (cursorIndex == 0) {
            false
        } else {
            cursorIndex--
            return true
        }
    }

    override fun hasNext(): Boolean = cursorIndex < elements.size - 1

    override fun next(): Boolean {
        return if (elements.isEmpty() || cursorIndex == elements.size - 1) {
            false
        } else {
            cursorIndex++
            return true
        }
    }

    override fun clear() {
        elements.clear()
        elements.add(InitialState)
        cursorIndex = 0
    }
}

private sealed class Events
private object InitialState : Events()
private data class Event<T>(val element: T) : Events()
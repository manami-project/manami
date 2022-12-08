package io.github.manamiproject.manami.app.events

import io.github.manamiproject.manami.app.lists.ListChangedEvent
import io.github.manamiproject.manami.app.lists.ListChangedEvent.EventType.ADDED
import io.github.manamiproject.manami.app.lists.ListChangedEvent.EventType.REMOVED
import java.util.function.Predicate

internal class EventfulList<T>(
    private val listType: EventListType,
    private val eventBus: EventBus = SimpleEventBus,
    private val list: MutableList<T> = mutableListOf(),
) : MutableList<T> by list {

    constructor(listType: EventListType, vararg values: T) : this(
        listType = listType,
        list = values.toMutableList(),
    )

    override fun add(element: T): Boolean {
        if (list.contains(element)) {
            return false
        }

        val hasBeenModified = list.add(element)

        if (hasBeenModified) {
            eventBus.post(
                ListChangedEvent(
                    list = listType,
                    type = ADDED,
                    obj = setOf(element),
                )
            )
        }

        return hasBeenModified
    }

    override fun add(index: Int, element: T) {
        require(index <= list.size - 1) { "Cannot add on unpopulated index." }

        if (list.contains(element)) {
            list.remove(element)
            list.add(index, element)
        } else {
            list.add(index, element)

            eventBus.post(
                ListChangedEvent(
                    list = listType,
                    type = ADDED,
                    obj = setOf(element),
                )
            )
        }
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        val isModifiable = elements.any { !list.contains(it) }

        list.removeAll(elements)
        list.addAll(index, elements)

        if (isModifiable) {
            eventBus.post(
                ListChangedEvent(
                    list = listType,
                    type = ADDED,
                    obj = elements.toSet(),
                )
            )
        }

        return isModifiable
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val elementsToAdd = elements.filterNot { list.contains(it) }
        val isModifiable = elementsToAdd.isNotEmpty()

        if (isModifiable) {
            list.addAll(elementsToAdd)

            eventBus.post(
                ListChangedEvent(
                    list = listType,
                    type = ADDED,
                    obj = elements.toSet(),
                )
            )
        }

        return isModifiable
    }

    override fun remove(element: T): Boolean {
        if (!list.contains(element)) {
            return false
        }

        val hasBeenModified = list.remove(element)

        if (hasBeenModified) {
            eventBus.post(
                ListChangedEvent(
                    list = listType,
                    type = REMOVED,
                    obj = setOf(element),
                )
            )
        }

        return hasBeenModified
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        val elementsToRemove = elements.filter { list.contains(it) }
        val isModifiable = elementsToRemove.isNotEmpty()

        if (isModifiable) {
            list.removeAll(elementsToRemove)

            eventBus.post(
                ListChangedEvent(
                    list = listType,
                    type = REMOVED,
                    obj = elementsToRemove.toSet(),
                )
            )
        }

        return isModifiable
    }

    override fun removeAt(index: Int): T {
        require(index < list.size - 1) { "Cannot remove unpopulated index." }

        val returnValue = list.removeAt(index)

        eventBus.post(
            ListChangedEvent(
                list = listType,
                type = REMOVED,
                obj = setOf(returnValue),
            )
        )

        return returnValue
    }

    override fun removeIf(filter: Predicate<in T>): Boolean {
        val elementsBeingRemoved = list.filter { filter.test(it)}
        val isModifiable = elementsBeingRemoved.isNotEmpty()

        if (isModifiable) {
            list.removeIf(filter)

            eventBus.post(
                ListChangedEvent(
                    list = listType,
                    type = REMOVED,
                    obj = elementsBeingRemoved.toSet(),
                )
            )
        }

        return isModifiable
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        val elementsToBeRemoved = list - elements
        val isModifiable = elementsToBeRemoved.isNotEmpty()

        if (isModifiable) {
            list.removeAll(elementsToBeRemoved)

            eventBus.post(
                ListChangedEvent(
                    list = listType,
                    type = REMOVED,
                    obj = elementsToBeRemoved.toSet(),
                )
            )
        }

        return isModifiable
    }

    override fun set(index: Int, element: T): T {
        require(index <= list.size - 1) { "Cannot replace entry on unpopulated index." }

        val replacedValue = list.set(index, element)

        if (replacedValue != element) {
            eventBus.post(
                ListChangedEvent(
                    list = listType,
                    type = REMOVED,
                    obj = setOf(replacedValue),
                )
            )
            eventBus.post(
                ListChangedEvent(
                    list = listType,
                    type = ADDED,
                    obj = setOf(element),
                )
            )
        }

        return replacedValue
    }

    override fun clear() {
        if (list.isEmpty()) {
            return
        }

        eventBus.post(
            ListChangedEvent(
                list = listType,
                type = REMOVED,
                obj = list.toSet(),
            )
        )

        list.clear()
    }

    override fun toString(): String = list.toString()

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is EventfulList<*>) return false
        if (other === this) return true

        return other.toList() == list.toList()
    }

    override fun hashCode(): Int = list.toList().hashCode()
}
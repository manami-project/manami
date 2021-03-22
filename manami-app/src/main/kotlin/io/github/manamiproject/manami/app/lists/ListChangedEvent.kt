package io.github.manamiproject.manami.app.lists

import io.github.manamiproject.manami.app.state.events.Event
import io.github.manamiproject.manami.app.state.events.EventListType

data class ListChangedEvent<T>(
    val list: EventListType,
    val type: EventType,
    val obj: Set<T>,
) : Event {

    enum class EventType {
        ADDED,
        REMOVED,
    }
}
package io.github.manamiproject.manami.app.state.events

interface Event

data class ListChangedEvent<T>(
    val list: ListType,
    val type: EventType,
    val obj: Set<T>,
) : Event {

    enum class ListType {
        ANIME_LIST,
        WATCH_LIST,
        IGNORE_LIST,
    }

    enum class EventType {
        ADDED,
        REMOVED,
    }
}
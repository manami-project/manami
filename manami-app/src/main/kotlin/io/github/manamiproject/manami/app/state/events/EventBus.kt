package io.github.manamiproject.manami.app.state.events

interface EventBus {

    fun subscribe(subscriber: Any)

    fun unsubscribe(subscriber: Any)

    fun post(event: Event)
}
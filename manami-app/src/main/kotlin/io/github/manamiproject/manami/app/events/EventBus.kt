package io.github.manamiproject.manami.app.events

interface EventBus {

    fun subscribe(subscriber: Any)

    fun unsubscribe(subscriber: Any)

    fun post(event: Event)
}
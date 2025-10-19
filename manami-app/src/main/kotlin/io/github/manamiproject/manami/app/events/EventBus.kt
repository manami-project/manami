package io.github.manamiproject.manami.app.events

/**
 * TODO 4.0.0: Remove
 */
@Deprecated("Remove")
interface EventBus {

    fun subscribe(subscriber: Any)

    fun unsubscribe(subscriber: Any)

    fun post(event: Event)
}
package io.github.manamiproject.manami.app.events

import io.github.manamiproject.modb.test.shouldNotBeInvoked

object TestEventBus: EventBus {
    override fun subscribe(subscriber: Any) = shouldNotBeInvoked()
    override fun unsubscribe(subscriber: Any) = shouldNotBeInvoked()
    override fun post(event: Event) = shouldNotBeInvoked()
}
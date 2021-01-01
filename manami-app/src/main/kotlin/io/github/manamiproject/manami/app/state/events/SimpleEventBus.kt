package io.github.manamiproject.manami.app.state.events

import java.util.concurrent.Executors
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter.Kind.VALUE

private typealias ClassName = String

object SimpleEventBus : EventBus {

    private val mapSubscribers = mutableMapOf<ClassName, MutableSet<Any>>()
    private val threadPool = Executors.newSingleThreadExecutor()

    override fun subscribe(subscriber: Any) {
        val functions = subscriber.javaClass.kotlin.members
                .filterIsInstance<KFunction<*>>()
                .filter { func -> func.annotations.any { it.annotationClass == Subscribe::class } }

        check(functions.isNotEmpty()) { "EventBus subscriber does not provide a function annotated with @Subscribe" }

        functions.filter { func -> func.parameters.count { it.kind == VALUE } == 1}.forEach { func ->
            func.parameters.find { it.kind == VALUE }?.apply {
                val eventClassName = (this.type.classifier as KClass<*>).toString()
                val currentSubscribers = mapSubscribers[eventClassName].let {
                    when(it) {
                        null -> mutableSetOf()
                        else -> it
                    }
                }

                currentSubscribers.add(subscriber)
                mapSubscribers[eventClassName] = currentSubscribers
            }
        }
    }

    override fun unsubscribe(subscriber: Any) {
        mapSubscribers.forEach { (_, value) ->
            value.removeIf { currentSubscriber -> currentSubscriber === subscriber }
        }
    }

    override fun post(event: Event) {
        val subscribers = mapSubscribers[event::class.toString()]
        val errorMessage = "No subscriber for given event [${event::class}]"

        checkNotNull(subscribers) { errorMessage }
        check(subscribers.isNotEmpty()) { errorMessage }

        subscribers.forEach { subscriber ->
            subscriber::class.members.filterIsInstance<KFunction<*>>()
                    .filter { func -> func.annotations.any { it.annotationClass == Subscribe::class } }
                    .filter { func -> func.parameters.find { it.kind == VALUE }?.type?.classifier == event::class }
                    .forEach {
                        threadPool.submit {
                            it.call(subscriber, event)
                        }
                    }
        }
    }
}
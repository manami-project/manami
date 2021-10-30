package io.github.manamiproject.manami.app.events

import java.util.concurrent.Executors
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KParameter.Kind.VALUE
import kotlin.reflect.full.isSubclassOf

private typealias ClassName = String

internal object SimpleEventBus : EventBus {

    private val mapSubscribers = mutableMapOf<ClassName, MutableSet<Any>>()
    private val threadPool = Executors.newSingleThreadExecutor()

    override fun subscribe(subscriber: Any) {
        val functions = findAnnotatedFunctions(subscriber)
            .filter { doesFunctionProvideExactlyOneParameter(it) }
            .filter { doesParameterImplementEvent(it.parameters) }

        check(functions.isNotEmpty()) { "Either the EventBus subscriber does not provide a function annotated with @Subscribe or the respective functions does not provide a single Parameter of a type which implements Event." }

        functions.forEach { func ->
                func.annotations.findSubscribeAnnotations().forEach { annotation ->
                    if (annotation.types.isNotEmpty()) {
                        annotation.types.forEach { type ->
                            upsertSubscriber(subscriber, type)
                        }
                    } else {
                        val updatedSubscribers = mapSubscribers[Event::class.toString()].let { subscriberEntry ->
                            when(subscriberEntry) {
                                null -> mutableSetOf()
                                else -> subscriberEntry
                            }
                        }
                        updatedSubscribers.add(subscriber)
                        mapSubscribers[Event::class.toString()] = updatedSubscribers
                    }
                }
        }
    }

    override fun unsubscribe(subscriber: Any) {
        mapSubscribers.forEach { (_, value) ->
            value.removeIf { currentSubscriber -> currentSubscriber === subscriber }
        }
    }

    override fun post(event: Event) {
        val typeSpecificsubscribers = mapSubscribers[event::class.toString()].takeIf { it?.isNotEmpty() == true } ?: emptyList()
        val broadcastSubscribers = mapSubscribers[Event::class.toString()] ?: emptyList()

        typeSpecificsubscribers.union(broadcastSubscribers).forEach { subscriber ->
            subscriber::class.members.filterIsInstance<KFunction<*>>()
                    .filter { func -> func.annotations.any { it.annotationClass == Subscribe::class } }
                    .filter { func -> func.parameters.find { it.kind == VALUE }?.type?.classifier in setOf(event::class, Event::class) }
                    .forEach {
                        threadPool.submit {
                            it.call(subscriber, event)
                        }
                    }
        }
    }

    private fun findAnnotatedFunctions(subscriber: Any): List<KFunction<*>> {
        return subscriber.javaClass.kotlin.members
            .filterIsInstance<KFunction<*>>()
            .filter { func -> func.annotations.any { it.annotationClass == Subscribe::class } }
    }

    private fun doesFunctionProvideExactlyOneParameter(func: KFunction<*>): Boolean {
        return func.parameters.count { it.kind == VALUE } == 1
    }

    private fun doesParameterImplementEvent(parameters: List<KParameter>): Boolean {
        return (parameters.find { it.kind == VALUE }!!.type.classifier as KClass<*>).isSubclassOf(Event::class)
    }

    private fun Collection<Annotation>.findSubscribeAnnotations(): Collection<Subscribe> {
        return filter { it.annotationClass == Subscribe::class }.map { it as Subscribe }
    }

    private fun upsertSubscriber(subscriber: Any, type: KClass<out Event>) {
        val eventClassName = type.toString()
        val currentSubscribers = mapSubscribers[eventClassName].let { subscriberEntry ->
            when(subscriberEntry) {
                null -> mutableSetOf()
                else -> subscriberEntry
            }
        }

        currentSubscribers.add(subscriber)
        mapSubscribers[eventClassName] = currentSubscribers
    }
}
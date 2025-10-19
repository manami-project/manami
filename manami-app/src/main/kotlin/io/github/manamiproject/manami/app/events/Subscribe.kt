package io.github.manamiproject.manami.app.events

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.reflect.KClass

/**
 * TODO 4.0.0: Remove
 */
@Target(FUNCTION)
@Retention(RUNTIME)
@Deprecated("Remove")
internal annotation class Subscribe(vararg val types: KClass<out Event>)
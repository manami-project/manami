package io.github.manamiproject.manami.app.events

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.reflect.KClass

@Target(FUNCTION)
@Retention(RUNTIME)
internal annotation class Subscribe(vararg val types: KClass<out Event>)
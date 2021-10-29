package io.github.manamiproject.manami.app.state.events

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.reflect.KClass

@Target(FUNCTION)
@Retention(RUNTIME)
annotation class Subscribe(vararg val types: KClass<out Event>)
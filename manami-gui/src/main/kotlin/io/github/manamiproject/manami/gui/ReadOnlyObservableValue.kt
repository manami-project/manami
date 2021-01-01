package io.github.manamiproject.manami.gui

import javafx.beans.InvalidationListener
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue

data class ReadOnlyObservableValue<T>(val obj: T) : ObservableValue<T> {

    constructor(creator: () -> T): this(creator())

    override fun addListener(listener: ChangeListener<in T>?) {
    }

    override fun addListener(listener: InvalidationListener?) {
    }

    override fun removeListener(listener: InvalidationListener?) {
    }

    override fun removeListener(listener: ChangeListener<in T>?) {
    }

    override fun getValue(): T = obj
}
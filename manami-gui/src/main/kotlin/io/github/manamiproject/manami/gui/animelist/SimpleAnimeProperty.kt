package io.github.manamiproject.manami.gui.animelist

import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.models.Anime
import javafx.beans.property.ObjectPropertyBase

class SimpleAnimeProperty(private val initialValue: Anime? = null): ObjectPropertyBase<Anime>(initialValue) {

    override fun getBean(): Any = this
    override fun getName(): String = EMPTY

    var value: Anime
        set(value) { super.setValue(value) }
        get() { return super.getValue() }
}
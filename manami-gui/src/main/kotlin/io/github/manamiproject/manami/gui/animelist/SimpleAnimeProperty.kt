package io.github.manamiproject.manami.gui.animelist

import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.anime.Anime
import javafx.beans.property.ObjectPropertyBase

class SimpleAnimeProperty(initialValue: Anime? = null): ObjectPropertyBase<Anime>(initialValue) {

    override fun getBean(): Any = this
    override fun getName(): String = EMPTY

    var value: Anime
        set(value) { super.setValue(value) }
        get() { return super.getValue() }
}
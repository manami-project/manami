package io.github.manamiproject.manami.gui.animelist

import io.github.manamiproject.manami.gui.animelist.AnimeFormTrigger.CREATE_CUSTOM
import io.github.manamiproject.modb.core.extensions.EMPTY
import javafx.beans.property.ObjectPropertyBase

class SimpleAnimeFormTriggerProperty(private var initialValue: AnimeFormTrigger = CREATE_CUSTOM): ObjectPropertyBase<AnimeFormTrigger>(initialValue) {

    override fun getBean(): Any = this
    override fun getName(): String = EMPTY

    var value: AnimeFormTrigger
        set(value) { super.setValue(value) }
        get() { return super.getValue() }
}
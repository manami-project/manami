package io.github.manamiproject.manami.gui.animelist

import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.modb.core.extensions.EMPTY
import javafx.beans.property.ObjectPropertyBase

class SimpleAnimeListEntryProperty(initialValue: AnimeListEntry? = null): ObjectPropertyBase<AnimeListEntry>(initialValue) {

    override fun getBean(): Any = this
    override fun getName(): String = EMPTY

    var value: AnimeListEntry
        set(value) { super.setValue(value) }
        get() { return super.getValue() }
}
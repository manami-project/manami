package io.github.manamiproject.manami.app

import io.github.manamiproject.modb.core.extensions.RegularFile

interface ManamiApp {

    fun newList()
    fun open(file: RegularFile)

    fun import(file: RegularFile)
    fun export(file: RegularFile)

    fun save()
    fun saveAs(file: RegularFile)

    fun undo()
    fun redo()

    fun search(searchString: String)
}
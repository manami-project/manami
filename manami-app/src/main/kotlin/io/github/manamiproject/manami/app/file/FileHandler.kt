package io.github.manamiproject.manami.app.file

import io.github.manamiproject.modb.core.extensions.RegularFile

interface FileHandler {

    fun newFile(ignoreUnsavedChanged: Boolean = false)
    fun open(file: RegularFile, ignoreUnsavedChanged: Boolean = false)

    fun isSaved(): Boolean
    fun isUnsaved(): Boolean
    fun save()
    fun saveAs(file: RegularFile)

    fun isUndoPossible(): Boolean
    fun undo()

    fun isRedoPossible(): Boolean
    fun redo()
}
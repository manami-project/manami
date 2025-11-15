package io.github.manamiproject.manami.app.file

import io.github.manamiproject.modb.core.extensions.RegularFile

interface FileHandler {

    fun newFile(ignoreUnsavedChanged: Boolean = false)
    suspend fun open(file: RegularFile, ignoreUnsavedChanged: Boolean = false)
    fun isOpenFileSet(): Boolean

    fun isSaved(): Boolean
    fun isUnsaved(): Boolean
    suspend fun save()
    suspend fun saveAs(file: RegularFile)

    fun isUndoPossible(): Boolean
    fun undo()

    fun isRedoPossible(): Boolean
    fun redo()
}
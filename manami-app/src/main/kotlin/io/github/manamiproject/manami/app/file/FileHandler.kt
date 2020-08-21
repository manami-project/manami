package io.github.manamiproject.manami.app.file

import io.github.manamiproject.modb.core.extensions.RegularFile

interface FileHandler {

    fun newFile()
    fun open(file: RegularFile)

    fun save()
    fun saveAs(file: RegularFile)
}
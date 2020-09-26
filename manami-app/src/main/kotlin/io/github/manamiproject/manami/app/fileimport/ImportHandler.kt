package io.github.manamiproject.manami.app.fileimport

import io.github.manamiproject.modb.core.extensions.RegularFile

interface ImportHandler {

    fun import(file: RegularFile)
}
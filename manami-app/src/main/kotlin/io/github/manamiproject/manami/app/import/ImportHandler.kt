package io.github.manamiproject.manami.app.import

import io.github.manamiproject.modb.core.extensions.RegularFile

interface ImportHandler {

    fun import(file: RegularFile)
}
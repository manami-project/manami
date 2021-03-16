package io.github.manamiproject.manami.app.file

import io.github.manamiproject.modb.core.extensions.RegularFile

interface FileWriter {

    fun writeTo(file: RegularFile)
}
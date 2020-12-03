package io.github.manamiproject.manami.app.export

import io.github.manamiproject.modb.core.extensions.RegularFile

interface ExportHandler {

    fun export(file: RegularFile, fileFormat: FileFormat)
}
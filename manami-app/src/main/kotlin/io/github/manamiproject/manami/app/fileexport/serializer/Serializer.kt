package io.github.manamiproject.manami.app.fileexport.serializer

import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.extensions.RegularFile

interface Serializer {

    fun serialize(file: RegularFile)

    /**
     * @return The supported file suffix without a dot. **Example** _json_
     */
    fun handles(): FileSuffix
}
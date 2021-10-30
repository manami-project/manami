package io.github.manamiproject.manami.app.file

import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.extensions.RegularFile

internal interface Parser<T> {

    fun parse(file: RegularFile): T

    /**
     * @return The supported file suffix without a dot. **Example** _xml_
     */
    fun handlesSuffix(): FileSuffix
}
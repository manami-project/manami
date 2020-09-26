package io.github.manamiproject.manami.app.fileimport.parser

import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.extensions.RegularFile

interface Parser {

    fun parse(file: RegularFile): ParsedFile

    /**
     * @return The supported file suffix without a dot. **Example** _json_
     */
    fun handlesSuffix(): FileSuffix
}
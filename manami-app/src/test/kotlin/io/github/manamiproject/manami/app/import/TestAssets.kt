package io.github.manamiproject.manami.app.import

import io.github.manamiproject.manami.app.import.parser.ParsedFile
import io.github.manamiproject.manami.app.import.parser.Parser
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.test.shouldNotBeInvoked

object TestImportParser: Parser<ParsedFile> {
    override fun parse(file: RegularFile): ParsedFile = shouldNotBeInvoked()
    override fun handlesSuffix(): FileSuffix = shouldNotBeInvoked()
}
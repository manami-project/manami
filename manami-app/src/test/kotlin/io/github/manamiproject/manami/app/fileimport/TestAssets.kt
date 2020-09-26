package io.github.manamiproject.manami.app.fileimport

import io.github.manamiproject.manami.app.fileimport.parser.ParsedFile
import io.github.manamiproject.manami.app.fileimport.parser.Parser
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.test.shouldNotBeInvoked

object TestParser: Parser {
    override fun parse(file: RegularFile): ParsedFile = shouldNotBeInvoked()
    override fun handlesSuffix(): FileSuffix = shouldNotBeInvoked()
}
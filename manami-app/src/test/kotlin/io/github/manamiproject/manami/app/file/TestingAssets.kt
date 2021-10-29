package io.github.manamiproject.manami.app.file

import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.test.shouldNotBeInvoked

object TestManamiFileParser: Parser<ParsedManamiFile> {
    override fun parse(file: RegularFile): ParsedManamiFile = shouldNotBeInvoked()
    override fun handlesSuffix(): FileSuffix = shouldNotBeInvoked()
}

object TestFileWriter: FileWriter {
    override fun writeTo(file: RegularFile) = shouldNotBeInvoked()
}
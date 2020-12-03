package io.github.manamiproject.manami.app.import

import io.github.manamiproject.manami.app.import.parser.Parser
import io.github.manamiproject.manami.app.state.commands.GenericReversibleCommand
import io.github.manamiproject.manami.app.import.parser.manami.ManamiLegacyFileParser
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.fileSuffix
import io.github.manamiproject.modb.core.extensions.regularFileExists

internal class DefaultImportHandler(
        private val parserList: List<Parser> = listOf(ManamiLegacyFileParser())
) : ImportHandler {

    init {
        require(parserList.isNotEmpty()) { "List of parser must not be empty" }
        require(hasOnlyOneParserPerSuffix()) { "Only one parser per file suffix" }
    }

    override fun import(file: RegularFile) {
        require(file.regularFileExists()) { "Given path doesn't exist or is not a file [${file.toAbsolutePath()}]" }

        val parser = parserList.find { it.handlesSuffix() == file.fileSuffix() } ?: throw IllegalArgumentException("No suitable parser for file type [${file.fileSuffix()}]")
        val content = parser.parse(file)
        GenericReversibleCommand(command = CmdAddEntriesFromParsedFile(parsedFile = content)).execute()
    }

    private fun hasOnlyOneParserPerSuffix(): Boolean {
        return parserList.groupBy { it.handlesSuffix() }.filter { it.value.size > 1 }.count() == 0
    }
}
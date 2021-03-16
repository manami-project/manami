package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.import.parser.Parser
import io.github.manamiproject.manami.app.state.CurrentFile
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.commands.history.CommandHistory
import io.github.manamiproject.manami.app.state.commands.history.DefaultCommandHistory
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.createFile
import io.github.manamiproject.modb.core.extensions.regularFileExists
import io.github.manamiproject.modb.core.logging.LoggerDelegate

internal class DefaultFileHandler(
        private val state: State = InternalState,
        private val commandHistory: CommandHistory = DefaultCommandHistory,
        private val parser: Parser<ParsedManamiFile> = FileParser(),
        private val fileWriter: FileWriter = DefaultFileWriter(),
) : FileHandler {

    override fun newFile(ignoreUnsavedChanged: Boolean) {
        log.info("Creating new file having [ignoreUnsavedChanged={}]", ignoreUnsavedChanged)

        if (!ignoreUnsavedChanged) {
            check(commandHistory.isSaved()) { "Cannot create a new list, because there are unsaved changes." }
        }

        CmdNewFile(
            state = state,
            commandHistory = commandHistory,
        ).execute()
    }

    override fun open(file: RegularFile, ignoreUnsavedChanged: Boolean) {
        log.info("Opening file [{}] having [ignoreUnsavedChanged={}]", file, ignoreUnsavedChanged)

        if (!ignoreUnsavedChanged) {
            check(commandHistory.isSaved()) { "Cannot open file, because there are unsaved changes." }
        }

        val parsedFile = parser.parse(file)

        CmdOpenFile(
            state = state,
            commandHistory = commandHistory,
            parsedFile = parsedFile,
            file = file,
        ).execute()
    }

    override fun isOpenFileSet(): Boolean = state.openedFile() is CurrentFile

    override fun isSaved(): Boolean = commandHistory.isSaved()

    override fun isUnsaved(): Boolean = commandHistory.isUnsaved()

    override fun save() {
        if (commandHistory.isSaved()) {
            return
        }

        val file = state.openedFile()
        check(file is CurrentFile)

        fileWriter.writeTo(file.regularFile)
        commandHistory.save()
    }

    override fun saveAs(file: RegularFile) {
        if (!file.regularFileExists()) {
            file.createFile()
        }

        state.setOpenedFile(file)
        save()
    }

    override fun isUndoPossible(): Boolean = commandHistory.isUndoPossible()

    override fun undo() = commandHistory.undo()

    override fun isRedoPossible(): Boolean = commandHistory.isRedoPossible()

    override fun redo() = commandHistory.redo()

    private companion object {
        private val log by LoggerDelegate()
    }
}
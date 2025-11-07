package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.commands.history.CommandHistory
import io.github.manamiproject.manami.app.commands.history.DefaultCommandHistory
import io.github.manamiproject.manami.app.events.CoroutinesFlowEventBus
import io.github.manamiproject.manami.app.events.EventBus
import io.github.manamiproject.manami.app.state.CurrentFile
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.fileName
import io.github.manamiproject.modb.core.extensions.regularFileExists
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.flow.update
import kotlin.io.path.createFile

internal class DefaultFileHandler(
    private val state: State = InternalState,
    private val commandHistory: CommandHistory = DefaultCommandHistory,
    private val parser: Parser<ParsedManamiFile> = FileParser(),
    private val fileWriter: FileWriter = DefaultFileWriter(),
    private val eventBus: EventBus = CoroutinesFlowEventBus,
) : FileHandler {

    override fun newFile(ignoreUnsavedChanged: Boolean) {
        log.info { "Creating new file using [ignoreUnsavedChanged=$ignoreUnsavedChanged]" }

        if (!ignoreUnsavedChanged) {
            check(commandHistory.isSaved()) { "Cannot create a new list, because there are unsaved changes." }
        }

        eventBus.clear()

        CmdNewFile(
            state = state,
            commandHistory = commandHistory,
        ).execute()
    }

    override fun open(file: RegularFile, ignoreUnsavedChanged: Boolean) {
        log.info { "Opening file [$file] using [ignoreUnsavedChanged=$ignoreUnsavedChanged]" }

        if (!ignoreUnsavedChanged) {
            check(commandHistory.isSaved()) { "Cannot open file, because there are unsaved changes." }
        }

        val parsedFile = parser.parse(file)

        eventBus.clear()

        CmdOpenFile(
            state = state,
            commandHistory = commandHistory,
            parsedFile = parsedFile,
            file = file,
        ).execute()

        eventBus.generalAppState.update { current -> current.copy(openedFile = file.fileName()) }
    }

    override fun isOpenFileSet(): Boolean = state.openedFile() is CurrentFile

    override fun isSaved(): Boolean = commandHistory.isSaved()

    override fun isUnsaved(): Boolean = commandHistory.isUnsaved()

    override suspend fun save() {
        if (commandHistory.isSaved()) {
            return
        }

        val file = state.openedFile()
        check(file is CurrentFile) { "No file set" }

        fileWriter.writeTo(file.regularFile)
        commandHistory.save()
    }

    override suspend fun saveAs(file: RegularFile) {
        if (!file.regularFileExists()) {
            file.createFile()
        }

        state.setOpenedFile(file)
        eventBus.generalAppState.update { current -> current.copy(openedFile = file.fileName()) }
        save()
    }

    override fun isUndoPossible(): Boolean = commandHistory.isUndoPossible()

    override fun undo() = commandHistory.undo()

    override fun isRedoPossible(): Boolean = commandHistory.isRedoPossible()

    override fun redo() = commandHistory.redo()

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DefaultFileHandler]
         * @since 4.0.0
         */
        val instance: DefaultFileHandler by lazy { DefaultFileHandler() }
    }
}
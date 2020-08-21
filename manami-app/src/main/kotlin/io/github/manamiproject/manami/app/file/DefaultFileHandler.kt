package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.regularFileExists
import io.github.manamiproject.modb.core.logging.LoggerDelegate

internal class DefaultFileHandler(private val state: State = InternalState) : FileHandler {

    override fun newFile() {
        log.info("Creating new file.")
        state.closeFile()
    }

    override fun open(file: RegularFile) {
        log.info("Opening file [{}]", file)
        check(file.regularFileExists())
        state.openedFile(file)
    }

    override fun save() {
        TODO("Not yet implemented")
    }

    override fun saveAs(file: RegularFile) {
        TODO("Not yet implemented")
    }

    companion object {
        private val log by LoggerDelegate()
    }
}
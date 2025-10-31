package io.github.manamiproject.manami.gui.components

import androidx.compose.ui.window.FrameWindowScope
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.fileName
import java.awt.Dialog.ModalityType.APPLICATION_MODAL
import java.io.File
import java.io.FilenameFilter

fun FrameWindowScope.showOpenFileDialog(): RegularFile? {
    val fileFilter: (String) -> Boolean = { fileName -> fileName.endsWith(".xml") }

    val files: Array<File> = java.awt.FileDialog(window).apply {
        mode = 0
        isVisible = true
        isMultipleMode = false
        title = "Select your anime list..."
        filenameFilter = FilenameFilter { _, name ->
            fileFilter(name)
        }
        modalityType = APPLICATION_MODAL
    }.files

    if (files.isEmpty()) {
        return null
    }

    val file = files.first().toPath()
    val fileName = file.fileName()

    return if (fileFilter(fileName)) {
        return file
    } else {
        null
    }
}

fun FrameWindowScope.showSaveAsFileDialog(): RegularFile? {
    val fileFilter: (String) -> Boolean = { fileName -> fileName.endsWith(".xml") }

    val files: Array<File> = java.awt.FileDialog(window).apply {
        mode = 1
        isVisible = true
        isMultipleMode = false
        title = "Save your anime list as..."
        filenameFilter = FilenameFilter { _, name ->
            fileFilter(name)
        }
        modalityType = APPLICATION_MODAL
    }.files

    if (files.isEmpty()) {
        return null
    }

    val file = files.first().toPath()
    val fileName = file.fileName()

    return if (fileFilter(fileName)) {
        return file
    } else {
        null
    }
}
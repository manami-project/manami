package io.github.manamiproject.manami.gui.components

import io.github.manamiproject.modb.core.extensions.Directory
import javax.swing.JFileChooser
import kotlin.io.path.isDirectory

internal fun showOpenDirectoryDialog(): Directory? {
    val chooser = JFileChooser().apply {
        fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    }
    val result = chooser.showOpenDialog(null)

    if (result != JFileChooser.APPROVE_OPTION) {
        return null
    }

    val directory = chooser.selectedFile.toPath()
    check(directory.isDirectory()) { "Selection is not a directory." }

    return directory
}
package io.github.manamiproject.manami.gui.components

import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.RegularFile
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.Stage

object PathChooser {

    private val XML_FILTER = ExtensionFilter("XML", "*.xml")

    fun showOpenFileDialog(stage: Stage): RegularFile? {
        val fileChooser = FileChooser().apply {
            title = "Select your anime list..."
            extensionFilters.addAll(XML_FILTER)
        }

        return fileChooser.showOpenDialog(stage).let { it?.toPath() }
    }

    fun showSaveAsFileDialog(stage: Stage): RegularFile? {
        val fileChooser = FileChooser().apply {
            title = "Save your anime list as..."
            extensionFilters.addAll(XML_FILTER)
        }

        return fileChooser.showSaveDialog(stage).let { it?.toPath() }
    }

    fun showBrowseForFolderDialog(stage: Stage): Directory? {
        val directoryChooser = DirectoryChooser().apply {
            title = "Browse for directory..."
        }

        return directoryChooser.showDialog(stage).let { it?.toPath() }
    }
}
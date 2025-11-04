package io.github.manamiproject.manami.gui.components.unsavedchangesdialog

internal data class UnsavedChangesDialogState(
    val showUnsavedChangesDialog: Boolean = false,
    val onCloseRequest: () -> Unit = {},
    val onYes: () -> Unit = {},
    val onNo: () -> Unit = {},
)

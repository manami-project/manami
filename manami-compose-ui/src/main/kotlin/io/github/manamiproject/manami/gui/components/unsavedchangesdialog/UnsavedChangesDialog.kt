package io.github.manamiproject.manami.gui.components.unsavedchangesdialog

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState

@Composable
internal fun UnsavedChangesDialog(
    onCloseRequest: () -> Unit,
    onYes: () -> Unit,
    onNo: () -> Unit,
) {
    DialogWindow(
        onCloseRequest = onCloseRequest,
        title = "Unsaved changes",
        state = rememberDialogState(width = 400.dp, height = 150.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            Text("""
                Your changes will be lost if you don't save them.
                "Do you want to save your changes?
            """.trimIndent()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(Modifier.align(Alignment.CenterHorizontally)) {
                Button(onClick = onCloseRequest) {
                    Text("cancel")
                }
                Spacer(modifier = Modifier.width(80.dp))
                Button(onClick = { onCloseRequest(); onYes() }) {
                    Text("yes")
                }
                Spacer(modifier = Modifier.width(20.dp))
                Button(onClick = { onCloseRequest(); onNo() }) {
                    Text("no")
                }
            }
        }
    }
}
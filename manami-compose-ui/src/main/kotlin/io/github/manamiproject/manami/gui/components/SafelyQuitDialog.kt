package io.github.manamiproject.manami.gui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState

@Composable
internal fun SafelyQuitDialog(onCloseRequest: () -> Unit) {
    DialogWindow(
        onCloseRequest = onCloseRequest,
        title = "About",
        state = rememberDialogState(width = 420.dp, height = 140.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            Text("""
                Always quit the app using File -> Quit to prevent data loss.
            """.trimIndent()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(Modifier.align(Alignment.CenterHorizontally)) {
                Button(onClick = onCloseRequest) {
                    Text("ok")
                }
            }
        }
    }
}
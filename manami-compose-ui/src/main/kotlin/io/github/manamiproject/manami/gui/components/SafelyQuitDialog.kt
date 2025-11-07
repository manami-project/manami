package io.github.manamiproject.manami.gui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import io.github.manamiproject.manami.gui.theme.ThemeState

@Composable
internal fun SafelyQuitDialog(onCloseRequest: () -> Unit) {
    DialogWindow(
        onCloseRequest = onCloseRequest,
        title = "Important",
        state = rememberDialogState(width = 420.dp, height = 180.dp),
    ) {
        Box( // unable to modify the background of the main window so painting a custom one is necessary
            modifier = Modifier
                .fillMaxSize()
                .background(ThemeState.instance.currentScheme.value.background)
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                Text(
                    """
                    Always quit the app using File -> Quit to prevent data loss.
                """.trimIndent()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(Modifier.align(Alignment.CenterHorizontally)) {
                    Button(onClick = onCloseRequest) {
                        Text("OK")
                    }
                }
            }
        }
    }
}
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
import io.github.manamiproject.manami.app.versioning.ResourceBasedVersionProvider
import io.github.manamiproject.manami.gui.theme.ManamiTheme
import io.github.manamiproject.manami.gui.theme.ThemeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

@Composable
internal fun About(onCloseRequest: () -> Unit) {
    var licenseLink by remember { mutableStateOf("AGPL-3.0") }
    CoroutineScope(Dispatchers.IO).async {
        licenseLink = "https://github.com/manami-project/manami/blob/${ResourceBasedVersionProvider.version().version}/LICENSE"
    }

    DialogWindow(
        onCloseRequest = onCloseRequest,
        title = "About",
        state = rememberDialogState(width = 800.dp, height = 300.dp),
    ) {
        ManamiTheme {
            Box( // unable to modify the background of the main window so painting a custom one is necessary
                modifier = Modifier
                    .fillMaxSize()
                    .background(ThemeState.instance.currentScheme.value.background)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                    Text(
                        """
                        Free non-commercial software. (AGPLv3.0)
        
                        Project / Source code: https://github.com/manami-project/manami
                        License: $licenseLink
                        
                        Uses data from https://github.com/manami-project/anime-offline-database
                        which is made available here under the Open Database License (ODbL)
                        License: https://opendatacommons.org/licenses/odbl/1-0/
                    """.trimIndent()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(Modifier.align(Alignment.CenterHorizontally)) {
                        Button(onClick = onCloseRequest) {
                            Text("close")
                        }
                    }
                }
            }
        }
    }
}
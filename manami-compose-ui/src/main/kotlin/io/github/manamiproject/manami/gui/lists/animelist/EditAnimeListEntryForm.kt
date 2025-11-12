package io.github.manamiproject.manami.gui.lists.animelist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import io.github.manamiproject.manami.app.lists.NoLink
import io.github.manamiproject.manami.gui.theme.ManamiTheme
import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE
import io.github.manamiproject.modb.core.anime.AnimeType
import io.github.manamiproject.modb.core.extensions.EMPTY
import javax.swing.JFileChooser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditAnimeListEntryForm(editAnimeListEntryFormViewModel: EditAnimeListEntryFormViewModel = EditAnimeListEntryFormViewModel.instance) {
    val animeListEntry = editAnimeListEntryFormViewModel.animeListEntry.collectAsState()

    var link by remember { mutableStateOf(EMPTY) }
    var title by remember { mutableStateOf(EMPTY) }
    var episodes by remember { mutableStateOf(0) }
    var typeSelectExpanded by remember { mutableStateOf(false) }
    val types = AnimeType.entries.map { it.toString().capitalize(Locale.current) }
    val typeSelectState = rememberTextFieldState(types.last())
    var thumbnail by remember { mutableStateOf(EMPTY) }
    var location by remember { mutableStateOf(EMPTY) }

    LaunchedEffect(animeListEntry.value) {
        link = if((animeListEntry.value?.link ?: NoLink) == NoLink) EMPTY else animeListEntry.value!!.link.asLink().uri.toString()
        title = animeListEntry.value?.title ?: EMPTY
        episodes = animeListEntry.value?.episodes ?: 0
        typeSelectState.setTextAndPlaceCursorAtEnd(animeListEntry.value?.type?.toString() ?: EMPTY)
        thumbnail = animeListEntry.value?.thumbnail?.toString() ?: NO_PICTURE.toString()
        location = animeListEntry.value?.location?.toString() ?: EMPTY
    }

    ManamiTheme {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Add anime to anime list", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = link,
                onValueChange = { link = it },
                label = { Text("Link to meta data provider") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = episodes.toString(),
                onValueChange = { new ->
                    if (new.toIntOrNull() != null) {
                        episodes = new.toInt()
                    }
                },
                label = { Text("Episodes") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = typeSelectExpanded,
                onExpandedChange = { typeSelectExpanded = it },
            ) {
                TextField(
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    state = typeSelectState,
                    readOnly = true,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    label = { Text("MetaDataProvider") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeSelectExpanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                )
                ExposedDropdownMenu(expanded = typeSelectExpanded, onDismissRequest = { typeSelectExpanded = false }) {
                    types.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                typeSelectState.setTextAndPlaceCursorAtEnd(option)
                                typeSelectExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }

            OutlinedTextField(
                value = thumbnail,
                onValueChange = { thumbnail = it },
                label = { Text("Link to thumbnail") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location in the filesystem") },
                    readOnly = true,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 5.dp)
                )
                Button(
                    onClick = {
                        val chooser = JFileChooser().apply {
                            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                        }
                        val result = chooser.showOpenDialog(null)
                        if (result == JFileChooser.APPROVE_OPTION) {
                            location = chooser.selectedFile.absolutePath
                        }
                    },
                ) {
                    Text("Choose")
                }
            }

            Button(
                onClick = {
                    editAnimeListEntryFormViewModel.update(
                        link,
                        title,
                        episodes,
                        typeSelectState.text.toString(),
                        thumbnail,
                        location,
                    )
                }
            ) {
                Text("Update")
            }
        }
    }
}
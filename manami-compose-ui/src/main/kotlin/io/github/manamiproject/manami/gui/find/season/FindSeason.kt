package io.github.manamiproject.manami.gui.find.season

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.TextField
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.manamiproject.manami.gui.components.IconButton
import io.github.manamiproject.manami.gui.components.RotatingDotsProgress
import io.github.manamiproject.manami.gui.components.animetable.AnimeTable
import io.github.manamiproject.manami.gui.theme.ManamiTheme
import io.github.manamiproject.modb.core.extensions.eitherNullOrBlank

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FindSeason(viewModel: FindSeasonViewModel = FindSeasonViewModel.instance) {
    val isSeasonSearchRunning = viewModel.isSeasonSearchRunning.collectAsState()

    val metaDataProviders = viewModel.metaDataProviders.collectAsState()
    var metaDataProviderSelectExpanded by remember { mutableStateOf(false) }
    val metaDataProviderSelectState = remember {
        TextFieldState(viewModel.metaDataProviderText)
    }

    LaunchedEffect(metaDataProviders.value) {
        if (metaDataProviders.value.isNotEmpty() && viewModel.metaDataProviderText.eitherNullOrBlank()) {
            metaDataProviderSelectState.setTextAndPlaceCursorAtEnd(metaDataProviders.value.first())
            viewModel.metaDataProviderText = metaDataProviders.value.first()
        }
    }

    var seasonSelectExpanded by remember { mutableStateOf(false) }
    val seasonOptions = viewModel.seasons()
    val seasonSelectState = remember {
        TextFieldState(viewModel.seasonSelectedText)
    }

    var yearSelectExpanded by remember { mutableStateOf(false) }
    val yearOptions = viewModel.yearRange()
    val yearSelectState = remember {
        TextFieldState(viewModel.yearSelectedText.toString())
    }

    val padding = 5.dp

    ManamiTheme {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ExposedDropdownMenuBox(
                expanded = metaDataProviderSelectExpanded,
                onExpandedChange = { metaDataProviderSelectExpanded = it },
                modifier = Modifier.padding(0.dp, 0.dp,padding, 0.dp)
            ) {
                TextField(
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    state = metaDataProviderSelectState,
                    readOnly = true,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    label = { Text("MetaDataProvider") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = metaDataProviderSelectExpanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                )
                ExposedDropdownMenu(expanded = metaDataProviderSelectExpanded, onDismissRequest = { metaDataProviderSelectExpanded = false }) {
                    metaDataProviders.value.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                metaDataProviderSelectState.setTextAndPlaceCursorAtEnd(option)
                                viewModel.metaDataProviderText = option
                                metaDataProviderSelectExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = seasonSelectExpanded,
                onExpandedChange = { seasonSelectExpanded = it },
                modifier = Modifier.padding(0.dp, 0.dp,padding, 0.dp)
            ) {
                TextField(
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    state = seasonSelectState,
                    readOnly = true,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    label = { Text("Season") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = seasonSelectExpanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                )
                ExposedDropdownMenu(expanded = seasonSelectExpanded, onDismissRequest = { seasonSelectExpanded = false }) {
                    seasonOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                seasonSelectState.setTextAndPlaceCursorAtEnd(option)
                                viewModel.seasonSelectedText = option
                                seasonSelectExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = yearSelectExpanded,
                onExpandedChange = { yearSelectExpanded = it },
                modifier = Modifier.padding(0.dp, 0.dp,padding, 0.dp)
            ) {
                TextField(
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    state = yearSelectState,
                    readOnly = true,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    label = { Text("Year") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearSelectExpanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                )
                ExposedDropdownMenu(expanded = yearSelectExpanded, onDismissRequest = { yearSelectExpanded = false }) {
                    yearOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.toString(), style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                yearSelectState.setTextAndPlaceCursorAtEnd(option.toString())
                                viewModel.yearSelectedText = option
                                yearSelectExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }

            IconButton(
                icon = Icons.Filled.Search,
                size = 40.dp,
                description = "Search",
                onClick = { viewModel.search(metaDataProviderSelectState.text.toString(), seasonSelectState.text.toString(), yearSelectState.text.toString().toInt()) },
            )
        }

        if (isSeasonSearchRunning.value) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.align(Alignment.Center)) {
                    RotatingDotsProgress()
                }
            }
        } else {
            AnimeTable(viewModel = viewModel)
        }
    }
}
package io.github.manamiproject.manami.gui.migration

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.manamiproject.manami.gui.components.RotatingDotsProgress
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Migration(viewModel: MigrationViewModel = MigrationViewModel.instance) {
    val isRunning = viewModel.isRunning.collectAsState()
    val containsResults = viewModel.containsResults.collectAsState()
    val metaDataProviders = viewModel.metaDataProviders.collectAsState()
    val entries = viewModel.entries.collectAsState()

    var metaDataProviderFromSelectExpanded by remember { mutableStateOf(false) }
    val metaDataProviderFromSelectState = remember {
        TextFieldState(viewModel.metaDataProviderFromText)
    }

    var metaDataProviderToSelectExpanded by remember { mutableStateOf(false) }
    val metaDataProviderToSelectState = remember {
        TextFieldState(viewModel.metaDataProviderToText)
    }

    LaunchedEffect(entries) {
        viewModel.restoreScrollPosition()
    }

    DisposableEffect(viewModel) {
        onDispose {
            viewModel.saveScrollPosition()
        }
    }

    ManamiTheme {
        if (isRunning.value) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.align(Alignment.Center)) {
                    RotatingDotsProgress()
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ExposedDropdownMenuBox(
                    expanded = metaDataProviderFromSelectExpanded,
                    onExpandedChange = { metaDataProviderFromSelectExpanded = it },
                    modifier = Modifier.padding(0.dp, 0.dp,5.dp, 0.dp),
                ) {
                    TextField(
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        state = metaDataProviderFromSelectState,
                        readOnly = true,
                        lineLimits = TextFieldLineLimits.SingleLine,
                        label = { Text("MetaDataProvider") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = metaDataProviderFromSelectExpanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    )
                    ExposedDropdownMenu(expanded = metaDataProviderFromSelectExpanded, onDismissRequest = { metaDataProviderFromSelectExpanded = false }) {
                        metaDataProviders.value.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
                                onClick = {
                                    metaDataProviderFromSelectState.setTextAndPlaceCursorAtEnd(option)
                                    viewModel.metaDataProviderFromText = option
                                    metaDataProviderFromSelectExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
                Text(
                    text = "=>",
                    modifier = Modifier.padding(0.dp, 0.dp,5.dp, 0.dp),
                )
                ExposedDropdownMenuBox(
                    expanded = metaDataProviderToSelectExpanded,
                    onExpandedChange = { metaDataProviderToSelectExpanded = it },
                    modifier = Modifier.padding(0.dp, 0.dp,5.dp, 0.dp),
                ) {
                    TextField(
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        state = metaDataProviderToSelectState,
                        readOnly = true,
                        lineLimits = TextFieldLineLimits.SingleLine,
                        label = { Text("MetaDataProvider") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = metaDataProviderToSelectExpanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    )
                    ExposedDropdownMenu(expanded = metaDataProviderToSelectExpanded, onDismissRequest = { metaDataProviderToSelectExpanded = false }) {
                        metaDataProviders.value.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
                                onClick = {
                                    metaDataProviderToSelectState.setTextAndPlaceCursorAtEnd(option)
                                    viewModel.metaDataProviderToText = option
                                    metaDataProviderToSelectExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
                Button(
                    onClick = { viewModel.start() },
                    modifier = Modifier.padding(0.dp, 0.dp,5.dp, 0.dp),
                ) {
                    Text("Start")
                }
                if (containsResults.value) {
                    Button(
                        onClick = { viewModel.migrate() },
                    ) {
                        Text("Migrate")
                    }
                }
            }
            if (entries.value.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = viewModel.listState,
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                    ) {
                        items(items = entries.value, key = { "${it.animeEntry.link}-${it.animeEntry.title}" }) { entry ->
                            MigrationSelectionEntry(entry)
                        }
                    }
                    VerticalScrollbar(
                        adapter = rememberScrollbarAdapter(viewModel.listState),
                        modifier = Modifier.align(Alignment.CenterEnd),
                    )
                }
            }
        }
    }
}
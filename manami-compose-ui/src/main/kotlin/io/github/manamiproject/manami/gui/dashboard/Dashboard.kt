package io.github.manamiproject.manami.gui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import io.github.manamiproject.manami.gui.components.simpletable.SimpleTable
import io.github.manamiproject.manami.gui.extensions.toOnClick
import io.github.manamiproject.manami.gui.theme.ManamiTheme
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import java.net.URI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Dashboard(viewModel: DashboardViewModel = DashboardViewModel.instance) {
    val isLoading = viewModel.isLoading.collectAsState()
    val data = viewModel.metaDataProviderNumberOfAnime.collectAsState()
    val newVersion = viewModel.newVersion.collectAsState()

    var metaDataProviderSelectExpanded by remember { mutableStateOf(false) }
    val metaDataProviders = viewModel.metaDataProviders.collectAsState()
    val metaDataProviderSelectState = rememberTextFieldState()

    LaunchedEffect(metaDataProviders.value) {
        if (metaDataProviders.value.isNotEmpty()) {
            metaDataProviderSelectState.setTextAndPlaceCursorAtEnd(metaDataProviders.value.first())
        }
    }

    val searchState = rememberTextFieldState(EMPTY)

    ManamiTheme {
        if (newVersion.value.neitherNullNorBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = URI("https://github.com/manami-project/manami/releases/tag/${newVersion.value}").toOnClick()) {
                    Text("\uD83D\uDE80 ${newVersion.value} available")
                }
            }
        }

        if (isLoading.value) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.align(Alignment.Center)) {
                    RotatingDotsProgress()
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(0.dp, 15.dp, 0.dp, 0.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ExposedDropdownMenuBox(
                    expanded = metaDataProviderSelectExpanded,
                    onExpandedChange = { metaDataProviderSelectExpanded = it },
                    modifier = Modifier.padding(0.dp, 0.dp,5.dp, 0.dp)
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
                                    metaDataProviderSelectExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
                TextField(
                    state = searchState,
                    readOnly = false,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    label = { Text("Title") },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier.width(600.dp).padding(0.dp, 0.dp, 5.dp, 0.dp),
                )
                IconButton(
                    icon = Icons.Filled.Search,
                    size = 40.dp,
                    description = "Search",
                    onClick = { viewModel.findByTitle(metaDataProviderSelectState.text.toString(), searchState.text.toString()) },
                )
            }
            SimpleTable(data.value) {
                keyHeadline = "MetaDataProvider"
                valueHeadline = "Number of anime"
            }
        }
    }
}
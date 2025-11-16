package io.github.manamiproject.manami.gui.find.inconsistencies

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.manamiproject.manami.gui.find.inconsistencies.FixSelection.CURRENT
import io.github.manamiproject.manami.gui.find.inconsistencies.FixSelection.REPLACEMENT
import io.github.manamiproject.manami.gui.tabs.TabBarViewModel
import io.github.manamiproject.manami.gui.tabs.Tabs.FIX_ANIME_LIST_ENTRY_INCONSISTENCIES_FORM

@Composable
internal fun FixAnimeListEntryInconsistenciesForm(
    viewModel: FixAnimeListEntryInconsistenciesFormViewModel = FixAnimeListEntryInconsistenciesFormViewModel.instance,
    tabBarViewModel: TabBarViewModel = TabBarViewModel.instance,
) {
    val titleCurrentValue = viewModel.titleCurrentValue.collectAsState()
    val titleReplacementValue = viewModel.titleReplacementValue.collectAsState()

    val episodesCurrentValue = viewModel.episodesCurrentValue.collectAsState()
    val episodesReplacementValue = viewModel.episodesReplacementValue.collectAsState()

    val typeCurrentValue = viewModel.typeCurrentValue.collectAsState()
    val typeReplacementValue = viewModel.typeReplacementValue.collectAsState()

    Column(Modifier.padding(16.dp).fillMaxWidth()) {
        if (titleCurrentValue.value != titleReplacementValue.value) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Title:")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                RadioButton(
                    selected = viewModel.titleSelection.value == CURRENT,
                    onClick = { viewModel.selectTitle(CURRENT) },
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    OutlinedTextField(
                        value = titleCurrentValue.value,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Current title") },
                        modifier = Modifier,
                    )
                }

            }

            Spacer(Modifier.height(5.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                RadioButton(
                    selected = viewModel.titleSelection.value == REPLACEMENT,
                    onClick = { viewModel.selectTitle(REPLACEMENT) },
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    OutlinedTextField(
                        value = titleReplacementValue.value,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Replacement") },
                        modifier = Modifier,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }


        if (episodesCurrentValue.value != episodesReplacementValue.value) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Episodes:")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                RadioButton(
                    selected = viewModel.episodesSelection.value == CURRENT,
                    onClick = { viewModel.selectEpisodes(CURRENT) },
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    OutlinedTextField(
                        value = episodesCurrentValue.value,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Current number of episodes") },
                        modifier = Modifier,
                    )
                }

            }

            Spacer(Modifier.height(5.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                RadioButton(
                    selected = viewModel.episodesSelection.value == REPLACEMENT,
                    onClick = { viewModel.selectEpisodes(REPLACEMENT) },
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    OutlinedTextField(
                        value = episodesReplacementValue.value,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Replacement") },
                        modifier = Modifier,
                    )
                }
            }

            Spacer(Modifier.height(15.dp))
        }


        if (typeCurrentValue.value != typeReplacementValue.value) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Type:")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                RadioButton(
                    selected = viewModel.typeSelection.value == CURRENT,
                    onClick = { viewModel.selectType(CURRENT) },
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    OutlinedTextField(
                        value = typeCurrentValue.value,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Current type") },
                        modifier = Modifier,
                    )
                }

            }

            Spacer(Modifier.height(5.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                RadioButton(
                    selected = viewModel.typeSelection.value == REPLACEMENT,
                    onClick = { viewModel.selectType(REPLACEMENT) },
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    OutlinedTextField(
                        value = typeReplacementValue.value,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Replacement") },
                        modifier = Modifier,
                    )
                }
            }

            Spacer(Modifier.height(15.dp))
        }

        Button(onClick = {
            viewModel.update()
            tabBarViewModel.closeTab(FIX_ANIME_LIST_ENTRY_INCONSISTENCIES_FORM)
        }) {
            Text("Update")
        }
    }
}
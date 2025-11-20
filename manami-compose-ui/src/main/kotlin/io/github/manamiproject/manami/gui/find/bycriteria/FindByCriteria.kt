package io.github.manamiproject.manami.gui.find.bycriteria

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.unit.dp
import io.github.manamiproject.manami.app.search.FindByCriteriaConfig
import io.github.manamiproject.manami.app.search.FindByCriteriaConfig.SearchConjunction.AND
import io.github.manamiproject.manami.gui.components.DualPaneMultiSelect
import io.github.manamiproject.manami.gui.components.RangeSelector
import io.github.manamiproject.manami.gui.extensions.capitalize
import io.github.manamiproject.manami.gui.theme.ManamiTheme
import io.github.manamiproject.modb.core.anime.*
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.eitherNullOrBlank

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FindByCriteria(viewModel: FindByCriteriaViewModel = FindByCriteriaViewModel.instance) {
    val scrollState = rememberSaveable(ScrollState.Saver) { ScrollState(0) }

    var metaDataProviderSelectExpanded by remember { mutableStateOf(false) }
    val metaDataProviders = viewModel.metaDataProviders.collectAsState()
    val metaDataProviderSelectState = remember {
        TextFieldState(viewModel.metaDataProviderText)
    }

    val allTypes = AnimeType.entries.map { it.toString() }
    val selectedTypes = remember { mutableStateListOf<String>() }

    val allStatus = AnimeStatus.entries.map { it.toString().capitalize() }
    val selectedStatus = remember { mutableStateListOf<String>() }

    val allSeasons = AnimeSeason.Season.entries.map { it.toString().capitalize() }
    val selectedSeasons = remember { mutableStateListOf<String>() }

    val selectedMinEpisodes = remember { mutableStateOf(EMPTY) }
    val selectedMaxEpisodes = remember { mutableStateOf(EMPTY) }

    val selectedMinYear = remember { mutableStateOf(EMPTY) }
    val selectedMaxYear = remember { mutableStateOf(EMPTY) }

    val selectedMinDuration = remember { mutableStateOf(EMPTY) }
    val selectedMaxDuration = remember { mutableStateOf(EMPTY) }
    var durationUnitSelectExpanded by remember { mutableStateOf(false) }
    val durationUnits = Duration.TimeUnit.entries.map { it.toString().capitalize() }
    val durationUnitSelectState = rememberTextFieldState(initialText = durationUnits.first())

    val selectedMinScore = remember { mutableStateOf(EMPTY) }
    val selectedMaxScore = remember { mutableStateOf(EMPTY) }
    var scoreTypeSelectExpanded by remember { mutableStateOf(false) }
    val scoreTypes = FindByCriteriaConfig.ScoreType.entries.map { it.viewName }
    val scoreTypeSelectState = rememberTextFieldState(initialText = scoreTypes.first())

    val allStudios = viewModel.availableStudios
    val selectedStudios = remember { mutableStateListOf<Studio>() }
    val selectedSearchConjunctionStudios = remember { mutableStateOf(AND) }

    val allProducers = viewModel.availableProducers
    val selectedProducers = remember { mutableStateListOf<Producer>() }
    val selectedSearchConjunctionProducers = remember { mutableStateOf(AND) }

    val allTags = viewModel.availableTags
    val selectedTags = remember { mutableStateListOf<Tag>() }
    val selectedSearchConjunctionTags = remember { mutableStateOf(AND) }

    LaunchedEffect(metaDataProviders.value) {
        if (metaDataProviders.value.isNotEmpty() && viewModel.metaDataProviderText.eitherNullOrBlank()) {
            metaDataProviderSelectState.setTextAndPlaceCursorAtEnd(metaDataProviders.value.first())
            viewModel.metaDataProviderText = metaDataProviders.value.first()
        }
    }

    LaunchedEffect(Unit) {
        scrollState.scrollTo(viewModel.scrollOffset)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.saveScrollPosition(scrollState.value)
        }
    }

    ManamiTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(scrollState), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                Text("MetaDataProvider", style = MaterialTheme.typography.headlineMedium)
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
                                    viewModel.metaDataProviderText = option
                                    metaDataProviderSelectExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text("Types", style = MaterialTheme.typography.headlineMedium)
                DualPaneMultiSelect(allTypes, selectedTypes)

                Spacer(Modifier.height(16.dp))

                Text("Status", style = MaterialTheme.typography.headlineMedium)
                DualPaneMultiSelect(allStatus, selectedStatus)

                Text("Season", style = MaterialTheme.typography.headlineMedium)
                DualPaneMultiSelect(allSeasons, selectedSeasons)

                Spacer(Modifier.height(16.dp))

                Text("Year", style = MaterialTheme.typography.headlineMedium)
                RangeSelector(selectedMinYear, selectedMaxYear)

                Spacer(Modifier.height(16.dp))

                Text("Episodes", style = MaterialTheme.typography.headlineMedium)
                RangeSelector(selectedMinEpisodes, selectedMaxEpisodes)

                Spacer(Modifier.height(16.dp))

                Text("Duration", style = MaterialTheme.typography.headlineMedium)
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        OutlinedTextField(
                            value = selectedMinDuration.value,
                            onValueChange = { newValue ->
                                when {
                                    newValue.toIntOrNull() != null -> selectedMinDuration.value = newValue
                                    newValue == EMPTY -> selectedMinDuration.value = EMPTY
                                    else -> selectedMinDuration.value = EMPTY
                                }
                            },
                            label = { Text("Start") },
                            modifier = Modifier.width(100.dp)
                        )
                        OutlinedTextField(
                            value = selectedMaxDuration.value,
                            onValueChange = { newValue ->
                                when {
                                    newValue.toIntOrNull() != null -> selectedMaxDuration.value = newValue
                                    newValue == EMPTY -> selectedMaxDuration.value = EMPTY
                                    else -> selectedMaxDuration.value = EMPTY
                                }
                            },
                            label = { Text("End") },
                            modifier = Modifier.width(100.dp)
                        )
                        ExposedDropdownMenuBox(
                            expanded = durationUnitSelectExpanded,
                            onExpandedChange = { durationUnitSelectExpanded = it },
                            modifier = Modifier.padding(0.dp, 0.dp,5.dp, 0.dp)
                        ) {
                            TextField(
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                                state = durationUnitSelectState,
                                readOnly = true,
                                lineLimits = TextFieldLineLimits.SingleLine,
                                label = { Text("Unit") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = durationUnitSelectExpanded) },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            )
                            ExposedDropdownMenu(expanded = durationUnitSelectExpanded, onDismissRequest = { durationUnitSelectExpanded = false }) {
                                durationUnits.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
                                        onClick = {
                                            durationUnitSelectState.setTextAndPlaceCursorAtEnd(option)
                                            durationUnitSelectExpanded = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text("Score", style = MaterialTheme.typography.headlineMedium)
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        OutlinedTextField(
                            value = selectedMinScore.value,
                            onValueChange = { newValue ->
                                when {
                                    newValue.toIntOrNull() != null -> selectedMinScore.value = newValue
                                    newValue == EMPTY -> selectedMinScore.value = EMPTY
                                    else -> selectedMinScore.value = EMPTY
                                }
                            },
                            label = { Text("Start") },
                            modifier = Modifier.width(100.dp)
                        )
                        OutlinedTextField(
                            value = selectedMaxScore.value,
                            onValueChange = { newValue ->
                                when {
                                    newValue.toIntOrNull() != null -> selectedMaxScore.value = newValue
                                    newValue == EMPTY -> selectedMaxScore.value = EMPTY
                                    else -> selectedMaxScore.value = EMPTY
                                }
                            },
                            label = { Text("End") },
                            modifier = Modifier.width(100.dp)
                        )
                        ExposedDropdownMenuBox(
                            expanded = scoreTypeSelectExpanded,
                            onExpandedChange = { scoreTypeSelectExpanded = it },
                            modifier = Modifier.padding(0.dp, 0.dp,5.dp, 0.dp)
                        ) {
                            TextField(
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                                state = scoreTypeSelectState,
                                readOnly = true,
                                lineLimits = TextFieldLineLimits.SingleLine,
                                label = { Text("Unit") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = scoreTypeSelectExpanded) },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            )
                            ExposedDropdownMenu(expanded = scoreTypeSelectExpanded, onDismissRequest = { scoreTypeSelectExpanded = false }) {
                                scoreTypes.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
                                        onClick = {
                                            scoreTypeSelectState.setTextAndPlaceCursorAtEnd(option)
                                            scoreTypeSelectExpanded = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text("Studios", style = MaterialTheme.typography.headlineMedium)
                DualPaneMultiSelect(allStudios, selectedStudios, selectedSearchConjunctionStudios)

                Spacer(Modifier.height(16.dp))

                Text("Producers", style = MaterialTheme.typography.headlineMedium)
                DualPaneMultiSelect(allProducers, selectedProducers, selectedSearchConjunctionProducers)

                Spacer(Modifier.height(16.dp))

                Text("Tags", style = MaterialTheme.typography.headlineMedium)
                DualPaneMultiSelect(allTags, selectedTags, selectedSearchConjunctionTags)

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.search(
                        metaDataProvider = metaDataProviderSelectState.text.toString(),
                        types = selectedTypes.toSet(),
                        status = selectedStatus.toSet(),
                        seasons = selectedSeasons.toSet(),
                        episodesMin = selectedMaxEpisodes.value,
                        episodesMax = selectedMaxEpisodes.value,
                        yearMin = selectedMinYear.value,
                        yearMax = selectedMaxYear.value,
                        durationMin = selectedMinDuration.value,
                        durationMax = selectedMaxDuration.value,
                        durationUnit = durationUnitSelectState.text.toString(),
                        scoreMin = selectedMinScore.value,
                        scoreMax = selectedMaxScore.value,
                        scoreType = scoreTypeSelectState.text.toString(),
                        studios = selectedStudios.toSet(),
                        studiosConjunction = selectedSearchConjunctionStudios.value,
                        producers = selectedProducers.toSet(),
                        producersConjunction = selectedSearchConjunctionProducers.value,
                        tags = selectedTags.toSet(),
                        tagsConjunction = selectedSearchConjunctionTags.value,
                    )},
                ) {
                    Text("Search")
                }
            }

            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(scrollState),
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
    }
}
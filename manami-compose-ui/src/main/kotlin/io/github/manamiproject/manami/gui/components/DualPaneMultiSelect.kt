package io.github.manamiproject.manami.gui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.manamiproject.manami.app.search.FindByCriteriaConfig
import io.github.manamiproject.manami.app.search.FindByCriteriaConfig.SearchConjunction.AND
import io.github.manamiproject.manami.app.search.FindByCriteriaConfig.SearchConjunction.OR
import io.github.manamiproject.manami.gui.theme.ManamiTheme
import io.github.manamiproject.modb.core.extensions.EMPTY

@Composable
fun DualPaneMultiSelect(
    allOptions: List<String>,
    selectedOptions: MutableList<String>,
    selectedSearchConjunction: MutableState<FindByCriteriaConfig.SearchConjunction>? = null,
    height: Dp = 300.dp,
) {
    var searchText by remember { mutableStateOf(EMPTY) }
    val filtered by remember(searchText, allOptions) {
        mutableStateOf(allOptions.filter { it.contains(searchText, ignoreCase = true) })
    }

    ManamiTheme {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(height)) {
            Column(modifier = Modifier.weight(10f).padding(end = 8.dp)) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Lookup") },
                    modifier = Modifier.fillMaxWidth()
                )

                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(filtered, key = { it }) { option ->
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Checkbox(
                                checked = option in selectedOptions,
                                onCheckedChange = { checked ->
                                    if (checked) selectedOptions.add(option) else selectedOptions.remove(option)
                                }
                            )
                            Text(option, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }

            if (selectedSearchConjunction != null) {
                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Button(onClick = { selectedSearchConjunction.value = if (selectedSearchConjunction.value == AND) OR else AND }) {
                        Text(selectedSearchConjunction.value.toString())
                    }
                }
            }

            Column(modifier = Modifier.weight(10f).padding(start = 8.dp)) {
                Text("Selected:", style = MaterialTheme.typography.headlineSmall)
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(selectedOptions, key = { it }) { option ->
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("• $option")
                            IconButton(icon = Icons.Default.Delete, description = "Remove", onClick = { selectedOptions.remove(option) })
                        }
                    }
                }
            }
        }
    }
}
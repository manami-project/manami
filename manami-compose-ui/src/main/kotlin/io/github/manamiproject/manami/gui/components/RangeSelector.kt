package io.github.manamiproject.manami.gui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.manamiproject.manami.gui.theme.ManamiTheme
import io.github.manamiproject.modb.core.extensions.EMPTY

@Composable
fun RangeSelector(selectedMin: MutableState<String>, selectedMax: MutableState<String>) {
    ManamiTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = selectedMin.value,
                    onValueChange = { newValue ->
                        when {
                            newValue.toIntOrNull() != null -> selectedMin.value = newValue
                            newValue == EMPTY -> selectedMin.value = EMPTY
                            else -> selectedMin.value = EMPTY
                        }
                    },
                    label = { Text("Start") },
                    modifier = Modifier.width(100.dp)
                )
                OutlinedTextField(
                    value = selectedMax.value,
                    onValueChange = { newValue ->
                        when {
                            newValue.toIntOrNull() != null -> selectedMax.value = newValue
                            newValue == EMPTY -> selectedMax.value = EMPTY
                            else -> selectedMax.value = EMPTY
                        }
                    },
                    label = { Text("End") },
                    modifier = Modifier.width(100.dp)
                )
            }
        }
    }
}
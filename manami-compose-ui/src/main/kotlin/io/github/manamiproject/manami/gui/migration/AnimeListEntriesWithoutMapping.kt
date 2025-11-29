package io.github.manamiproject.manami.gui.migration

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.dp
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.lists.NoLink
import io.github.manamiproject.manami.gui.cache.ImageCache
import io.github.manamiproject.manami.gui.extensions.toOnClick
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun AnimeListEntriesWithoutMapping(entry: MigrationSelectionEntry, viewModel: MigrationViewModel = MigrationViewModel.instance) {
    val manualSelections by viewModel.manualSelections.collectAsState()
    val defaultBitmap = ImageCache.instance.fetchDefaultImage()
    val imageBitmap by produceState(initialValue = defaultBitmap, key1 = entry.animeEntry.thumbnail) {
        val fetched = ImageCache.instance.fetch(entry.animeEntry.thumbnail)
        value = (fetched as PresentValue).value
    }
    val imageSize = 200.dp

    ManamiTheme {
        Column(modifier = Modifier.padding(0.dp, 0.dp, 20.dp, 0.dp).height(IntrinsicSize.Min)) {
            Text(
                text = entry.animeEntry.title,
                modifier = Modifier.clickable(onClick = entry.animeEntry.link.asLink().uri.toOnClick()),
                style = MaterialTheme.typography.headlineSmall,
            )
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Image(
                        painter = BitmapPainter(imageBitmap),
                        contentDescription = null,
                        modifier = Modifier.width(imageSize),
                    )
                }
                Column {
                    val selectedLink = manualSelections[entry.animeEntry]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedLink != null,
                            onClick = {
                                when (selectedLink != null) {
                                    true -> viewModel.removeMapping(entry.animeEntry)
                                    false ->viewModel.selectMapping(entry.animeEntry, NoLink)
                                }
                            }
                        )
                        Text("Delete")
                    }
                }
            }
        }
    }
}
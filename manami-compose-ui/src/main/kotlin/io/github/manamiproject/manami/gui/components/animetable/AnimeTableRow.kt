package io.github.manamiproject.manami.gui.components.animetable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotInterested
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.lists.AnimeEntry
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.gui.cache.ImageCache
import io.github.manamiproject.manami.gui.components.IconButton
import io.github.manamiproject.manami.gui.theme.ManamiTheme
import io.github.manamiproject.manami.gui.theme.ThemeState
import java.awt.Desktop

@Composable
internal fun <T: AnimeEntry> AnimeTableRow(
    config: AnimeTableConfig.() -> Unit = {},
    anime: T,
    viewModel: AnimeTableViewModel<T>,
) {
    val animeTableConfig = AnimeTableConfig().apply(config)
    val backgroundColor = ThemeState.instance.currentScheme.surface
    val iconSize = 40.dp
    val padding = 8.dp
    val onClick: () -> Unit = {
        if (anime.link is Link) {
            try {
                Desktop.getDesktop().browse(anime.link.asLink().uri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var imageBitmap by remember { mutableStateOf<ImageBitmap>(ImageCache.instance.fetchDefaultImage()) }
    LaunchedEffect(anime.thumbnail) {
        imageBitmap = (ImageCache.instance.fetch(anime.thumbnail) as PresentValue).value
    }

    ManamiTheme {
        Row(Modifier.fillMaxSize().background(backgroundColor).height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier.weight(animeTableConfig.weights[0])
                    .padding(padding)
                    .clickable(onClick = onClick)
                    .fillMaxHeight(),
                contentAlignment = Center,
            ) {
                Image(
                    painter = BitmapPainter(imageBitmap),
                    contentDescription = null,
                )
            }

            Box(
                modifier = Modifier.weight(animeTableConfig.weights[1])
                    .background(backgroundColor)
                    .fillMaxHeight()
                    .padding(padding)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = anime.title,
                    style = TextStyle.Default.copy(
                        color = ThemeState.instance.currentScheme.onSurface,
                        fontSize = TextUnit(24f, TextUnitType.Sp),
                    ),
                )
            }

            Box(
                modifier = Modifier.weight(animeTableConfig.weights[2])
                    .fillMaxHeight()
                    .padding(padding),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row {
                    if (animeTableConfig.withShowAnimeDetailsButton) {
                        IconButton(
                            icon = Icons.AutoMirrored.Filled.List,
                            size = iconSize,
                            description = "Show anime details",
                            onClick = { viewModel.showAnimeDetails(anime.link) },
                        )
                    }

                    if (animeTableConfig.withToWatchListButton) {
                        IconButton(
                            icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                            size = iconSize,
                            description = "Add to watch list",
                            onClick = { viewModel.addToWatchList(anime) },
                        )
                    }

                    if (animeTableConfig.withToIgnoreListButton) {
                        IconButton(
                            icon = Icons.Filled.NotInterested,
                            size = iconSize,
                            description = "Add to ignore list",
                            onClick = { viewModel.addToIgnoreList(anime) },
                        )
                    }

                    if (animeTableConfig.withEditButton) {
                        IconButton(
                            icon = Icons.Filled.Edit,
                            size = iconSize,
                            description = "Edit",
                            onClick = { TODO() },
                        )
                    }

                    if (animeTableConfig.withDeleteButton) {
                        IconButton(
                            icon = Icons.Filled.Delete,
                            size = iconSize,
                            description = "Delete",
                            onClick = { viewModel.delete(anime) },
                        )
                    }

                    if (animeTableConfig.withHideButton) {
                        IconButton(
                            icon = Icons.Filled.Clear,
                            size = iconSize,
                            description = "Hide",
                            onClick = { viewModel.hide(anime) },
                        )
                    }
                }
            }
        }
    }
}
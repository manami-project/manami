package io.github.manamiproject.manami.gui.components.animetable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
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
import io.github.manamiproject.manami.gui.extensions.toOnClick
import io.github.manamiproject.manami.gui.theme.ManamiTheme
import io.github.manamiproject.manami.gui.theme.ThemeState

@Composable
internal fun <T: AnimeEntry> AnimeTableRow(
    config: AnimeTableConfig.() -> Unit = {},
    anime: T,
    viewModel: AnimeTableViewModel<T>,
) {
    val animeTableConfig = AnimeTableConfig().apply(config)
    val backgroundColor = ThemeState.instance.currentScheme.value.background
    val iconSize = 40.dp
    val padding = 8.dp
    val openLinkAction: () -> Unit = if (anime.link is Link) { anime.link.asLink().uri.toOnClick() } else { {} }

    val defaultBitmap = ImageCache.instance.fetchDefaultImage()
    val imageBitmap by produceState(initialValue = defaultBitmap, key1 = anime.thumbnail) {
        val fetched = ImageCache.instance.fetch(anime.thumbnail)
        value = (fetched as PresentValue).value
    }
    val imageSize = 200.dp

    ManamiTheme {
        Row(Modifier.fillMaxSize().background(backgroundColor).height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier.weight(animeTableConfig.weights[0])
                    .padding(padding)
                    .clickable(onClick = openLinkAction)
                    .fillMaxHeight()
                    .size(imageSize),
                contentAlignment = Center,
            ) {
                Image(
                    painter = BitmapPainter(imageBitmap),
                    contentDescription = null,
                    modifier = Modifier.width(imageSize),
                )
            }

            Box(
                modifier = Modifier.weight(animeTableConfig.weights[1])
                    .background(backgroundColor)
                    .fillMaxHeight()
                    .padding(padding)
                    .clickable(onClick = openLinkAction),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = anime.title,
                    style = TextStyle.Default.copy(
                        color = ThemeState.instance.currentScheme.value.onBackground,
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
                    if (animeTableConfig.withOpenDirectoryButton) {
                        IconButton(
                            icon = Icons.Filled.FolderOpen,
                            size = iconSize,
                            description = "Open directory",
                            onClick = { viewModel.openDirectory(anime) },
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

                    if (animeTableConfig.withShowAnimeDetailsButton) {
                        IconButton(
                            icon = Icons.Filled.Info,
                            size = iconSize,
                            description = "Show anime details",
                            onClick = { viewModel.showAnimeDetails(anime.link) },
                        )
                    }

                    if (animeTableConfig.withFindRelatedAnimeButton) {
                        IconButton(
                            icon = Icons.Filled.Polyline,
                            size = iconSize,
                            description = "Find related anime",
                            onClick = { viewModel.findRelatedAnime(anime.link) },
                        )
                    }

                    if (animeTableConfig.withFindSimilarAnimeButton) {
                        IconButton(
                            icon = Icons.AutoMirrored.Filled.ManageSearch,
                            size = iconSize,
                            description = "Find similar anime",
                            onClick = { viewModel.findSimilarAnime(anime.link) },
                        )
                    }

                    if (animeTableConfig.withToAnimeListButton) {
                        IconButton(
                            icon = Icons.Filled.CheckCircleOutline,
                            size = iconSize,
                            description = "Add to anime list",
                            onClick = { viewModel.addToAnimeList(anime) },
                        )
                    }

                    if (animeTableConfig.withToWatchListButton) {
                        IconButton(
                            icon = Icons.Filled.Visibility,
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

                    if (animeTableConfig.withDeleteButton) {
                        IconButton(
                            icon = Icons.Filled.DeleteOutline,
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
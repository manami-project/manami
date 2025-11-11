package io.github.manamiproject.manami.gui.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.manamiproject.manami.gui.components.IconButton
import io.github.manamiproject.manami.gui.dashboard.Dashboard
import io.github.manamiproject.manami.gui.find.anime.FindAnime
import io.github.manamiproject.manami.gui.find.animedetails.FindAnimeDetails
import io.github.manamiproject.manami.gui.find.bytitle.FindByTitle
import io.github.manamiproject.manami.gui.find.inconsistencies.FindInconsistencies
import io.github.manamiproject.manami.gui.find.relatedanime.FindRelatedAnime
import io.github.manamiproject.manami.gui.find.season.FindSeason
import io.github.manamiproject.manami.gui.find.similaranime.FindSimilarAnime
import io.github.manamiproject.manami.gui.lists.animelist.AddAnimeToWatchListForm
import io.github.manamiproject.manami.gui.lists.animelist.AnimeList
import io.github.manamiproject.manami.gui.lists.ignorelist.IgnoreList
import io.github.manamiproject.manami.gui.lists.watchlist.WatchList
import io.github.manamiproject.manami.gui.tabs.Tabs.*
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun TabBar(
    viewModel: TabBarViewModel = TabBarViewModel.instance,
) {
    ManamiTheme {
        Column {
            PrimaryTabRow(
                selectedTabIndex = viewModel.openTabs.indexOf(viewModel.activeTab.value).coerceAtLeast(0),
                modifier = Modifier.fillMaxWidth().height(45.dp),
            ) {
                viewModel.openTabs.forEach { tab ->
                    Tab(
                        selected = tab == viewModel.activeTab.value,
                        onClick = { viewModel.openOrActivate(tab) },
                        text = {
                            Row(
                                verticalAlignment = CenterVertically,
                            ) {
                                Text(tab.title)
                                if (tab.isCloseable) {
                                    IconButton(
                                        icon = Icons.Filled.Clear,
                                        description = "Close",
                                        onClick = { viewModel.closeTab(tab) },
                                    )
                                }
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            when (viewModel.activeTab.value) {
                DASHBOARD -> Dashboard()
                ANIME_LIST -> AnimeList()
                WATCH_LIST -> WatchList()
                IGNORE_LIST -> IgnoreList()
                FIND_ANIME -> FindAnime()
                FIND_SEASON -> FindSeason()
                FIND_INCONSISTENCIES -> FindInconsistencies()
                FIND_RELATED_ANIME -> FindRelatedAnime()
                FIND_SIMILAR_ANIME -> FindSimilarAnime()
                FIND_ANIME_DETAILS -> FindAnimeDetails()
                FIND_BY_TITLE -> FindByTitle()
                ADD_ANIME_TO_ANIME_LIST_FORM -> AddAnimeToWatchListForm()
            }
        }
    }
}
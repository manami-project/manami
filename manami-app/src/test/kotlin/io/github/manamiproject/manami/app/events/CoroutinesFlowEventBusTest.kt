package io.github.manamiproject.manami.app.events

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.modb.core.anime.AnimeType.TV
import kotlinx.coroutines.flow.update
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.io.path.Path
import kotlin.test.AfterTest
import kotlin.test.Test

internal class CoroutinesFlowEventBusTest {

    @AfterTest
    fun afterTest() {
        CoroutinesFlowEventBus.clear()
    }

    @Nested
    inner class ClearTests {
        
        @Test
        fun `clear all states`() {
            // given
            val animeListEntry = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/57"),
                title = "Beck",
                episodes = 26,
                type = TV,
                location = Path("some/relative/path/beck"),
            )
            CoroutinesFlowEventBus.dashboardState.update { current -> current.copy(isAnimeCachePopulatorRunning = true) }
            CoroutinesFlowEventBus.generalAppState.update { current -> current.copy(openedFile = "test.json") }
            CoroutinesFlowEventBus.animeListState.update { current -> current.copy(entries = listOf(animeListEntry)) }
            CoroutinesFlowEventBus.watchListState.update { current -> current.copy(isAdditionRunning = true) }
            CoroutinesFlowEventBus.ignoreListState.update { current -> current.copy(isAdditionRunning = true) }
            CoroutinesFlowEventBus.inconsistenciesState.update { current -> current.copy(isRunning = true) }
            CoroutinesFlowEventBus.metaDataProviderMigrationState.update { current -> current.copy(isRunning = true) }
            CoroutinesFlowEventBus.findRelatedAnimeState.update { current -> current.copy(isForAnimeListRunning = true) }
            CoroutinesFlowEventBus.findInListState.update { current -> current.copy(isRunning = true) }
            CoroutinesFlowEventBus.findSeasonState.update { current -> current.copy(isRunning = true) }
            CoroutinesFlowEventBus.findByTagState.update { current -> current.copy(isRunning = true) }
            CoroutinesFlowEventBus.findSimilarAnimeState.update { current -> current.copy(isRunning = true) }
            CoroutinesFlowEventBus.findAnimeState.update { current -> current.copy(isRunning = true) }
            
            // when
            CoroutinesFlowEventBus.clear()
            
            // then
            assertThat(CoroutinesFlowEventBus.dashboardState.value).isEqualTo(DashboardState())
            assertThat(CoroutinesFlowEventBus.generalAppState.value).isEqualTo(GeneralAppState())
            assertThat(CoroutinesFlowEventBus.animeListState.value).isEqualTo(AnimeListState())
            assertThat(CoroutinesFlowEventBus.watchListState.value).isEqualTo(WatchListState())
            assertThat(CoroutinesFlowEventBus.ignoreListState.value).isEqualTo(IgnoreListState())
            assertThat(CoroutinesFlowEventBus.inconsistenciesState.value).isEqualTo(InconsistenciesState())
            assertThat(CoroutinesFlowEventBus.metaDataProviderMigrationState.value).isEqualTo(MetaDataProviderMigrationState())
            assertThat(CoroutinesFlowEventBus.findRelatedAnimeState.value).isEqualTo(RelatedAnimeState())
            assertThat(CoroutinesFlowEventBus.findInListState.value).isEqualTo(FindInListState())
            assertThat(CoroutinesFlowEventBus.findSeasonState.value).isEqualTo(FindSeasonState())
            assertThat(CoroutinesFlowEventBus.findByTagState.value).isEqualTo(FindByTagState())
            assertThat(CoroutinesFlowEventBus.findSimilarAnimeState.value).isEqualTo(FindSimilarAnimeState())
            assertThat(CoroutinesFlowEventBus.findAnimeState.value).isEqualTo(FindAnimeState())
        }
    }
}
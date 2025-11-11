package io.github.manamiproject.manami.gui.lists.animelist

import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.LinkEntry
import io.github.manamiproject.manami.app.lists.NoLink
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.gui.tabs.TabBarViewModel
import io.github.manamiproject.manami.gui.tabs.Tabs.ADD_ANIME_TO_ANIME_LIST_FORM
import io.github.manamiproject.modb.core.anime.AnimeType
import io.github.manamiproject.modb.core.extensions.directoryExists
import io.github.manamiproject.modb.core.extensions.eitherNullOrBlank
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.URI
import kotlin.io.path.Path

internal class AddAnimeToAnimeListFormViewModel(
    private val app: Manami = Manami.instance,
    private val tabBarViewModel: TabBarViewModel = TabBarViewModel.instance,
) {

    private val viewModelScope = CoroutineScope(Default + SupervisorJob())

    val isAddAnimeEntryDataRunning = app.animeListModificationState
        .map { it.isAddAnimeEntryDataRunning }
        .stateIn(
            scope = viewModelScope,
            started = Eagerly,
            initialValue = false,
        )

    val animeDetails = app.animeListModificationState
        .map { it.addAnimeEntryData }
        .stateIn(
            scope = viewModelScope,
            started = Eagerly,
            initialValue = null,
        )

    fun fetchAnimeDetails(link: LinkEntry) {
        if (link is NoLink) return

        viewModelScope.launch {
            app.findAnimeDetailsForAddingAnEntry(link.asLink().uri)
        }
    }

    fun addToAnimeList(
        link: String,
        title: String,
        episodes: Int,
        type: String,
        thumbnail: String,
        location: String,
    ) {
        if (link.eitherNullOrBlank()) return
        if (title.eitherNullOrBlank()) return
        if (episodes < 0) return
        if (type.eitherNullOrBlank()) return
        if (thumbnail.eitherNullOrBlank()) return
        if (location.eitherNullOrBlank()) return

        val checkedThumbnail = try {
            val uri = URI(thumbnail)
            uri.toURL()
            uri
        } catch (_: Exception) {
            return
        }

        val checkedUri = try {
            val uri = URI(link)
            uri.toURL()
            uri
        } catch (_: Exception) {
            return
        }

        val checkedLocation = Path(location)

        if (!checkedLocation.directoryExists()) return

        if (!AnimeType.entries.map { it.toString() }.contains(type.uppercase())) return
        val checkedType = AnimeType.of(type)

        app.addAnimeListEntry(
            AnimeListEntry(
                link = Link(checkedUri),
                title = title,
                thumbnail = checkedThumbnail,
                episodes = episodes,
                type = checkedType,
                location = checkedLocation,
            )
        )

        tabBarViewModel.closeTab(ADD_ANIME_TO_ANIME_LIST_FORM)
    }

    internal companion object {
        /**
         * Singleton of [AddAnimeToAnimeListFormViewModel]
         * @since 4.0.0
         */
        val instance: AddAnimeToAnimeListFormViewModel by lazy { AddAnimeToAnimeListFormViewModel() }
    }
}
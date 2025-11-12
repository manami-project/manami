package io.github.manamiproject.manami.gui.lists.animelist

import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.gui.tabs.TabBarViewModel
import io.github.manamiproject.manami.gui.tabs.Tabs.EDIT_ANIME_LIST_ENTRY_FORM
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

internal class EditAnimeListEntryFormViewModel(
    private val app: Manami = Manami.instance,
    private val tabBarViewModel: TabBarViewModel = TabBarViewModel.instance,
) {

    private val viewModelScope = CoroutineScope(Default + SupervisorJob())

    val animeListEntry = app.animeListModificationState
        .map { it.editAnimeListEntryData }
        .stateIn(
            scope = viewModelScope,
            started = Eagerly,
            initialValue = null,
        )

    fun editEntry(animeListEntry: AnimeListEntry) {
        viewModelScope.launch {
            app.prepareAnimeListEntryForEditingAnEntry(animeListEntry)
        }
    }

    fun update(
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

        app.replaceAnimeListEntry(
            current = animeListEntry.value!!,
            replacement = AnimeListEntry(
                link = Link(checkedUri),
                title = title,
                thumbnail = checkedThumbnail,
                episodes = episodes,
                type = checkedType,
                location = checkedLocation,
            )
        )

        tabBarViewModel.closeTab(EDIT_ANIME_LIST_ENTRY_FORM)
    }

    internal companion object {
        /**
         * Singleton of [EditAnimeListEntryFormViewModel]
         * @since 4.0.0
         */
        val instance: EditAnimeListEntryFormViewModel by lazy { EditAnimeListEntryFormViewModel() }
    }
}
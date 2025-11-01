package io.github.manamiproject.manami.gui.components.animetable

internal data class AnimeTableConfig(
    var withToWatchListButton: Boolean = true,
    var withToIgnoreListButton: Boolean = true,
    var withHideButton: Boolean = true,
    var withDeleteButton: Boolean = false,
    var withSortableTitle: Boolean = true,
    var withEditButton: Boolean = false,
)

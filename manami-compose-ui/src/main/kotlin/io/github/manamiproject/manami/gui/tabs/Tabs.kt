package io.github.manamiproject.manami.gui.tabs

internal enum class Tabs(
    val title: String,
    val isCloseable: Boolean = true,
) {
    DASHBOARD(title = "Dashboard", isCloseable = false),
    ANIME_LIST(title = "Anime List"),
    WATCH_LIST(title = "Watch List"),
    IGNORE_LIST(title = "Ignore List"),
    FIND_ANIME(title = "Find Anime"),
    FIND_INCONSISTENCIES(title = "Find Inconsistencies"),
    FIND_RELATED_ANIME(title = "Find Related Anime"),
    FIND_SEASON(title = "Find Season"),
    FIND_SIMILAR_ANIME(title = "Find Similar Anime"),
    FIND_ANIME_DETAILS(title = "Anime Details"),
}
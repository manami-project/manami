package io.github.manamiproject.manami.app.events

import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry

/**
 * @since 4.0.0
 */
data class IgnoreListState(
    val isAdditionRunning: Boolean = false,
    val entries: Collection<IgnoreListEntry> = emptyList(),
)

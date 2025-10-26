package io.github.manamiproject.manami.app.events

import io.github.manamiproject.manami.app.versioning.SemanticVersion
import io.github.manamiproject.modb.core.config.Hostname

/**
 * @since 4.0.0
 */
data class DashboardState(
    val entries: Map<Hostname, Int> = emptyMap(),
    val newVersion: SemanticVersion? = null,
    val isAnimeCachePopulatorRunning: Boolean = false,
)
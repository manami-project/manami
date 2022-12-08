package io.github.manamiproject.manami.app.inconsistencies.animelist.episodes

import io.github.manamiproject.manami.app.events.Event

data class AnimeListEpisodesInconsistenciesResultEvent(
    val entries: List<EpisodeDiff>,
) : Event
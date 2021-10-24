package io.github.manamiproject.manami.app.inconsistencies.animelist.metadata

import io.github.manamiproject.manami.app.state.events.Event

data class AnimeListMetaDataInconsistenciesResultEvent(
    val diff: AnimeListMetaDataDiff
) : Event

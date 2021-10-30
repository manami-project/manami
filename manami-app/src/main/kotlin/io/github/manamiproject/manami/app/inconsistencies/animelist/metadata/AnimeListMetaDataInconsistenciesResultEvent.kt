package io.github.manamiproject.manami.app.inconsistencies.animelist.metadata

import io.github.manamiproject.manami.app.events.Event

data class AnimeListMetaDataInconsistenciesResultEvent(
    val diff: AnimeListMetaDataDiff
) : Event

package io.github.manamiproject.manami.app.search

import io.github.manamiproject.manami.app.state.events.Event
import io.github.manamiproject.modb.core.models.Anime

data class AnimeSeasonEntryFoundEvent(val anime: Anime): Event
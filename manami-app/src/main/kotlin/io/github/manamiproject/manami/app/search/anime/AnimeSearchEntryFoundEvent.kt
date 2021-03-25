package io.github.manamiproject.manami.app.search.anime

import io.github.manamiproject.manami.app.state.events.Event
import io.github.manamiproject.modb.core.models.Anime

data class AnimeSearchEntryFoundEvent(val anime: Anime): Event

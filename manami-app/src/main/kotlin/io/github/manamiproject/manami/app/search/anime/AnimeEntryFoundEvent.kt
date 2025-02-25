package io.github.manamiproject.manami.app.search.anime

import io.github.manamiproject.manami.app.events.Event
import io.github.manamiproject.modb.core.anime.Anime

data class AnimeEntryFoundEvent(val anime: Anime): Event
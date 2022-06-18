package io.github.manamiproject.manami.app.search.similaranime

import io.github.manamiproject.manami.app.events.Event
import io.github.manamiproject.modb.core.models.Anime

data class SimilarAnimeFoundEvent(val entries: List<Anime>) : Event
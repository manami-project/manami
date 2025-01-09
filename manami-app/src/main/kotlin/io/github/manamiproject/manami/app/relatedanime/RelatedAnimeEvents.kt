package io.github.manamiproject.manami.app.relatedanime

import io.github.manamiproject.manami.app.events.Event
import io.github.manamiproject.manami.app.events.EventListType
import io.github.manamiproject.modb.core.models.Anime

data class RelatedAnimeFinishedEvent(val listType: EventListType, val resultList: List<Anime>): Event

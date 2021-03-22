package io.github.manamiproject.manami.app.relatedanime

import io.github.manamiproject.manami.app.state.events.Event
import io.github.manamiproject.manami.app.state.events.EventListType
import io.github.manamiproject.modb.core.models.Anime

data class RelatedAnimeFoundEvent(val listType: EventListType, val anime: Anime): Event
data class RelatedAnimeStatusEvent(val listType: EventListType, val finishedChecking: Int, val toBeChecked: Int): Event

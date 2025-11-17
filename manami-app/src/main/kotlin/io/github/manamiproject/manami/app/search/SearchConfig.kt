package io.github.manamiproject.manami.app.search

import io.github.manamiproject.manami.app.search.SearchConjunction.AND
import io.github.manamiproject.modb.core.anime.*
import io.github.manamiproject.modb.core.config.Hostname

data class SearchConfig(
    val metaDataProvider: Hostname,
    val type: Set<Tag> = emptySet(),
    val typeConjunction: SearchConjunction = AND,
    val episodes: IntRange = -1..-1,
    val durationInSeconds: IntRange = -1..-1,
    val year: IntRange = YEAR_OF_THE_FIRST_ANIME..-1,
    val status: Set<AnimeStatus> = AnimeStatus.entries.toSet(),
    val statusConjunction: SearchConjunction = AND,
    val studio: Set<Studio> = emptySet(),
    val studioConjunction: SearchConjunction = AND,
    val producers: Set<Producer> = emptySet(),
    val producerConjunction: SearchConjunction = AND,
    val tags: Set<Tag> = emptySet(),
    val tagConjunction: SearchConjunction = AND,
) {
    init {
        require(year.first >= YEAR_OF_THE_FIRST_ANIME) { "Invalid year range. Minimum cannot be before the year of the first anime [${YEAR_OF_THE_FIRST_ANIME}]" }
    }
}
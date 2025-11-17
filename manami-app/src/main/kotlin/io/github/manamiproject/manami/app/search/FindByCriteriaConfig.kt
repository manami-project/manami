package io.github.manamiproject.manami.app.search

import io.github.manamiproject.manami.app.search.FindByCriteriaConfig.SearchConjunction.AND
import io.github.manamiproject.modb.core.anime.*
import io.github.manamiproject.modb.core.config.Hostname

data class FindByCriteriaConfig(
    val metaDataProvider: Hostname,
    val types: Set<AnimeType> = emptySet(),
    val episodes: IntRange = -1..-1,
    val durationInSeconds: IntRange = -1..-1,
    val year: IntRange = YEAR_OF_THE_FIRST_ANIME..-1,
    val seasons: Set<AnimeSeason.Season> = emptySet(),
    val status: Set<AnimeStatus> = emptySet(),
    val studios: Set<Studio> = emptySet(),
    val studiosConjunction: SearchConjunction = AND,
    val producers: Set<Producer> = emptySet(),
    val producersConjunction: SearchConjunction = AND,
    val tags: Set<Tag> = emptySet(),
    val tagsConjunction: SearchConjunction = AND,
) {

    enum class SearchConjunction {
        OR,
        AND;

        companion object {
            fun of(value: String): SearchConjunction {
                return entries.find { it.toString().equals(value, ignoreCase = true) } ?: throw IllegalArgumentException("No value for [$value]")
            }
        }
    }
}
package io.github.manamiproject.manami.gui.extensions

import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.NoScore
import io.github.manamiproject.modb.core.anime.ScoreValue
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.toDuration

fun Anime.toMap(): Map<String, String> {
    val formattedScore = when(score) {
        is NoScore -> "-"
        is ScoreValue -> {
            val scoreValue = score as ScoreValue
            "AGM: ${scoreValue.arithmeticGeometricMean} | AM: ${scoreValue.arithmeticMean} | Mdn: ${scoreValue.median} | Scale: 1 - 10"
        }
    }

    return mapOf(
        "title" to title,
        "sources" to sources.sorted().joinToString(" | ").toString(),
        "type" to type.toString(),
        "episodes" to episodes.toString(),
        "status" to status.toString(),
        "season" to "${animeSeason.season} ${animeSeason.year}",
        "duration" to duration.duration.toDuration(SECONDS).toString(),
        "score" to formattedScore,
        "synonyms" to synonyms.sorted().joinToString("\n"),
        "studios" to studios.sorted().joinToString("\n"),
        "producers" to synonyms.sorted().joinToString("\n"),
        "tags" to tags.sorted().joinToString("\n"),
    )
}
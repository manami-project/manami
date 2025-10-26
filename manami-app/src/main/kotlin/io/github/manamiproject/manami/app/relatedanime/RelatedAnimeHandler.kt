package io.github.manamiproject.manami.app.relatedanime

interface RelatedAnimeHandler {

    suspend fun findRelatedAnimeForAnimeList()
    suspend fun findRelatedAnimeForIgnoreList()
}
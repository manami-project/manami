package io.github.manamiproject.manami.app.relatedanime

import java.net.URI

interface RelatedAnimeHandler {

    suspend fun findRelatedAnime(initialSources: Collection<URI>)
}
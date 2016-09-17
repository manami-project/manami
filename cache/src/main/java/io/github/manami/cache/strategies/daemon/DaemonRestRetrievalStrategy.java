package io.github.manami.cache.strategies.daemon;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Named;

import io.github.manami.cache.strategies.AnimeRetrieval;
import io.github.manami.cache.strategies.RecommendationsRetrieval;
import io.github.manami.cache.strategies.RelatedAnimeRetrieval;
import io.github.manami.dto.entities.Anime;

/**
 * External REST service
 */
@Named
public class DaemonRestRetrievalStrategy implements AnimeRetrieval, RelatedAnimeRetrieval, RecommendationsRetrieval {

    public boolean isAvailable() {
        return false;
    }


    @Override
    public Optional<Anime> fetchAnime(final String url) {
        return null;
    }


    @Override
    public Set<String> fetchRelatedAnimes(final String url) {
        return null;
    }


    @Override
    public Map<String, Integer> fetchRecommendations(final String url) {
        return null;
    }
}

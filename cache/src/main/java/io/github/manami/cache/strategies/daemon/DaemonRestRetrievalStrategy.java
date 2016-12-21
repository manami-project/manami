package io.github.manami.cache.strategies.daemon;

import io.github.manami.cache.strategies.AnimeRetrieval;
import io.github.manami.cache.strategies.RecommendationsRetrieval;
import io.github.manami.cache.strategies.RelatedAnimeRetrieval;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;

import javax.inject.Named;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * External REST service
 */
@Named
public class DaemonRestRetrievalStrategy implements AnimeRetrieval, RelatedAnimeRetrieval, RecommendationsRetrieval {

    public boolean isAvailable() {
        return false;
    }


    @Override
    public Optional<Anime> fetchAnime(final InfoLink infoLink) {
        return null;
    }


    @Override
    public Set<InfoLink> fetchRelatedAnimes(final InfoLink infoLink) {
        return null;
    }


    @Override
    public Map<InfoLink, Integer> fetchRecommendations(final InfoLink infoLink) {
        return null;
    }
}

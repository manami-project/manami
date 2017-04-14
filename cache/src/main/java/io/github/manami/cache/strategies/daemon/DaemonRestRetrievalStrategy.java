package io.github.manami.cache.strategies.daemon;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Optional;
import java.util.Set;

import javax.inject.Named;

import io.github.manami.cache.strategies.AnimeRetrieval;
import io.github.manami.cache.strategies.RecommendationsRetrieval;
import io.github.manami.cache.strategies.RelatedAnimeRetrieval;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.RecommendationList;

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
        return Optional.empty();
    }


    @Override
    public Set<InfoLink> fetchRelatedAnime(final InfoLink infoLink) {
        return newHashSet();
    }


    @Override
    public RecommendationList fetchRecommendations(final InfoLink infoLink) {
        return new RecommendationList();
    }
}

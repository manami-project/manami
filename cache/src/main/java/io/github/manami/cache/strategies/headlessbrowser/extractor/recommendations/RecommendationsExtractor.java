package io.github.manami.cache.strategies.headlessbrowser.extractor.recommendations;

import io.github.manami.cache.strategies.headlessbrowser.extractor.AnimeExtractor;
import io.github.manami.dto.entities.RecommendationList;

public interface RecommendationsExtractor extends AnimeExtractor {

    RecommendationList extractRecommendations(String siteContent);
}

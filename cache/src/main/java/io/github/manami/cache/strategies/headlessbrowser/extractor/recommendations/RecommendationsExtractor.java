package io.github.manami.cache.strategies.headlessbrowser.extractor.recommendations;

import io.github.manami.cache.strategies.headlessbrowser.extractor.AnimeExtractor;
import io.github.manami.dto.entities.InfoLink;

import java.util.Map;

public interface RecommendationsExtractor extends AnimeExtractor {

    Map<InfoLink, Integer> extractRecommendations(String siteContent);
}

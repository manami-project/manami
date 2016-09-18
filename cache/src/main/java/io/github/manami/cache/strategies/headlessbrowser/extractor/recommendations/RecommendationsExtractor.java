package io.github.manami.cache.strategies.headlessbrowser.extractor.recommendations;

import java.util.Map;

import io.github.manami.cache.strategies.headlessbrowser.extractor.AnimeExtractor;

public interface RecommendationsExtractor extends AnimeExtractor {

    Map<String, Integer> extractRecommendations(String siteContent);
}

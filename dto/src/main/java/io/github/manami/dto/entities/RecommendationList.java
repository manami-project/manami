package io.github.manami.dto.entities;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.List;
import java.util.Map;

public class RecommendationList {

    private final Map<InfoLink, Recommendation> recommendations;


    public RecommendationList() {
        recommendations = newHashMap();
    }


    public Recommendation addRecommendation(final Recommendation recommendation) {
        Recommendation value = recommendation;

        if (recommendations.containsKey(recommendation.getInfoLink())) {
            final int amountInList = recommendations.get(recommendation.getInfoLink()).getAmount();
            final int newAmount = value.getAmount();
            value = new Recommendation(recommendation.getInfoLink(), amountInList + newAmount);
        }

        return recommendations.put(recommendation.getInfoLink(), value);
    }


    public List<Recommendation> asList() {
        return newArrayList(recommendations.values());
    }


    public boolean isEmpty() {
        return recommendations.isEmpty();
    }


    public boolean isNotEmpty() {
        return !recommendations.isEmpty();
    }


    public boolean containsKey(final InfoLink infoLink) {
        return recommendations.containsKey(infoLink);
    }


    public Recommendation get(final InfoLink infoLink) {
        return recommendations.get(infoLink);
    }
}
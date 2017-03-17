package io.github.manami.dto.entities;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class RecommendationList {

    private final Map<InfoLink, Recommendation> recommendations;


    public RecommendationList() {
        recommendations = Maps.newHashMap();
    }


    public Recommendation addRecommendation(final Recommendation recommendation) {
        final Recommendation value = recommendation;

        if (recommendations.containsKey(recommendation.getInfoLink())) {
            final int amountInList = recommendations.get(recommendation.getInfoLink()).getAmount();
            final int newAmount = value.getAmount();
            value.setAmount(amountInList + newAmount);
        }

        return recommendations.put(recommendation.getInfoLink(), value);
    }


    public List<Recommendation> asList() {
        return Lists.newArrayList(recommendations.values());
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

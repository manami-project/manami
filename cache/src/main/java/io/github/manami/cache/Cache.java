package io.github.manami.cache;

import java.util.Optional;
import java.util.Set;

import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.RecommendationList;

/**
 * The cache is supposed to save raw html files from which the information can
 * be extracted at any time.
 *
 * @author manami-project
 * @since 2.0.0
 */
public interface Cache {

    Optional<Anime> fetchAnime(InfoLink infoLink);


    Set<InfoLink> fetchRelatedAnime(InfoLink infoLink);


    RecommendationList fetchRecommendations(InfoLink infoLink);
}

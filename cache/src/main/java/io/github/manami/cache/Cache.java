package io.github.manami.cache;

import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The cache is supposed to save raw html files from which the information can
 * be extracted at any time.
 *
 * @author manami-project
 * @since 2.0.0
 */
public interface Cache {

    Optional<Anime> fetchAnime(InfoLink infoLink);


    Set<InfoLink> fetchRelatedAnimes(Anime anime);


    Map<InfoLink, Integer> fetchRecommendations(Anime anime);
}

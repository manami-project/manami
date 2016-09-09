package io.github.manami.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.github.manami.cache.strategies.daemon.DaemonRestRetrievalStrategy;
import io.github.manami.cache.strategies.headlessbrowser.HeadlessBrowserRetrievalStrategy;
import io.github.manami.dto.entities.Anime;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Facade for all inquiries against a cache.
 * It orchestrates the use of concrete {@link io.github.manami.cache.Cache} implementations.
 */
@Slf4j
@Named
public final class CacheManager implements Cache {

    private DaemonRestRetrievalStrategy daemonRestRetrievalStrategy;
    private HeadlessBrowserRetrievalStrategy headlessBrowserRetrievalStrategy;

    /** Key: URL of the anime, Value: Instance of the anime including all meta data. */
    private LoadingCache<String, Optional<Anime>> animeEntryCache = null;

    /** Key: URL of the anime, Value: List of anime urls which are related to the anime url in the key */
    private LoadingCache<String, Set<String>> relatedAnimeCache = null;

    /** Key: URL of the anime, Value: List of anime urls which are recommended titles to the anime url */
    private LoadingCache<String, Map<String, Integer>> recommendationsCache = null;



    @Inject
    public CacheManager(DaemonRestRetrievalStrategy daemonRestRetrievalStrategy, HeadlessBrowserRetrievalStrategy headlessBrowserRetrievalStrategy) {
        this.daemonRestRetrievalStrategy = daemonRestRetrievalStrategy;
        this.headlessBrowserRetrievalStrategy = headlessBrowserRetrievalStrategy;
        buildAnimeCache();
        buildRelatedAnimeCache();
        buildRecommendationsCache();
    }

    /**
     * Checks whether a {@link DaemonRestRetrievalStrategy} instance is up an running.
     *
     * @return
     */
    private boolean isDaemonAvailable() {
        return daemonRestRetrievalStrategy.isAvailable();
    }

    @Override
    public Optional<Anime> fetchAnime(String url) {
        if(isBlank(url)) {
            return Optional.empty();
        }

        try {
            return animeEntryCache.get(url);
        } catch (ExecutionException e) {
            log.error("Error fetching anime entry [{url}] from cache.");
            return Optional.empty();
        }
    }

    @Override
    public Set<String> fetchRelatedAnimes(Anime anime) {
        Set<String> ret = Sets.newHashSet();

        if(anime == null || isBlank(anime.getInfoLink())) {
            return ret;
        }

        try {
            return relatedAnimeCache.get(anime.getInfoLink());
        } catch (ExecutionException e) {
            log.error("Unable to fetch related anime list from cache for [{}]", anime);
        }

        return ret;
    }


    @Override
    public Map<String, Integer> fetchRecommendations(Anime anime) {
        Map<String, Integer> ret = Maps.newHashMap();

        if(anime == null || isBlank(anime.getInfoLink())) {
            return ret;
        }

        try {
            return recommendationsCache.get(anime.getInfoLink());
        } catch (ExecutionException e) {
            log.error("Unable to fetch related anime list from cache for [{}]", anime);
        }

        return ret;
    }

    private void buildAnimeCache() {
        animeEntryCache = CacheBuilder.newBuilder().build(new CacheLoader<String, Optional<Anime>>() {

            @Override
            public Optional<Anime> load(final String infoLink) throws Exception {
                if(isDaemonAvailable()) {
                    return daemonRestRetrievalStrategy.fetchAnime(infoLink);
                }

                return headlessBrowserRetrievalStrategy.fetchAnime(infoLink);
            }
        });
    }

    private void buildRelatedAnimeCache() {
        relatedAnimeCache = CacheBuilder.newBuilder().build(new CacheLoader<String, Set<String>>() {

            @Override
            public Set<String> load(final String url) throws Exception {
                if(isDaemonAvailable()) {
                    return daemonRestRetrievalStrategy.fetchRelatedAnimes(url);
                }

                return headlessBrowserRetrievalStrategy.fetchRelatedAnimes(url);
            }
        });
    }

    private void buildRecommendationsCache() {
        recommendationsCache = CacheBuilder.newBuilder().build(new CacheLoader<String, Map<String, Integer>>() {

            @Override
            public Map<String, Integer> load(final String url) throws Exception {
                if(isDaemonAvailable()) {
                    return daemonRestRetrievalStrategy.fetchRecommendations(url);
                }

                return headlessBrowserRetrievalStrategy.fetchRecommendations(url);
            }
        });
    }
}
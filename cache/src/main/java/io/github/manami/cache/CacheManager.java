package io.github.manami.cache;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.github.manami.cache.strategies.daemon.DaemonRestRetrievalStrategy;
import io.github.manami.cache.strategies.headlessbrowser.HeadlessBrowserRetrievalStrategy;
import io.github.manami.dto.entities.Anime;
import lombok.extern.slf4j.Slf4j;

/**
 * Facade for all inquiries against a cache.
 * It orchestrates the use of concrete {@link io.github.manami.cache.Cache}
 * implementations.
 */
@Slf4j
@Named
public final class CacheManager implements Cache {

    private final DaemonRestRetrievalStrategy daemonRestRetrievalStrategy;
    private final HeadlessBrowserRetrievalStrategy headlessBrowserRetrievalStrategy;

    /**
     * Key: URL of the anime, Value: Instance of the anime including all meta
     * data.
     */
    private LoadingCache<String, Optional<Anime>> animeEntryCache = null;

    /**
     * Key: URL of the anime, Value: List of anime urls which are related to the
     * anime url in the key
     */
    private LoadingCache<String, Set<String>> relatedAnimeCache = null;

    /**
     * Key: URL of the anime, Value: List of anime urls which are recommended
     * titles to the anime url
     */
    private LoadingCache<String, Map<String, Integer>> recommendationsCache = null;


    @Inject
    public CacheManager(final DaemonRestRetrievalStrategy daemonRestRetrievalStrategy, final HeadlessBrowserRetrievalStrategy headlessBrowserRetrievalStrategy) {
        this.daemonRestRetrievalStrategy = daemonRestRetrievalStrategy;
        this.headlessBrowserRetrievalStrategy = headlessBrowserRetrievalStrategy;
        buildAnimeCache();
        buildRelatedAnimeCache();
        buildRecommendationsCache();
    }


    /**
     * Checks whether a {@link DaemonRestRetrievalStrategy} instance is up an
     * running.
     *
     * @return
     */
    private boolean isDaemonAvailable() {
        return daemonRestRetrievalStrategy.isAvailable();
    }


    @Override
    public Optional<Anime> fetchAnime(final String url) {
        Optional<Anime> cachedEntry = Optional.empty();

        if (isBlank(url)) {
            return cachedEntry;
        }

        try {
            cachedEntry = animeEntryCache.get(url);

            if (!cachedEntry.isPresent()) {
                log.warn("No Entry for [{}]. Invalidating cache entry and refetching entry.", url);
                animeEntryCache.invalidate(url);
                cachedEntry = animeEntryCache.get(url);
                log.warn("After reinitialising cache entry for [{}] [{}]", url, cachedEntry);
            }
        } catch (final ExecutionException e) {
            log.error("Error fetching anime entry [{}] from cache.", url);
            return Optional.empty();
        }

        return cachedEntry;
    }


    @Override
    public Set<String> fetchRelatedAnimes(final Anime anime) {
        Set<String> ret = Sets.newHashSet();

        if (isAnimeInvalid(anime)) {
            return ret;
        }

        try {
            final String url = anime.getInfoLink();
            ret = relatedAnimeCache.get(url);

            if (ret == null || ret.isEmpty()) {
                log.warn("No related animes in cache entry [{}]. Invalidating cache entry and refetching entry.", url);
                relatedAnimeCache.invalidate(url);
                ret = relatedAnimeCache.get(url);
                log.warn("After reinitialising cache entry for [{}] [{}]", url, ret);
            }
        } catch (final ExecutionException e) {
            log.error("Unable to fetch related anime list from cache for [{}]", anime);
        }

        return ret;
    }


    @Override
    public Map<String, Integer> fetchRecommendations(final Anime anime) {
        Map<String, Integer> ret = Maps.newHashMap();

        if (isAnimeInvalid(anime)) {
            return ret;
        }

        try {
            final String url = anime.getInfoLink();
            ret = recommendationsCache.get(url);

            if (ret == null || ret.isEmpty()) {
                log.warn("No recommendations in cache entry [{}]. Invalidating cache entry and refetching entry.", url);
                recommendationsCache.invalidate(url);
                ret = recommendationsCache.get(url);
                log.warn("After reinitialising cache entry for [{}] [{}]", url, ret);
            }
        } catch (final ExecutionException e) {
            log.error("Unable to fetch related anime list from cache for [{}]", anime);
        }

        return ret;
    }


    private boolean isAnimeInvalid(final Anime anime) {
        return anime == null || isBlank(anime.getInfoLink());
    }


    private void buildAnimeCache() {
        animeEntryCache = CacheBuilder.newBuilder().build(new CacheLoader<String, Optional<Anime>>() {

            @Override
            public Optional<Anime> load(final String infoLink) throws Exception {
                if (isDaemonAvailable()) {
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
                if (isDaemonAvailable()) {
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
                if (isDaemonAvailable()) {
                    return daemonRestRetrievalStrategy.fetchRecommendations(url);
                }

                return headlessBrowserRetrievalStrategy.fetchRecommendations(url);
            }
        });
    }
}
package io.github.manami.cache;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.http.HttpVersion.HTTP_1_1;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import io.github.manami.cache.populate.AdbEntry;
import io.github.manami.cache.populate.AnimeOfflineDatabase;
import io.github.manami.cache.strategies.headlessbrowser.HeadlessBrowserRetrievalStrategy;
import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.RecommendationList;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.slf4j.MDC;

/**
 * Facade for all inquiries against a cache.
 * It orchestrates the use of concrete {@link io.github.manami.cache.Cache}
 * implementations.
 */
@Slf4j
@Named
public final class CacheManager implements Cache {

    private static final int TITLE_MAX_LENGTH = 200;
    private final HeadlessBrowserRetrievalStrategy headlessBrowserRetrievalStrategy;

    /**
     * Key: URL of the anime, Value: Instance of the anime including all meta
     * data.
     */
    private LoadingCache<InfoLink, Optional<Anime>> animeEntryCache = null;

    /**
     * Key: URL of the anime, Value: Set of anime urls which are related to the
     * anime url in the key
     */
    private LoadingCache<InfoLink, Set<InfoLink>> relatedAnimeCache = null;

    /**
     * Key: URL of the anime, Value: List of anime urls which are recommended
     * titles to the anime url with their amount of occurence
     */
    private LoadingCache<InfoLink, RecommendationList> recommendationsCache = null;


    @Inject
    public CacheManager(final HeadlessBrowserRetrievalStrategy headlessBrowserRetrievalStrategy) {
        this.headlessBrowserRetrievalStrategy = headlessBrowserRetrievalStrategy;
        buildAnimeCache();
        buildRelatedAnimeCache();
        buildRecommendationsCache();

        new Thread(() -> {
            try {
                populateCache();
            } catch (Exception e) {
                log.error("Unable to populate cache.", e);
            }
        }).start();
    }

    private void populateCache() throws IOException, URISyntaxException {
        final CloseableHttpClient hc = HttpClients
            .custom().setHttpProcessor(HttpProcessorBuilder.create().build()).build();
        final HttpGet request = new HttpGet(new URL("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/anime-offline-database.json").toURI());

        newArrayList(request.getAllHeaders()).forEach(request::removeHeader);

        request.setProtocolVersion(HTTP_1_1);
        request.setHeader("Host", "raw.githubusercontent.com");
        request.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:66.0) Gecko/20100101 Firefox/66.0");
        request.setHeader("Accept", "application/json");

        final CloseableHttpResponse execute = hc.execute(request);

        if (execute.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new IllegalStateException("Cache data download failed.");
        }

        List<AdbEntry> adbEntries = new Gson().fromJson(
            new InputStreamReader(execute.getEntity().getContent(), UTF_8),
            AnimeOfflineDatabase.class
        ).data
            .parallelStream()
            .peek(it -> it.sources = it.sources.stream()
                .filter(source -> source.startsWith("https://myanimelist.net"))
                .collect(toList()))
            .filter(it -> it.sources.size() == 1)
            .collect(toList());

        adbEntries.parallelStream()
            .map(it -> {
                Anime anime = new Anime(it.title, new InfoLink(it.sources.get(0)));
                anime.setType(AnimeType.findByName(it.type));
                anime.setEpisodes(it.episodes);
                anime.setPicture(it.picture);
                anime.setThumbnail(it.thumbnail);
                anime.setLocation("/");

                Set<InfoLink> relations = it.relations.stream()
                    .filter(source -> source.startsWith("https://myanimelist.net"))
                    .map(InfoLink::new)
                    .collect(toSet());

                return Pair.of(anime, relations);
            })
            .filter( it -> Anime.isValidAnime(it.getKey()))
            .map( it -> {
                relatedAnimeCache.put(it.getKey().getInfoLink(), it.getValue());
                return it.getKey();
            })
            .forEach( it -> animeEntryCache.put(it.getInfoLink(), Optional.of(it)));
    }

    @Override
    public Optional<Anime> fetchAnime(final InfoLink infoLink) {
        MDC.put("infoLink", infoLink.getUrl());
        Optional<Anime> cachedEntry = Optional.empty();

        if (!infoLink.isValid()) {
            return cachedEntry;
        }

        try {
            cachedEntry = animeEntryCache.get(infoLink);

            if (!cachedEntry.isPresent() || cachedEntry.get().getTitle().length() > TITLE_MAX_LENGTH) {
                log.warn("No meta data entry extracted or title way too long. Invalidating cache entry and refetching it.");
                animeEntryCache.invalidate(infoLink);
                cachedEntry = animeEntryCache.get(infoLink);
                log.warn("Result after reinitialising cache entry [{}]", cachedEntry);
            }
        } catch (final ExecutionException e) {
            log.error("Error fetching anime entry from cache.");
            return Optional.empty();
        }

        return cachedEntry;
    }


    @Override
    public Set<InfoLink> fetchRelatedAnime(final InfoLink infoLink) {
        Set<InfoLink> ret = newHashSet();

        if (!infoLink.isValid()) {
            return ret;
        }

        try {
            ret = relatedAnimeCache.get(infoLink);

            if (ret == null) {
                log.warn("No related anime in cache. Invalidating cache entry and refetching it.");
                relatedAnimeCache.invalidate(infoLink);
                ret = relatedAnimeCache.get(infoLink);
                log.warn("Result after reinitialising cache entry for [{}]", ret);
            }
        } catch (final ExecutionException e) {
            log.error("Unable to fetch related anime list from cache.");
        }

        return ret;
    }


    @Override
    public RecommendationList fetchRecommendations(final InfoLink infoLink) {
        RecommendationList ret = new RecommendationList();

        if (!infoLink.isValid()) {
            return ret;
        }

        try {
            ret = recommendationsCache.get(infoLink);

            if (ret == null || ret.isEmpty()) {
                log.warn("No recommendations in cache entry. Invalidating cache entry and refetching it.");
                recommendationsCache.invalidate(infoLink);
                ret = recommendationsCache.get(infoLink);
                log.warn("Result after reinitialising cache entry for [{}]", ret);
            }
        } catch (final ExecutionException e) {
            log.error("Unable to fetch recommendations from cache for.");
        }

        return ret;
    }


    private void buildAnimeCache() {
        animeEntryCache = CacheBuilder.newBuilder().build(new CacheLoader<InfoLink, Optional<Anime>>() {

            @Override
            public Optional<Anime> load(final InfoLink infoLink) throws Exception {
                return headlessBrowserRetrievalStrategy.fetchAnime(infoLink);
            }
        });
    }


    private void buildRelatedAnimeCache() {
        relatedAnimeCache = CacheBuilder.newBuilder().build(new CacheLoader<InfoLink, Set<InfoLink>>() {

            @Override
            public Set<InfoLink> load(final InfoLink infoLink) throws Exception {
                log.debug("no cache hit for {}", infoLink);
                return headlessBrowserRetrievalStrategy.fetchRelatedAnime(infoLink);
            }
        });
    }


    private void buildRecommendationsCache() {
        recommendationsCache = CacheBuilder.newBuilder().build(new CacheLoader<InfoLink, RecommendationList>() {

            @Override
            public RecommendationList load(final InfoLink infoLink) throws Exception {
                return headlessBrowserRetrievalStrategy.fetchRecommendations(infoLink);
            }
        });
    }
}
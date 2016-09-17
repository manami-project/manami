package io.github.manami.cache.strategies.headlessbrowser;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.collect.Sets;

import io.github.manami.cache.strategies.AnimeRetrieval;
import io.github.manami.cache.strategies.RecommendationsRetrieval;
import io.github.manami.cache.strategies.RelatedAnimeRetrieval;
import io.github.manami.cache.strategies.headlessbrowser.extractor.ExtractorList;
import io.github.manami.cache.strategies.headlessbrowser.extractor.HeadlessBrowser;
import io.github.manami.cache.strategies.headlessbrowser.extractor.anime.AnimeEntryExtractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.relatedanime.RelatedAnimeExtractor;
import io.github.manami.dto.entities.Anime;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

/**
 * This class provides access to the anime meta data. The data are taken
 * directly from the respective website using a headless browser to render the
 * page an extract the relavant data.
 *
 * @author manami-project
 * @since 2.0.0
 */
@Named
@Slf4j
public class HeadlessBrowserRetrievalStrategy implements AnimeRetrieval, RelatedAnimeRetrieval, RecommendationsRetrieval {

    /** Instance of a headless browser. */
    private final HeadlessBrowser browser;

    /** Wrapper for all extractors. */
    private final ExtractorList extractors;


    /**
     * @since 2.2.0
     * @param extractors
     *            List of extractors
     * @param browser
     *            Headless Browser
     */
    @Inject
    public HeadlessBrowserRetrievalStrategy(final ExtractorList extractors, final HeadlessBrowser browser) {
        this.extractors = extractors;
        this.browser = browser;
    }


    @Override
    public Optional<Anime> fetchAnime(final String url) {
        return Optional.ofNullable(downloadAndExtractAnime(url));
    }


    /**
     * Either gets the content from
     *
     * @since 2.0.0
     * @param url
     *            URL to download.
     * @return Raw XML of the downloaded site.
     */
    @Synchronized
    private String downloadSiteContent(final String url) {
        return browser.pageAsString(url);
    }


    /**
     * Downloads an infoLink and returns an anime.
     *
     * @since 2.5.1
     * @return
     */
    private Anime downloadAndExtractAnime(final String url) {
        Anime ret = null;
        final Optional<AnimeEntryExtractor> extractor = extractors.getAnimeEntryExtractor(url);

        if (isNotBlank(url) && extractor.isPresent()) {
            final String normalizedUrl = extractor.get().normalizeInfoLink(url);

            // the site is not cached
            if (isNotBlank(normalizedUrl)) {
                final String infoLinkSite = downloadSiteContent(normalizedUrl);
                ret = extractor.get().extractAnimeEntry(normalizedUrl, infoLinkSite);
            }
        }

        return ret;
    }


    @Override
    public Set<String> fetchRelatedAnimes(final String url) {
        Set<String> ret = Sets.newHashSet();
        final Optional<RelatedAnimeExtractor> extractor = extractors.getRelatedAnimeExtractor(url);

        if (isNotBlank(url) && extractor.isPresent()) {
            final String normalizedUrl = extractor.get().normalizeInfoLink(url);

            // the site is not cached
            if (isNotBlank(normalizedUrl)) {
                final String infoLinkSite = downloadSiteContent(normalizedUrl);
                ret = extractor.get().extractRelatedAnimes(normalizedUrl, infoLinkSite);
            }
        }

        return ret;
    }


    @Override
    public Map<String, Integer> fetchRecommendations(final String url) {
        return null;
    }
}
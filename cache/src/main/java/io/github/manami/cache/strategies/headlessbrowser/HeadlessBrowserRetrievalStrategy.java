package io.github.manami.cache.strategies.headlessbrowser;

import com.google.common.collect.Sets;
import io.github.manami.cache.strategies.AnimeRetrieval;
import io.github.manami.cache.strategies.RecommendationsRetrieval;
import io.github.manami.cache.strategies.RelatedAnimeRetrieval;
import io.github.manami.cache.strategies.headlessbrowser.extractor.ExtractorList;
import io.github.manami.cache.strategies.headlessbrowser.extractor.HeadlessBrowser;
import io.github.manami.cache.strategies.headlessbrowser.extractor.anime.AnimeEntryExtractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.recommendations.RecommendationsExtractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.relatedanime.RelatedAnimeExtractor;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    public Optional<Anime> fetchAnime(final InfoLink infoLink) {
        return Optional.ofNullable(downloadAndExtractAnime(infoLink));
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
    private String downloadSiteContent(final InfoLink url) {
        return browser.pageAsString(url);
    }


    /**
     * Downloads an infoLink and returns an anime.
     *
     * @since 2.5.1
     * @return
     */
    private Anime downloadAndExtractAnime(final InfoLink infoLink) {
        Anime ret = null;
        final Optional<AnimeEntryExtractor> extractor = extractors.getAnimeEntryExtractor(infoLink);

        if (infoLink.isValid() && extractor.isPresent()) {
            final InfoLink normalizedInfoLink = extractor.get().normalizeInfoLink(infoLink);

            // the site is not cached
            if (normalizedInfoLink.isValid()) {
                final String infoLinkSite = downloadSiteContent(normalizedInfoLink);
                ret = extractor.get().extractAnimeEntry(normalizedInfoLink, infoLinkSite);
            }
        }

        return ret;
    }


    @Override
    public Set<InfoLink> fetchRelatedAnimes(final InfoLink infoLink) {
        Set<InfoLink> ret = Sets.newHashSet();
        final Optional<RelatedAnimeExtractor> extractor = extractors.getRelatedAnimeExtractor(infoLink);

        if (infoLink.isValid() && extractor.isPresent()) {
            final InfoLink normalizedInfoLink = extractor.get().normalizeInfoLink(infoLink);

            // the site is not cached
            if (normalizedInfoLink.isValid()) {
                final String infoLinkSite = downloadSiteContent(normalizedInfoLink);
                ret = extractor.get().extractRelatedAnimes(infoLinkSite);
            }
        }

        return ret;
    }


    @Override
    public Map<InfoLink, Integer> fetchRecommendations(final InfoLink infoLink) {
        Map<InfoLink, Integer> ret = new HashMap<>();
        final Optional<RecommendationsExtractor> extractor = extractors.getRecommendationsExtractor(infoLink);

        if (infoLink.isValid() && extractor.isPresent()) {
            InfoLink normalizedInfoLink = extractor.get().normalizeInfoLink(infoLink);

            // the site is not cached
            if (normalizedInfoLink.isValid()) {
                final String infoLinkSite = downloadSiteContent(normalizedInfoLink);
                ret = extractor.get().extractRecommendations(infoLinkSite);
            }
        }

        return ret;
    }
}
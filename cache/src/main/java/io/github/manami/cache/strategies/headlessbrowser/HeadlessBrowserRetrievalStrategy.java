package io.github.manami.cache.strategies.headlessbrowser;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import io.github.manami.cache.strategies.AnimeRetrieval;
import io.github.manami.cache.strategies.RecommendationsRetrieval;
import io.github.manami.cache.strategies.RelatedAnimeRetrieval;
import io.github.manami.cache.strategies.headlessbrowser.extractor.ExtractorList;
import io.github.manami.cache.strategies.headlessbrowser.extractor.anime.AnimeEntryExtractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.recommendations.RecommendationsExtractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.relatedanime.RelatedAnimeExtractor;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.RecommendationList;

/**
 * This class provides access to the anime meta data. The data are taken
 * directly from the respective website using a headless browser to render the
 * page an extract the relavant data.
 *
 * @author manami-project
 * @since 2.0.0
 */
@Named
public class HeadlessBrowserRetrievalStrategy implements AnimeRetrieval, RelatedAnimeRetrieval, RecommendationsRetrieval {

    /** Wrapper for all extractors. */
    private final ExtractorList extractors;


    /**
     * @since 2.2.0
     * @param extractors
     *            List of extractors
     */
    @Inject
    public HeadlessBrowserRetrievalStrategy(final ExtractorList extractors) {
        this.extractors = extractors;
    }


    @Override
    public Optional<Anime> fetchAnime(final InfoLink infoLink) {
        return Optional.ofNullable(downloadAndExtractAnime(infoLink));
    }


    /**
     * Either gets the content from
     *
     * @since 2.0.0
     * @param infoLink
     *            URL to download.
     * @return Raw XML of the downloaded site.
     */
    private String downloadSiteContent(final InfoLink infoLink) {
        return new PhantomJsBrowser().pageAsString(infoLink);
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
        Set<InfoLink> ret = newHashSet();
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
    public RecommendationList fetchRecommendations(final InfoLink infoLink) {
        RecommendationList ret = new RecommendationList();
        final Optional<RecommendationsExtractor> extractor = extractors.getRecommendationsExtractor(infoLink);

        if (infoLink.isValid() && extractor.isPresent()) {
            final InfoLink normalizedInfoLink = extractor.get().normalizeInfoLink(infoLink);

            // the site is not cached
            if (normalizedInfoLink.isValid()) {
                final String infoLinkSite = downloadSiteContent(normalizedInfoLink);
                ret = extractor.get().extractRecommendations(infoLinkSite);
            }
        }

        return ret;
    }
}
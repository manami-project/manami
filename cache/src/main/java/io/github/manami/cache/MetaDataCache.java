package io.github.manami.cache;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import io.github.manami.cache.extractor.HeadlessBrowser;
import io.github.manami.cache.extractor.anime.AnimeSiteExtractor;
import io.github.manami.cache.extractor.anime.ExtractorList;
import io.github.manami.dto.entities.Anime;

/**
 * This class provides access to the anime meta data. The data are either taken
 * directly from the cache itself or the cache downloads them from the
 * respective resource and populates the cache. This cache is initiated to
 * increase the speed of the meta data download as htmlunit is not very fast.
 *
 * @author manami-project
 * @since 2.0.0
 */
@Named
public class MetaDataCache implements Cache {

    /** Logger */
    private final static Logger LOG = LoggerFactory.getLogger(MetaDataCache.class);

    /** Instance of a headless browser. */
    private final HeadlessBrowser browser;

    /** Wrapper for all extractors. */
    private final ExtractorList extractors;

    private final LoadingCache<String, Optional<Anime>> memCache;


    /**
     * @since 2.2.0
     * @param extractors
     *            List of extractors
     * @param browser
     *            Headless Browser
     */
    @Inject
    public MetaDataCache(final ExtractorList extractors, final HeadlessBrowser browser) {
        this.extractors = extractors;
        this.browser = browser;
        memCache = CacheBuilder.newBuilder().build(new CacheLoader<String, Optional<Anime>>() {

            @Override
            public Optional<Anime> load(final String infoLink) throws Exception {
                return Optional.ofNullable(downloadAnime(infoLink));
            }
        });
    }


    @Override
    public Anime fetchAnime(final String url) {
        Anime ret = null;

        try {
            final Optional<Anime> optional = memCache.get(url);
            ret = optional.isPresent() ? optional.get() : null;
        } catch (final Exception e) {
            LOG.error("Could not fetch entry {} from cache.", url, e);
        }

        return ret;
    }


    /**
     * Either gets the content from
     *
     * @since 2.0.0
     * @param url
     *            URL to download.
     * @return Raw XML of the downloaded site.
     */
    private String downloadSiteContent(final String url) {
        synchronized (browser) {
            return browser.pageAsString(url);
        }
    }


    /**
     * Downloads an infoLink and returns an anime.
     *
     * @since 2.5.1
     * @return
     */
    private Anime downloadAnime(final String url) {
        Anime ret = null;
        final AnimeSiteExtractor extractor = extractors.getAnimeExtractor(url);

        if (extractor != null) {
            final String normalizedUrl = extractor.normalizeInfoLink(url);

            // the site is not cached
            if (isNotBlank(normalizedUrl)) {
                final String infoLinkSite = downloadSiteContent(normalizedUrl);
                ret = extractor.extractAnimeEntry(normalizedUrl, infoLinkSite);
            }
        }

        return ret;
    }
}
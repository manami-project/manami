package io.github.manami.cache.extractor.anime;

import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.AbstractMinimalEntry;
import io.github.manami.dto.entities.Anime;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;

/**
 * Abstract class for anime site plugins. Their task is to give the possibility
 * to extract information such as the title, episodes, type, etc.
 *
 * @author manami project
 * @since 2.0.0
 */
public abstract class AbstractAnimeSitePlugin implements AnimeExtractor {

    /** Content on from which all meta information are being extracted from. */
    protected String siteContent;


    @Override
    public Anime extractAnimeEntry(final String url, final String siteContent) {
        if (StringUtils.isBlank(url) || StringUtils.isBlank(siteContent)) {
            return null;
        }

        this.siteContent = siteContent;
        Anime ret = null;
        trimContent();

        if (isValidInfoLink()) {
            final String picture = extractPictureLink();
            final String thumbnail = extractThumbnail();
            final String title = extractTitle();
            final AnimeType type = extractType();
            Integer episodes = 0;
            if (NumberUtils.isNumber(extractEpisodes())) {
                episodes = Integer.valueOf(extractEpisodes());
            }

            if (StringUtils.isNotBlank(title) && type != null && episodes >= 0) {
                ret = new Anime();
                ret.setTitle(StringEscapeUtils.unescapeHtml4(title));
                ret.setType(type);
                ret.setEpisodes(episodes);
                ret.setInfoLink(normalizeInfoLink(url));
                ret.setPicture(StringUtils.isNoneBlank(picture) ? extractPictureLink() : AbstractMinimalEntry.NO_IMG);
                ret.setThumbnail(StringUtils.isNoneBlank(thumbnail) ? extractThumbnail() : AbstractMinimalEntry.NO_IMG_THUMB);

                final List<String> relatedAnimes = extractRelatedAnimes();
                for (final String infoLink : relatedAnimes) {
                    ret.getRelatedAnimes().add(infoLink);
                }
            }
        }

        return ret;
    }


    /**
     * Trims the parameter string.
     *
     * @since 2.0.0
     */
    private void trimContent() {
        siteContent = siteContent.trim();

        // get rid of newlines and doubled whitespaces
        siteContent = siteContent.replaceAll("(\r\n|\n\r|\r|\n|\t)", "");

        while (siteContent.contains("  ")) {
            siteContent = siteContent.replaceAll("  ", " ");
        }
    }


    @Override
    public boolean isResponsible(final String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }

        return url.startsWith("http://" + getDomain()) || url.startsWith("http://www." + getDomain()) || url.startsWith("https://" + getDomain()) || url.startsWith("https://www." + getDomain());
    }


    /**
     * Returns the domain of a specific anime site plugin. Important is that it
     * does not include the protocol.
     *
     * @since 2.0.0
     * @return The domain.
     */
    protected abstract String getDomain();


    /**
     * Checks whether the gathered anime is a valid.
     *
     * @since 2.0.0
     * @return true if it's a valid info link site.
     */
    protected abstract boolean isValidInfoLink();


    /**
     * Extracts the anime's title.
     *
     * @since 2.0.0
     * @return Title
     */
    protected abstract String extractTitle();


    /**
     * Extracts the anime's type.
     *
     * @since 2.0.0
     * @return Type
     */
    protected abstract AnimeType extractType();


    /**
     * Extracts the number of episodes.
     *
     * @since 2.0.0
     * @return Number of episodes.
     */
    protected abstract String extractEpisodes();


    /**
     * Extracts a URL for a picture.
     *
     * @since 2.1.0
     * @return Picture (big)
     */
    protected abstract String extractPictureLink();


    /**
     * Extracts a URL for a picture in thumbnail size.
     *
     * @since 2.1.1
     * @return Picture (thumbnail size)
     */
    protected abstract String extractThumbnail();


    @Override
    public String normalizeInfoLink(final String url) {
        return url;
    }
}

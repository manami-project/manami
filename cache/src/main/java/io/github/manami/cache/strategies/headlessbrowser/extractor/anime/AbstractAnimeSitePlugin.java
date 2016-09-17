package io.github.manami.cache.strategies.headlessbrowser.extractor.anime;

import static io.github.manami.dto.entities.AbstractMinimalEntry.NO_IMG;
import static io.github.manami.dto.entities.AbstractMinimalEntry.NO_IMG_THUMB;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.normalizeSpace;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.math.NumberUtils;

import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.Anime;

/**
 * Abstract class for anime site plugins. Their task is to give the possibility
 * to extract information such as the title, episodes, type, etc.
 *
 * @author manami-project
 * @since 2.0.0
 */
public abstract class AbstractAnimeSitePlugin implements AnimeEntryExtractor {

    /** Content on from which all meta information are being extracted from. */
    protected String siteContent;


    @Override
    public Anime extractAnimeEntry(final String url, final String siteContent) {
        if (isBlank(url) || isBlank(siteContent)) {
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

            if (isNotBlank(title) && type != null && episodes >= 0) {
                ret = new Anime();
                ret.setTitle(StringEscapeUtils.unescapeHtml4(title));
                ret.setType(type);
                ret.setEpisodes(episodes);
                ret.setInfoLink(normalizeInfoLink(url));
                ret.setPicture(isNoneBlank(picture) ? extractPictureLink() : NO_IMG);
                ret.setThumbnail(isNoneBlank(thumbnail) ? extractThumbnail() : NO_IMG_THUMB);
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
        siteContent = normalizeSpace(siteContent);
    }


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

package io.github.manami.cache.strategies.headlessbrowser.extractor;

import io.github.manami.cache.strategies.headlessbrowser.extractor.anime.AnimeExtractor;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Contains a list of all [{@link AnimeExtractor}s.
 *
 * @author manami-project
 * @since 2.1.2
 */
@Named
public class ExtractorList {

    /** List of all anime site extractors. */
    private final List<AnimeExtractor> extractors;


    /**
     * Constructor injecting the list of all annotated extractors.
     *
     * @since 2.1.2
     * @param extractors
     *            List of extractors.
     */
    @Inject
    public ExtractorList(@Extractor final List<AnimeExtractor> extractors) {
        this.extractors = extractors;
    }


    /**
     * Returns the first extractor that claims responsibility.
     *
     * @since 2.3.0
     * @param url
     *            An instance of the first {@link AnimeExtractor} claiming
     *            responsibility or null.
     * @return First {@link AnimeExtractor} which is responsible.
     */
    public AnimeExtractor getAnimeExtractor(final String url) {
        for (final AnimeExtractor extractor : extractors) {
            if (extractor.isResponsible(url)) {
                return extractor;
            }
        }

        return null;
    }
}

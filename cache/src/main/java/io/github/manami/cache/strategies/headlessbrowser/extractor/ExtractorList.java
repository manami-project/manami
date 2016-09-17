package io.github.manami.cache.strategies.headlessbrowser.extractor;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import io.github.manami.cache.strategies.headlessbrowser.extractor.anime.AnimeEntryExtractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.anime.AnimeExtractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.relatedanime.RelatedAnimeExtractor;

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
     *            An instance of the first {@link AnimeEntryExtractor} claiming
     *            responsibility.
     * @return First {@link AnimeEntryExtractor} which is responsible.
     */
    public Optional<AnimeEntryExtractor> getAnimeEntryExtractor(final String url) {
        final Optional<AnimeExtractor> ret = getExtractor(url, AnimeEntryExtractor.class);

        if (ret.isPresent()) {
            return Optional.of((AnimeEntryExtractor) ret.get());
        }

        return Optional.empty();
    }


    /**
     * Returns the first extractor that claims responsibility.
     *
     * @since 2.10.6
     * @param url
     *            An instance of the first {@link RelatedAnimeExtractor}
     *            claiming
     *            responsibility.
     * @return First {@link RelatedAnimeExtractor} which is responsible.
     */
    public Optional<RelatedAnimeExtractor> getRelatedAnimeExtractor(final String url) {
        final Optional<AnimeExtractor> ret = getExtractor(url, RelatedAnimeExtractor.class);

        if (ret.isPresent()) {
            return Optional.of((RelatedAnimeExtractor) ret.get());
        }

        return Optional.empty();
    }


    private Optional<AnimeExtractor> getExtractor(final String url, final Class clazz) {
        for (final AnimeExtractor extractor : extractors) {
            if (extractor.isResponsible(url) && clazz.isInstance(extractor)) {
                return Optional.of(extractor);
            }
        }

        return Optional.empty();
    }
}

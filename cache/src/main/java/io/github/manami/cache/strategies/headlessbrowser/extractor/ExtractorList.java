package io.github.manami.cache.strategies.headlessbrowser.extractor;

import io.github.manami.cache.strategies.headlessbrowser.extractor.anime.AnimeEntryExtractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.recommendations.RecommendationsExtractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.relatedanime.RelatedAnimeExtractor;
import io.github.manami.dto.entities.InfoLink;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Optional;

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
        if (extractors == null) {
            throw new IllegalStateException("Extractor list cannot be null");
        }
        this.extractors = extractors;
    }


    /**
     * Returns the first extractor that claims responsibility.
     *
     * @since 2.3.0
     * @param infoLink
     *            An instance of the first {@link AnimeEntryExtractor} claiming
     *            responsibility.
     * @return First {@link AnimeEntryExtractor} which is responsible.
     */
    public Optional<AnimeEntryExtractor> getAnimeEntryExtractor(final InfoLink infoLink) {
        final Optional<AnimeExtractor> ret = getExtractor(infoLink, AnimeEntryExtractor.class);

        if (ret.isPresent()) {
            return Optional.of((AnimeEntryExtractor) ret.get());
        }

        return Optional.empty();
    }


    /**
     * Returns the first extractor that claims responsibility.
     *
     * @since 2.10.6
     * @param infoLink
     *            An instance of the first {@link RelatedAnimeExtractor}
     *            claiming
     *            responsibility.
     * @return First {@link RelatedAnimeExtractor} which is responsible.
     */
    public Optional<RelatedAnimeExtractor> getRelatedAnimeExtractor(final InfoLink infoLink) {
        final Optional<AnimeExtractor> ret = getExtractor(infoLink, RelatedAnimeExtractor.class);

        if (ret.isPresent()) {
            return Optional.of((RelatedAnimeExtractor) ret.get());
        }

        return Optional.empty();
    }


    private Optional<AnimeExtractor> getExtractor(final InfoLink infoLink, final Class clazz) {
        for (final AnimeExtractor extractor : extractors) {
            if (extractor.isResponsible(infoLink.getUrl()) && clazz.isInstance(extractor)) {
                return Optional.of(extractor);
            }
        }

        return Optional.empty();
    }


    /**
     * Returns the first extractor that claims responsibility.
     *
     * @since 2.10.6
     * @param url
     *            An instance of the first {@link RecommendationsExtractor}
     *            claiming
     *            responsibility.
     * @return First {@link RecommendationsExtractor} which is responsible.
     */
    public Optional<RecommendationsExtractor> getRecommendationsExtractor(final InfoLink url) {
        final Optional<AnimeExtractor> ret = getExtractor(url, RecommendationsExtractor.class);

        if (ret.isPresent()) {
            return Optional.of((RecommendationsExtractor) ret.get());
        }

        return Optional.empty();
    }
}

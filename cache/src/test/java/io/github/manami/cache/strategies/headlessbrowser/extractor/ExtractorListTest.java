package io.github.manami.cache.strategies.headlessbrowser.extractor;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

import io.github.manami.cache.strategies.headlessbrowser.extractor.anime.AnimeEntryExtractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.anime.mal.MyAnimeListNetAnimeExtractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.recommendations.RecommendationsExtractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.recommendations.mal.MyAnimeListNetRecommendationsExtractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.relatedanime.RelatedAnimeExtractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.relatedanime.mal.MyAnimeListNetRelatedAnimeExtractor;

public class ExtractorListTest {

    private List<AnimeExtractor> extractorListAll;
    private static final String DEATH_NOTE_URL = "https://myanimelist.net/anime/1535/Death_Note";
    private static final String DEATH_NOTE_UNSUPPORTED_URL = "http://www.animenewsnetwork.com/encyclopedia/anime.php?id=6592";


    @BeforeMethod
    public void beforeMethod() {
        extractorListAll = Lists.newArrayList();
        extractorListAll.add(new MyAnimeListNetAnimeExtractor());
        extractorListAll.add(new MyAnimeListNetRelatedAnimeExtractor());
        extractorListAll.add(new MyAnimeListNetRecommendationsExtractor());
    }


    @Test(groups = "unitTest", expectedExceptions = IllegalStateException.class)
    public void testExtractorListInitializedWithNull() {
        // given

        // when
        new ExtractorList(null);

        // then
    }


    @Test(groups = "unitTest")
    public void testExtractorListNormally() {
        // given

        // when
        final ExtractorList list = new ExtractorList(extractorListAll);

        // then
        assertNotNull(list);
    }


    @Test(groups = "unitTest")
    public void getAnimeEntryExtractor() {
        // given
        final ExtractorList list = new ExtractorList(extractorListAll);

        // when
        final Optional<AnimeEntryExtractor> result = list.getAnimeEntryExtractor(DEATH_NOTE_URL);

        // then
        assertTrue(result.isPresent());
    }


    @Test(groups = "unitTest")
    public void getAnimeEntryExtractorNotResponsible() {
        // given
        final ExtractorList list = new ExtractorList(extractorListAll);

        // when
        final Optional<AnimeEntryExtractor> result = list.getAnimeEntryExtractor(DEATH_NOTE_UNSUPPORTED_URL);

        // then
        assertFalse(result.isPresent());
    }


    @Test(groups = "unitTest")
    public void getAnimeEntryExtractorNotAvailable() {
        // given
        final List<AnimeExtractor> extractorList = Lists.newArrayList();
        extractorList.add(new MyAnimeListNetRelatedAnimeExtractor());
        extractorList.add(new MyAnimeListNetRecommendationsExtractor());
        final ExtractorList list = new ExtractorList(extractorList);

        // when
        final Optional<AnimeEntryExtractor> result = list.getAnimeEntryExtractor(DEATH_NOTE_URL);

        // then
        assertFalse(result.isPresent());
    }


    @Test(groups = "unitTest")
    public void getRecommendationsExtractor() {
        // given
        final ExtractorList list = new ExtractorList(extractorListAll);

        // when
        final Optional<RecommendationsExtractor> result = list.getRecommendationsExtractor(DEATH_NOTE_URL);

        // then
        assertTrue(result.isPresent());
    }


    @Test(groups = "unitTest")
    public void getRecommendationsExtractorNotResponsible() {
        // given
        final ExtractorList list = new ExtractorList(extractorListAll);

        // when
        final Optional<RecommendationsExtractor> result = list.getRecommendationsExtractor(DEATH_NOTE_UNSUPPORTED_URL);

        // then
        assertFalse(result.isPresent());
    }


    @Test(groups = "unitTest")
    public void getRecommendationsExtractorNotAvailable() {
        // given
        final List<AnimeExtractor> extractorList = Lists.newArrayList();
        extractorList.add(new MyAnimeListNetAnimeExtractor());
        extractorList.add(new MyAnimeListNetRelatedAnimeExtractor());
        final ExtractorList list = new ExtractorList(extractorList);

        // when
        final Optional<RecommendationsExtractor> result = list.getRecommendationsExtractor(DEATH_NOTE_URL);

        // then
        assertFalse(result.isPresent());
    }


    @Test(groups = "unitTest")
    public void getRelatedAnimeExtractor() {
        // given
        final ExtractorList list = new ExtractorList(extractorListAll);

        // when
        final Optional<RelatedAnimeExtractor> result = list.getRelatedAnimeExtractor(DEATH_NOTE_URL);

        // then
        assertTrue(result.isPresent());
    }


    @Test(groups = "unitTest")
    public void getRelatedAnimeExtractorNotResponsible() {
        // given
        final ExtractorList list = new ExtractorList(extractorListAll);

        // when
        final Optional<RelatedAnimeExtractor> result = list.getRelatedAnimeExtractor(DEATH_NOTE_UNSUPPORTED_URL);

        // then
        assertFalse(result.isPresent());
    }


    @Test(groups = "unitTest")
    public void getRelatedAnimeExtractorNotAvailable() {
        // given
        final List<AnimeExtractor> extractorList = Lists.newArrayList();
        extractorList.add(new MyAnimeListNetAnimeExtractor());
        extractorList.add(new MyAnimeListNetRecommendationsExtractor());
        final ExtractorList list = new ExtractorList(extractorList);

        // when
        final Optional<RelatedAnimeExtractor> result = list.getRelatedAnimeExtractor(DEATH_NOTE_URL);

        // then
        assertFalse(result.isPresent());
    }
}

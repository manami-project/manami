package io.github.manami.cache.strategies.headlessbrowser.extractor;

import static com.google.common.collect.Lists.newArrayList;
import static io.github.manami.cache.strategies.headlessbrowser.extractor.TestConst.UNIT_TEST_GROUP;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.github.manami.cache.strategies.headlessbrowser.extractor.anime.AnimeEntryExtractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.anime.mal.MyAnimeListNetAnimeExtractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.recommendations.RecommendationsExtractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.recommendations.mal.MyAnimeListNetRecommendationsExtractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.relatedanime.RelatedAnimeExtractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.relatedanime.mal.MyAnimeListNetRelatedAnimeExtractor;
import io.github.manami.dto.entities.InfoLink;

public class ExtractorListTest {

    private List<AnimeExtractor> extractorListAll;
    private static final String DEATH_NOTE_URL = "https://myanimelist.net/anime/1535/Death_Note";
    private static final String DEATH_NOTE_UNSUPPORTED_URL = "http://www.animenewsnetwork.com/encyclopedia/anime.php?id=6592";


    @BeforeMethod
    public void beforeMethod() {
        extractorListAll = newArrayList();
        extractorListAll.add(new MyAnimeListNetAnimeExtractor());
        extractorListAll.add(new MyAnimeListNetRelatedAnimeExtractor());
        extractorListAll.add(new MyAnimeListNetRecommendationsExtractor());
    }


    @Test(groups = UNIT_TEST_GROUP, expectedExceptions = IllegalStateException.class)
    public void testExtractorListInitializedWithNull() {
        // given

        // when
        new ExtractorList(null);

        // then
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testExtractorListNormally() {
        // given

        // when
        final ExtractorList list = new ExtractorList(extractorListAll);

        // then
        assertNotNull(list);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void getAnimeEntryExtractor() {
        // given
        final ExtractorList list = new ExtractorList(extractorListAll);

        // when
        final Optional<AnimeEntryExtractor> result = list.getAnimeEntryExtractor(new InfoLink(DEATH_NOTE_URL));

        // then
        assertTrue(result.isPresent());
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void getAnimeEntryExtractorNotResponsible() {
        // given
        final ExtractorList list = new ExtractorList(extractorListAll);

        // when
        final Optional<AnimeEntryExtractor> result = list.getAnimeEntryExtractor(new InfoLink(DEATH_NOTE_UNSUPPORTED_URL));

        // then
        assertFalse(result.isPresent());
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void getAnimeEntryExtractorNotAvailable() {
        // given
        final List<AnimeExtractor> extractorList = newArrayList();
        extractorList.add(new MyAnimeListNetRelatedAnimeExtractor());
        extractorList.add(new MyAnimeListNetRecommendationsExtractor());
        final ExtractorList list = new ExtractorList(extractorList);

        // when
        final Optional<AnimeEntryExtractor> result = list.getAnimeEntryExtractor(new InfoLink(DEATH_NOTE_URL));

        // then
        assertFalse(result.isPresent());
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void getRecommendationsExtractor() {
        // given
        final ExtractorList list = new ExtractorList(extractorListAll);

        // when
        final Optional<RecommendationsExtractor> result = list.getRecommendationsExtractor(new InfoLink(DEATH_NOTE_URL));

        // then
        assertTrue(result.isPresent());
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void getRecommendationsExtractorNotResponsible() {
        // given
        final ExtractorList list = new ExtractorList(extractorListAll);

        // when
        final Optional<RecommendationsExtractor> result = list.getRecommendationsExtractor(new InfoLink(DEATH_NOTE_UNSUPPORTED_URL));

        // then
        assertFalse(result.isPresent());
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void getRecommendationsExtractorNotAvailable() {
        // given
        final List<AnimeExtractor> extractorList = newArrayList();
        extractorList.add(new MyAnimeListNetAnimeExtractor());
        extractorList.add(new MyAnimeListNetRelatedAnimeExtractor());
        final ExtractorList list = new ExtractorList(extractorList);

        // when
        final Optional<RecommendationsExtractor> result = list.getRecommendationsExtractor(new InfoLink(DEATH_NOTE_URL));

        // then
        assertFalse(result.isPresent());
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void getRelatedAnimeExtractor() {
        // given
        final ExtractorList list = new ExtractorList(extractorListAll);

        // when
        final Optional<RelatedAnimeExtractor> result = list.getRelatedAnimeExtractor(new InfoLink(DEATH_NOTE_URL));

        // then
        assertTrue(result.isPresent());
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void getRelatedAnimeExtractorNotResponsible() {
        // given
        final ExtractorList list = new ExtractorList(extractorListAll);

        // when
        final Optional<RelatedAnimeExtractor> result = list.getRelatedAnimeExtractor(new InfoLink(DEATH_NOTE_UNSUPPORTED_URL));

        // then
        assertFalse(result.isPresent());
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void getRelatedAnimeExtractorNotAvailable() {
        // given
        final List<AnimeExtractor> extractorList = newArrayList();
        extractorList.add(new MyAnimeListNetAnimeExtractor());
        extractorList.add(new MyAnimeListNetRecommendationsExtractor());
        final ExtractorList list = new ExtractorList(extractorList);

        // when
        final Optional<RelatedAnimeExtractor> result = list.getRelatedAnimeExtractor(new InfoLink(DEATH_NOTE_URL));

        // then
        assertFalse(result.isPresent());
    }
}

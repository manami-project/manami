package io.github.manami.dto.entities;

import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class RecommendationListTest {

    @Test(groups = UNIT_TEST_GROUP)
    public void testConstructor() {
        // given

        // when
        final RecommendationList sut = new RecommendationList();

        // then
        assertFalse(sut.isNotEmpty());
        assertTrue(sut.isEmpty());
        assertNotNull(sut.asList());
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testAddRecommendation() {
        // given
        final RecommendationList sut = new RecommendationList();
        final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");
        final Recommendation recom = new Recommendation(infoLink, 103);

        // when
        sut.addRecommendation(recom);

        // then
        assertTrue(sut.isNotEmpty());
        assertFalse(sut.isEmpty());
        assertNotNull(sut.asList());
        assertTrue(sut.containsKey(infoLink));
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testAsList() {
        // given
        final RecommendationList sut = new RecommendationList();
        final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");
        final Recommendation recom = new Recommendation(infoLink, 103);
        sut.addRecommendation(recom);

        // when
        sut.asList().clear();

        // then
        assertTrue(sut.isNotEmpty());
        assertFalse(sut.isEmpty());
        assertNotNull(sut.asList());
        assertTrue(sut.containsKey(infoLink));
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testNotContainsKey() {
        // given
        final RecommendationList sut = new RecommendationList();
        sut.addRecommendation(new Recommendation(new InfoLink("http://myanimelist.net/anime/1535"), 103));

        // when
        final boolean result = sut.containsKey(new InfoLink("https://myanimelist.net/anime/32281"));

        // then
        assertFalse(result);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testGet() {
        // given
        final RecommendationList sut = new RecommendationList();
        final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");
        final Recommendation recom = new Recommendation(infoLink, 103);
        sut.addRecommendation(recom);

        // when
        final Recommendation result = sut.get(infoLink);

        // then
        assertTrue(sut.isNotEmpty());
        assertFalse(sut.isEmpty());
        assertNotNull(sut.asList());
        assertTrue(sut.containsKey(infoLink));
        assertEquals(result, recom);
    }
}

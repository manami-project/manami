package io.github.manami.cache.strategies.headlessbrowser.extractor.recommendations.mal;

import static com.google.common.collect.Sets.newHashSet;
import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.github.manami.cache.strategies.headlessbrowser.extractor.util.mal.MyAnimeListNetUtil;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.RecommendationList;

public class MyAnimeListNetRecommendationsExtractorTest {

    private static final String DEATH_NOTE_URL_NO_PROTOCOL = MyAnimeListNetUtil.DOMAIN + "/anime/1535/Death_Note";
    private static final String EXPECTED_DEATH_NOTE_URL = "https://myanimelist.net/anime.php?id=1535&display=userrecs";
    private static final String TEST_FILE = "recommendations_test_entry.html";
    private MyAnimeListNetRecommendationsExtractor sut;
    private String rawHtml;


    @BeforeMethod
    public void beforeMethod() throws IOException {
        sut = new MyAnimeListNetRecommendationsExtractor();
        final ClassPathResource resource = new ClassPathResource(TEST_FILE);
        final StringBuilder strBuilder = new StringBuilder();
        Files.readAllLines(resource.getFile().toPath()).forEach(strBuilder::append);
        rawHtml = strBuilder.toString();
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void extractRecommendations() {
        // given
        final String entry1 = "http://myanimelist.net/anime/2236";
        final Set<InfoLink> entries = newHashSet();
        entries.add(new InfoLink("http://myanimelist.net/anime/648"));
        entries.add(new InfoLink("http://myanimelist.net/anime/4382"));
        entries.add(new InfoLink("http://myanimelist.net/anime/21845"));
        entries.add(new InfoLink("http://myanimelist.net/anime/1692"));
        entries.add(new InfoLink("http://myanimelist.net/anime/2476"));
        entries.add(new InfoLink("http://myanimelist.net/anime/661"));
        entries.add(new InfoLink("http://myanimelist.net/anime/147"));
        entries.add(new InfoLink("http://myanimelist.net/anime/510"));
        entries.add(new InfoLink("http://myanimelist.net/anime/2973"));

        // when
        final RecommendationList result = sut.extractRecommendations(rawHtml);

        // then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(result.asList().size(), 10);
        assertTrue(result.containsKey(new InfoLink(entry1)));
        assertEquals(result.get(new InfoLink(entry1)).getAmount(), Integer.valueOf(2));

        entries.forEach(entry -> {
            assertTrue(result.containsKey(entry));
            assertEquals(result.get(entry).getAmount(), Integer.valueOf(1));
        });
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void isResponsibleTrueHttpWww() {
        // given
        final InfoLink url = new InfoLink("http://www." + DEATH_NOTE_URL_NO_PROTOCOL);

        // when
        final boolean result = sut.isResponsible(url);

        // then
        assertTrue(result);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void isResponsibleTrueHttp() {
        // given
        final InfoLink url = new InfoLink("http://" + DEATH_NOTE_URL_NO_PROTOCOL);

        // when
        final boolean result = sut.isResponsible(url);

        // then
        assertTrue(result);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void isResponsibleTrueHttpsWww() {
        // given
        final InfoLink url = new InfoLink("https://www." + DEATH_NOTE_URL_NO_PROTOCOL);

        // when
        final boolean result = sut.isResponsible(url);

        // then
        assertTrue(result);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void isResponsibleTrueHttps() {
        // given
        final InfoLink url = new InfoLink("https://" + DEATH_NOTE_URL_NO_PROTOCOL);

        // when
        final boolean result = sut.isResponsible(url);

        // then
        assertTrue(result);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void isResponsibleFalse() {
        // given
        final InfoLink url = new InfoLink("https://animenewsnetwork.com/encyclopedia/anime.php?id=6592");

        // when
        final boolean result = sut.isResponsible(url);

        // then
        assertFalse(result);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void isResponsibleBlank() {
        // given
        final InfoLink urlEmpty = new InfoLink(EMPTY);
        final InfoLink urlWhitespace = new InfoLink(EMPTY);
        final InfoLink urlNull = new InfoLink(null);

        // when
        final boolean resultEmpty = sut.isResponsible(urlEmpty);
        final boolean resultWhitespace = sut.isResponsible(urlWhitespace);
        final boolean resultNull = sut.isResponsible(urlNull);

        // then
        assertFalse(resultEmpty);
        assertFalse(resultWhitespace);
        assertFalse(resultNull);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void normalizeInfoLinkQueryParameter() {
        // given
        final InfoLink infoLink = new InfoLink("https://myanimelist.net/anime.php?id=1535");

        // when
        final InfoLink result = sut.normalizeInfoLink(infoLink);

        // then
        assertEquals(result.getUrl(), EXPECTED_DEATH_NOTE_URL);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void normalizeInfoLinkSearch() {
        // given
        final InfoLink infoLink = new InfoLink("https://myanimelist.net/anime/1535/Death_Note?q=death%20note");

        // when
        final InfoLink result = sut.normalizeInfoLink(infoLink);

        // then
        assertEquals(result.getUrl(), EXPECTED_DEATH_NOTE_URL);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void normalizeInfoLinkDefault() {
        // given
        final InfoLink url = new InfoLink("https://myanimelist.net/anime/1535/Death_Note");

        // when
        final InfoLink result = sut.normalizeInfoLink(url);

        // then
        assertEquals(result.getUrl(), EXPECTED_DEATH_NOTE_URL);
    }


    @Test(groups = UNIT_TEST_GROUP, description = "If you get the expected url already it is being returned as is, but with http instead of https.")
    public void normalizeInfoLinkIdentical() {
        // given
        final InfoLink infoLink = new InfoLink("https://myanimelist.net/anime/1535/Death_Note");

        // when
        final InfoLink result = sut.normalizeInfoLink(infoLink);

        // then
        assertEquals(result.getUrl(), EXPECTED_DEATH_NOTE_URL);
    }


    @Test(groups = UNIT_TEST_GROUP, description = "If the url does not match the expected pattern it is being returned unchanged.")
    public void normalizeInfoLinkDifferentPattern() {
        // given
        final InfoLink infoLink = new InfoLink(" https://myanimelist.net/news?_location=mal_h_m");

        // when
        final InfoLink result = sut.normalizeInfoLink(infoLink);

        // then
        assertEquals(result.getUrl(), infoLink.getUrl());
    }
}

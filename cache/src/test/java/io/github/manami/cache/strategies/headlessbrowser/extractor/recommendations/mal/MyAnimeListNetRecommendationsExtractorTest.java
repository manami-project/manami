package io.github.manami.cache.strategies.headlessbrowser.extractor.recommendations.mal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Set;

import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.beust.jcommander.internal.Sets;

import io.github.manami.cache.strategies.headlessbrowser.extractor.util.mal.MyAnimeListNetUtil;

public class MyAnimeListNetRecommendationsExtractorTest {

    private static final String DEATH_NOTE_URL_NO_PROTOCOL = MyAnimeListNetUtil.DOMAIN + "/anime/1535/Death_Note";
    private static final String EXPECTED_DEATH_NOTE_URL = "http://myanimelist.net/anime/1535/Death_Note/userrecs";
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


    @AfterMethod
    public void afterMethod() {
    }


    @Test
    public void extractRecommendations() {
        // given
        final String entry1 = "http://myanimelist.net/anime/2236";
        final Set<String> entries = Sets.newHashSet();

        entries.add("http://myanimelist.net/anime/648");
        entries.add("http://myanimelist.net/anime/4382");
        entries.add("http://myanimelist.net/anime/21845");
        entries.add("http://myanimelist.net/anime/1692");
        entries.add("http://myanimelist.net/anime/2476");
        entries.add("http://myanimelist.net/anime/661");
        entries.add("http://myanimelist.net/anime/147");
        entries.add("http://myanimelist.net/anime/510");
        entries.add("http://myanimelist.net/anime/2973");

        // when
        final Map<String, Integer> result = sut.extractRecommendations(rawHtml);

        // then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(result.size(), 10);
        assertTrue(result.containsKey(entry1));
        assertEquals(result.get(entry1), Integer.valueOf(2));

        entries.forEach(entry -> {
            assertTrue(result.containsKey(entry));
            assertEquals(result.get(entry), Integer.valueOf(1));
        });
    }


    @Test(groups = "unitTest")
    public void isResponsibleTrueHttpWww() {
        // given
        final String url = "http://www." + DEATH_NOTE_URL_NO_PROTOCOL;

        // when
        final boolean result = sut.isResponsible(url);

        // then
        assertTrue(result);
    }


    @Test(groups = "unitTest")
    public void isResponsibleTrueHttp() {
        // given
        final String url = "http://" + DEATH_NOTE_URL_NO_PROTOCOL;

        // when
        final boolean result = sut.isResponsible(url);

        // then
        assertTrue(result);
    }


    @Test(groups = "unitTest")
    public void isResponsibleTrueHttpsWww() {
        // given
        final String url = "https://www." + DEATH_NOTE_URL_NO_PROTOCOL;

        // when
        final boolean result = sut.isResponsible(url);

        // then
        assertTrue(result);
    }


    @Test(groups = "unitTest")
    public void isResponsibleTrueHttps() {
        // given
        final String url = "https://" + DEATH_NOTE_URL_NO_PROTOCOL;

        // when
        final boolean result = sut.isResponsible(url);

        // then
        assertTrue(result);
    }


    @Test(groups = "unitTest")
    public void isResponsibleFalse() {
        // given
        final String url = "https://animenewsnetwork.com/encyclopedia/anime.php?id=6592";

        // when
        final boolean result = sut.isResponsible(url);

        // then
        assertFalse(result);
    }


    @Test(groups = "unitTest")
    public void isResponsibleBlank() {
        // given
        final String urlEmpty = "";
        final String urlWhitespace = "";
        final String urlNull = null;

        // when
        final boolean resultEmpty = sut.isResponsible(urlEmpty);
        final boolean resultWhitespace = sut.isResponsible(urlWhitespace);
        final boolean resultNull = sut.isResponsible(urlNull);

        // then
        assertFalse(resultEmpty);
        assertFalse(resultWhitespace);
        assertFalse(resultNull);
    }


    @Test(groups = "unitTest")
    public void normalizeInfoLinkQueryParameter() {
        // given
        final String url = "https://myanimelist.net/anime.php?id=1535";

        // when
        final String result = sut.normalizeInfoLink(url);

        // then
        assertEquals(result, EXPECTED_DEATH_NOTE_URL);
    }


    @Test(groups = "unitTest")
    public void normalizeInfoLinkSearch() {
        // given
        final String url = "https://myanimelist.net/anime/1535/Death_Note?q=death%20note";

        // when
        final String result = sut.normalizeInfoLink(url);

        // then
        assertEquals(result, EXPECTED_DEATH_NOTE_URL);
    }


    @Test(groups = "unitTest")
    public void normalizeInfoLinkDefault() {
        // given
        final String url = "https://myanimelist.net/anime/1535/Death_Note";

        // when
        final String result = sut.normalizeInfoLink(url);

        // then
        assertEquals(result, EXPECTED_DEATH_NOTE_URL);
    }


    @Test(groups = "unitTest", description = "If you get the expected url already it is being returned as is, but with http instead of https.")
    public void normalizeInfoLinkIdentical() {
        // given
        final String url = "https://myanimelist.net/anime/1535/Death_Note";

        // when
        final String result = sut.normalizeInfoLink(url);

        // then
        assertEquals(result, EXPECTED_DEATH_NOTE_URL);
    }


    @Test(groups = "unitTest", description = "If the url does not match the expected pattern it is being returned unchanged.")
    public void normalizeInfoLinkDifferentPattern() {
        // given
        final String url = " https://myanimelist.net/news?_location=mal_h_m";

        // when
        final String result = sut.normalizeInfoLink(url);

        // then
        assertEquals(result, url);
    }
}

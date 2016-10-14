package io.github.manami.cache.strategies.headlessbrowser.extractor.relatedanime;

import io.github.manami.cache.strategies.headlessbrowser.extractor.relatedanime.mal.MyAnimeListNetRelatedAnimeExtractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.util.mal.MyAnimeListNetUtil;
import io.github.manami.dto.entities.InfoLink;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class RelatedAnimeExtractorTest {

    private static final String DEATH_NOTE_URL_NO_PROTOCOL = MyAnimeListNetUtil.DOMAIN + "/anime/1535/Death_Note";
    private static final String EXPECTED_DEATH_NOTE_URL = "http://myanimelist.net/anime/1535";
    private static final String TEST_FILE = "related_anime_test_entry.html";
    private MyAnimeListNetRelatedAnimeExtractor sut;
    private String rawHtml;


    @BeforeMethod
    public void beforeMethod() throws IOException {
        sut = new MyAnimeListNetRelatedAnimeExtractor();
        final ClassPathResource resource = new ClassPathResource(TEST_FILE);
        final StringBuilder strBuilder = new StringBuilder();
        Files.readAllLines(resource.getFile().toPath()).forEach(strBuilder::append);
        rawHtml = strBuilder.toString();
    }


    @Test
    public void extractRelatedAnimes() {
        // given

        // when
        final Set<String> result = sut.extractRelatedAnimes(rawHtml);

        // then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(result.size(), 3);
        assertTrue(result.contains("http://myanimelist.net/anime/849"));
        assertTrue(result.contains("http://myanimelist.net/anime/7311"));
        assertTrue(result.contains("http://myanimelist.net/anime/26351"));
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
        final InfoLink infoLink = new InfoLink("https://myanimelist.net/anime.php?id=1535");

        // when
        final InfoLink result = sut.normalizeInfoLink(infoLink);

        // then
        assertEquals(result.getUrl(), EXPECTED_DEATH_NOTE_URL);
    }


    @Test(groups = "unitTest")
    public void normalizeInfoLinkSearch() {
        // given
        final InfoLink infoLink = new InfoLink("https://myanimelist.net/anime/1535/Death_Note?q=death%20note");

        // when
        final InfoLink result = sut.normalizeInfoLink(infoLink);

        // then
        assertEquals(result.getUrl(), EXPECTED_DEATH_NOTE_URL);
    }


    @Test(groups = "unitTest")
    public void normalizeInfoLinkDefault() {
        // given
        final InfoLink infoLink = new InfoLink("https://myanimelist.net/anime/1535/Death_Note");

        // when
        final InfoLink result = sut.normalizeInfoLink(infoLink);

        // then
        assertEquals(result.getUrl(), EXPECTED_DEATH_NOTE_URL);
    }


    @Test(groups = "unitTest", description = "If you get the expected url already it is being returned as is, but with http instead of https.")
    public void normalizeInfoLinkIdentical() {
        // given
        final InfoLink infoLink = new InfoLink("https://myanimelist.net/anime/1535/Death_Note");

        // when
        final InfoLink result = sut.normalizeInfoLink(infoLink);

        // then
        assertEquals(result.getUrl(), EXPECTED_DEATH_NOTE_URL);
    }


    @Test(groups = "unitTest", description = "If the url does not match the expected pattern it is being returned unchanged.")
    public void normalizeInfoLinkDifferentPattern() {
        // given
        final InfoLink infoLink = new InfoLink(" https://myanimelist.net/news?_location=mal_h_m");

        // when
        final InfoLink result = sut.normalizeInfoLink(infoLink);

        // then
        assertEquals(result.getUrl(), infoLink.getUrl());
    }
}

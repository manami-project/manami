package io.github.manami.cache.strategies.headlessbrowser.extractor.anime.mal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;

import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.github.manami.cache.strategies.headlessbrowser.extractor.util.mal.MyAnimeListNetUtil;
import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.Anime;

public class MyAnimeListNetAnimeExtractorTest {

    private static final String DEATH_NOTE_URL_NO_PROTOCOL = MyAnimeListNetUtil.DOMAIN + "/anime/1535/Death_Note";
    private static final String EXPECTED_DEATH_NOTE_URL = "http://myanimelist.net/anime/1535";
    private static final String TEST_FILE = "meta_information_test_entry.html";
    private MyAnimeListNetAnimeExtractor sut;
    private String rawHtml;


    @BeforeMethod
    public void setUp() throws IOException {
        sut = new MyAnimeListNetAnimeExtractor();
        final ClassPathResource resource = new ClassPathResource(TEST_FILE);
        final StringBuilder strBuilder = new StringBuilder();
        Files.readAllLines(resource.getFile().toPath()).forEach(strBuilder::append);
        rawHtml = strBuilder.toString();
    }


    @Test(groups = "unitTest")
    public void testIsValidInfoLink() throws IOException {
        // given
        sut.extractAnimeEntry("http://myanimelist.net/anime/1535", rawHtml);

        // when
        final boolean result = sut.isValidInfoLink();

        // then
        assertTrue(result);
    }


    @Test(groups = "unitTest")
    public void testExtractTitle() throws IOException {
        // given
        final String expectedValue = "Death Note";

        // when
        final Anime result = sut.extractAnimeEntry("http://myanimelist.net/anime/1535", rawHtml);

        // then
        assertNotNull(result);
        assertEquals(result.getTitle(), expectedValue);
    }


    @Test(groups = "unitTest")
    public void testExtractType() throws IOException {
        // given
        final AnimeType expectedValue = AnimeType.TV;

        // when
        final Anime result = sut.extractAnimeEntry("http://myanimelist.net/anime/1535", rawHtml);

        // then
        assertNotNull(result);
        assertEquals(result.getType(), expectedValue);
    }


    @Test(groups = "unitTest")
    public void testExtractEpisodes() throws IOException {
        // given
        final int expectedValue = 37;

        // when
        final Anime result = sut.extractAnimeEntry("http://myanimelist.net/anime/1535", rawHtml);

        // then
        assertNotNull(result);
        assertEquals(result.getEpisodes(), expectedValue);
    }


    @Test(groups = "unitTest")
    public void testExtractPictureLink() throws IOException {
        // given
        final String expectedValue = "https://myanimelist.cdn-dena.com/images/anime/9/9453.jpg";

        // when
        final Anime result = sut.extractAnimeEntry("http://myanimelist.net/anime/1535", rawHtml);

        // then
        assertNotNull(result);
        assertEquals(result.getPicture(), expectedValue);
    }


    @Test(groups = "unitTest")
    public void testExtractThumbnail() throws IOException {
        // given
        final String expectedValue = "https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg";

        // when
        final Anime result = sut.extractAnimeEntry("http://myanimelist.net/anime/1535", rawHtml);

        // then
        assertNotNull(result);
        assertEquals(result.getThumbnail(), expectedValue);
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

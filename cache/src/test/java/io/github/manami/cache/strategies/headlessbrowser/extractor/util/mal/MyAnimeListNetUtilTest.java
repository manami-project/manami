package io.github.manami.cache.strategies.headlessbrowser.extractor.util.mal;

import io.github.manami.dto.entities.InfoLink;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MyAnimeListNetUtilTest {

    private static final String DEATH_NOTE_URL_NO_PROTOCOL = MyAnimeListNetUtil.DOMAIN + "/anime/1535/Death_Note";
    private static final String EXPECTED_DEATH_NOTE_URL = "http://myanimelist.net/anime/1535";


    @Test(groups = "unitTest")
    public void isResponsibleTrueHttpWww() {
        // given
        final InfoLink infoLink = new InfoLink("http://www." + DEATH_NOTE_URL_NO_PROTOCOL);

        // when
        final boolean result = MyAnimeListNetUtil.isResponsible(infoLink);

        // then
        assertTrue(result);
    }


    @Test(groups = "unitTest")
    public void isResponsibleTrueHttp() {
        // given
        final InfoLink infoLink = new InfoLink("http://" + DEATH_NOTE_URL_NO_PROTOCOL);

        // when
        final boolean result = MyAnimeListNetUtil.isResponsible(infoLink);

        // then
        assertTrue(result);
    }


    @Test(groups = "unitTest")
    public void isResponsibleTrueHttpsWww() {
        // given
        final InfoLink infoLink = new InfoLink("https://www." + DEATH_NOTE_URL_NO_PROTOCOL);

        // when
        final boolean result = MyAnimeListNetUtil.isResponsible(infoLink);

        // then
        assertTrue(result);
    }


    @Test(groups = "unitTest")
    public void isResponsibleTrueHttps() {
        // given
        final InfoLink infoLink = new InfoLink("https://" + DEATH_NOTE_URL_NO_PROTOCOL);

        // when
        final boolean result = MyAnimeListNetUtil.isResponsible(infoLink);

        // then
        assertTrue(result);
    }


    @Test(groups = "unitTest")
    public void isResponsibleFalse() {
        // given
        final InfoLink infoLink = new InfoLink("https://animenewsnetwork.com/encyclopedia/anime.php?id=6592");

        // when
        final boolean result = MyAnimeListNetUtil.isResponsible(infoLink);

        // then
        assertFalse(result);
    }


    @Test(groups = "unitTest")
    public void isResponsibleBlank() {
        // given
        final InfoLink urlEmpty = new InfoLink("");
        final InfoLink urlWhitespace = new InfoLink("");
        final InfoLink urlNull = new InfoLink(null);

        // when
        final boolean resultEmpty = MyAnimeListNetUtil.isResponsible(urlEmpty);
        final boolean resultWhitespace = MyAnimeListNetUtil.isResponsible(urlWhitespace);
        final boolean resultNull = MyAnimeListNetUtil.isResponsible(urlNull);

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
        final InfoLink result = MyAnimeListNetUtil.normalizeInfoLink(infoLink);

        // then
        assertEquals(result.getUrl(), EXPECTED_DEATH_NOTE_URL);
    }


    @Test(groups = "unitTest")
    public void normalizeInfoLinkSearch() {
        // given
        final InfoLink infoLink = new InfoLink("https://myanimelist.net/anime/1535/Death_Note?q=death%20note");

        // when
        final InfoLink result = MyAnimeListNetUtil.normalizeInfoLink(infoLink);

        // then
        assertEquals(result.getUrl(), EXPECTED_DEATH_NOTE_URL);
    }


    @Test(groups = "unitTest")
    public void normalizeInfoLinkDefault() {
        // given
        final InfoLink infoLink = new InfoLink("https://myanimelist.net/anime/1535/Death_Note");

        // when
        final InfoLink result = MyAnimeListNetUtil.normalizeInfoLink(infoLink);

        // then
        assertEquals(result.getUrl(), EXPECTED_DEATH_NOTE_URL);
    }


    @Test(groups = "unitTest", description = "If you get the expected url already it is being returned as is, but with http instead of https.")
    public void normalizeInfoLinkIdentical() {
        // given
        final InfoLink infoLink = new InfoLink("https://myanimelist.net/anime/1535/Death_Note");

        // when
        final InfoLink result = MyAnimeListNetUtil.normalizeInfoLink(infoLink);

        // then
        assertEquals(result.getUrl(), EXPECTED_DEATH_NOTE_URL);
    }


    @Test(groups = "unitTest", description = "If the url does not match the expected pattern it is being returned unchanged.")
    public void normalizeInfoLinkDifferentPattern() {
        // given
        final InfoLink infoLink = new InfoLink(" https://myanimelist.net/news?_location=mal_h_m");

        // when
        final InfoLink result = MyAnimeListNetUtil.normalizeInfoLink(infoLink);

        // then
        assertEquals(result, infoLink);
    }
}
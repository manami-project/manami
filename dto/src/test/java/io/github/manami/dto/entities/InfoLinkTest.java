package io.github.manami.dto.entities;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class InfoLinkTest {

    @Test
    public void testIsPresentNull() {
        // given
        final InfoLink infoLink = new InfoLink(null);

        // when
        final boolean result = infoLink.isPresent();

        // then
        assertFalse(result);
    }


    @Test
    public void testIsPresentEmptyString() {
        // given
        final InfoLink infoLink = new InfoLink(" ");

        // when
        final boolean result = infoLink.isPresent();

        // then
        assertFalse(result);
    }


    @Test
    public void testIsPresentTrue() {
        // given
        final InfoLink infoLink = new InfoLink("https://myanimelist.net/anime/1535");

        // when
        final boolean result = infoLink.isPresent();

        // then
        assertTrue(result);
    }


    @Test
    public void testToString() {
        // given
        final String url = "https://myanimelist.net/anime/1535";
        final InfoLink infoLink = new InfoLink(url);

        // when
        final String result = infoLink.toString();

        // then
        assertEquals(result, url);
    }


    @Test
    public void testGetUrl() {
        // given
        final String url = "https://myanimelist.net/anime/1535";
        final InfoLink infoLink = new InfoLink(url);

        // when
        final String result = infoLink.getUrl();

        // then
        assertEquals(result, url);
    }


    @Test
    public void testIsValidNull() {
        // given
        final InfoLink infoLink = new InfoLink(null);

        // when
        final boolean result = infoLink.isValid();

        // then
        assertFalse(result);
    }


    @Test
    public void testIsValidEmptyString() {
        // given
        final InfoLink infoLink = new InfoLink(" ");

        // when
        final boolean result = infoLink.isValid();

        // then
        assertFalse(result);
    }


    @Test
    public void testIsValidTrue() {
        // given
        final InfoLink infoLink = new InfoLink("https://myanimelist.net/anime/1535");

        // when
        final boolean result = infoLink.isValid();

        // then
        assertTrue(result);
    }


    @Test
    public void testEqualsNull() {
        // given
        final InfoLink infoLink = new InfoLink("https://myanimelist.net/anime/1535");

        // when
        final boolean result = infoLink.equals(null);

        // then
        assertFalse(result);
    }


    @Test
    public void testEqualsDifferentType() {
        // given
        final InfoLink infoLink = new InfoLink("https://myanimelist.net/anime/1535");

        // when
        final boolean result = infoLink.equals(Integer.parseInt("2"));

        // then
        assertFalse(result);
    }


    @Test
    public void testEqualsSameInstance() {
        // given
        final InfoLink infoLink = new InfoLink("https://myanimelist.net/anime/1535");

        // when
        final boolean result = infoLink.equals(infoLink);

        // then
        assertTrue(result);
    }


    @Test
    public void testEqualsDifferentInstanceSameUrl() {
        // given
        final String url = "https://myanimelist.net/anime/1535";
        final InfoLink infoLinkA = new InfoLink(url);
        final InfoLink infoLinkB = new InfoLink(url);

        // when
        final boolean result = infoLinkA.equals(infoLinkB);

        // then
        assertTrue(result);
    }


    @Test
    public void testEqualsDifferentInstanceDifferentUrl() {
        // given
        final InfoLink infoLinkA = new InfoLink("https://myanimelist.net/anime/1535");
        final InfoLink infoLinkB = new InfoLink("https://myanimelist.net/anime/15");

        // when
        final boolean result = infoLinkA.equals(infoLinkB);

        // then
        assertFalse(result);
    }


    @Test
    public void testEqualOneEntryIsNotPresent() {
        // given
        final InfoLink infoLinkA = new InfoLink("https://myanimelist.net/anime/1535");
        final InfoLink infoLinkB = new InfoLink("   ");

        // when
        final boolean result = infoLinkA.equals(infoLinkB);

        // then
        assertFalse(result);
    }


    @Test
    public void testEqualOtherEntryIsNotPresent() {
        // given
        final InfoLink infoLinkA = new InfoLink("   ");
        final InfoLink infoLinkB = new InfoLink("https://myanimelist.net/anime/1535");

        // when
        final boolean result = infoLinkA.equals(infoLinkB);

        // then
        assertFalse(result);
    }
}

package io.github.manami.dto.events;

import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.WatchListEntry;

public class SearchResultEventTest {

    @Test(groups = UNIT_TEST_GROUP)
    public void testConstructor() {
        // given
        final String searchString = "Death Note";

        // when
        final SearchResultEvent sut = new SearchResultEvent(searchString);

        // then
        assertNotNull(sut);

        assertNotNull(sut.getAnimeListSearchResultList());
        assertTrue(sut.getAnimeListSearchResultList().isEmpty());

        assertNotNull(sut.getFilterListSearchResultList());
        assertTrue(sut.getFilterListSearchResultList().isEmpty());

        assertNotNull(sut.getWatchListSearchResultList());
        assertTrue(sut.getWatchListSearchResultList().isEmpty());

        assertEquals(sut.getSearchString(), searchString);
    }


    @Test(groups = UNIT_TEST_GROUP, expectedExceptions = UnsupportedOperationException.class)
    public void testAnimeListUnmodifiable() {
        // given
        final String searchString = "Death Note";
        final SearchResultEvent sut = new SearchResultEvent(searchString);
        final Anime anime = new Anime();

        // when
        sut.getAnimeListSearchResultList().add(anime);

        // then
    }


    @Test(groups = UNIT_TEST_GROUP, expectedExceptions = UnsupportedOperationException.class)
    public void testFilterListUnmodifiable() {
        // given
        final String searchString = "Death Note";
        final SearchResultEvent sut = new SearchResultEvent(searchString);
        final FilterEntry entry = new FilterEntry(searchString, new InfoLink("http://myanimelist.net/anime/1535"));

        // when
        sut.getFilterListSearchResultList().add(entry);

        // then
    }


    @Test(groups = UNIT_TEST_GROUP, expectedExceptions = UnsupportedOperationException.class)
    public void testWatchListUnmodifiable() {
        // given
        final String searchString = "Death Note";
        final SearchResultEvent sut = new SearchResultEvent(searchString);
        final WatchListEntry entry = new WatchListEntry(searchString, new InfoLink("http://myanimelist.net/anime/1535"));

        // when
        sut.getWatchListSearchResultList().add(entry);

        // then
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testAddingEntryToAnimeList() {
        // given
        final String searchString = "Death Note";
        final Anime anime = new Anime();
        final SearchResultEvent sut = new SearchResultEvent(searchString);

        // when
        sut.addAnimeListSearchResult(anime);

        // then
        assertEquals(sut.getAnimeListSearchResultList().size(), 1);
        assertEquals(sut.getAnimeListSearchResultList().get(0), anime);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testAddingNullToAnimeList() {
        // given
        final String searchString = "Death Note";
        final SearchResultEvent sut = new SearchResultEvent(searchString);

        // when
        sut.addAnimeListSearchResult(null);

        // then
        assertTrue(sut.getAnimeListSearchResultList().isEmpty());
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testAddingEntryToFilterList() {
        // given
        final String searchString = "Death Note";
        final FilterEntry entry = new FilterEntry(searchString, new InfoLink("http://myanimelist.net/anime/1535"));
        final SearchResultEvent sut = new SearchResultEvent(searchString);

        // when
        sut.addFilterListSearchResult(entry);

        // then
        assertEquals(sut.getFilterListSearchResultList().size(), 1);
        assertEquals(sut.getFilterListSearchResultList().get(0), entry);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testAddingNullToFilterList() {
        // given
        final String searchString = "Death Note";
        final SearchResultEvent sut = new SearchResultEvent(searchString);

        // when
        sut.addFilterListSearchResult(null);

        // then
        assertTrue(sut.getFilterListSearchResultList().isEmpty());
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testAddingEntryToWatchList() {
        // given
        final String searchString = "Death Note";
        final WatchListEntry entry = new WatchListEntry(searchString, new InfoLink("http://myanimelist.net/anime/1535"));
        final SearchResultEvent sut = new SearchResultEvent(searchString);

        // when
        sut.addWatchListSearchResult(entry);

        // then
        assertEquals(sut.getWatchListSearchResultList().size(), 1);
        assertEquals(sut.getWatchListSearchResultList().get(0), entry);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testAddingNullToWatchList() {
        // given
        final String searchString = "Death Note";
        final SearchResultEvent sut = new SearchResultEvent(searchString);

        // when
        sut.addWatchListSearchResult(null);

        // then
        assertTrue(sut.getWatchListSearchResultList().isEmpty());
    }
}

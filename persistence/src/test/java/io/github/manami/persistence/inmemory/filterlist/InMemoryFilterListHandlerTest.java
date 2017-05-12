package io.github.manami.persistence.inmemory.filterlist;

import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.AbstractMinimalEntry;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.WatchListEntry;

public class InMemoryFilterListHandlerTest {

    private InMemoryFilterListHandler inMemoryFilterListHandler;


    @BeforeMethod
    public void setUp() throws IOException {
        inMemoryFilterListHandler = new InMemoryFilterListHandler();
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterAnimeIsNull() {
        // given

        // when
        final boolean result = inMemoryFilterListHandler.filterAnime(null);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryFilterListHandler.fetchFilterList().size(), 0);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterAnimeIsEntryWithoutTitle() {
        // given
        final FilterEntry entry = new FilterEntry(EMPTY, new InfoLink("http://myanimelist.net/anime/1535"));

        // when
        final boolean result = inMemoryFilterListHandler.filterAnime(entry);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryFilterListHandler.fetchFilterList().size(), 0);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterAnimeIsEntryWithoutInfoLink() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", null);

        // when
        final boolean result = inMemoryFilterListHandler.filterAnime(entry);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryFilterListHandler.fetchFilterList().size(), 0);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterAnimeIsEntryWithoutThumbnail() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));

        // when
        final boolean result = inMemoryFilterListHandler.filterAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryFilterListHandler.fetchFilterList().size(), 1);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterAnimeIsFullEntry() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", new InfoLink("http://myanimelist.net/anime/1535"));

        // when
        final boolean result = inMemoryFilterListHandler.filterAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryFilterListHandler.fetchFilterList().size(), 1);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterEntryExists() {
        // given
        final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");
        final FilterEntry entry = new FilterEntry("Death Note", infoLink);
        inMemoryFilterListHandler.filterAnime(entry);

        // when
        final boolean result = inMemoryFilterListHandler.filterEntryExists(infoLink);

        // then
        assertEquals(result, true);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterEntryNotExists() {
        // given

        // when
        final boolean result = inMemoryFilterListHandler.filterEntryExists(new InfoLink("http://myanimelist.net/anime/1535"));

        // then
        assertEquals(result, false);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterAnimeList() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));
        inMemoryFilterListHandler.filterAnime(entry);

        // when
        final List<FilterEntry> fetchFilterList = inMemoryFilterListHandler.fetchFilterList();

        // then
        assertEquals(fetchFilterList.size(), 1);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testRemoveFromFilterListWorks() {
        // given
        final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");
        final FilterEntry entry = new FilterEntry("Death Note", infoLink);
        inMemoryFilterListHandler.filterAnime(entry);

        // when
        final boolean result = inMemoryFilterListHandler.removeFromFilterList(infoLink);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryFilterListHandler.fetchFilterList().isEmpty(), true);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testRemoveFromFilterListNullAsArgument() {
        // given

        // when
        final boolean result = inMemoryFilterListHandler.removeFromFilterList(null);

        // then
        assertEquals(result, false);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testUpdateOrCreateWithNull() {
        // given

        // when
        inMemoryFilterListHandler.updateOrCreate(null);

        // then
        assertEquals(inMemoryFilterListHandler.fetchFilterList().isEmpty(), true);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testUpdateOrCreateForNewFilterEntry() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", new InfoLink("http://myanimelist.net/anime/1535"));

        // when
        inMemoryFilterListHandler.updateOrCreate(entry);

        // then
        assertEquals(inMemoryFilterListHandler.fetchFilterList().isEmpty(), false);
        assertEquals(inMemoryFilterListHandler.fetchFilterList().get(0), entry);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testUpdateOrCreateForModifiedFilterEntry() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", AbstractMinimalEntry.NO_IMG_THUMB, new InfoLink("http://myanimelist.net/anime/1535"));

        inMemoryFilterListHandler.filterAnime(entry);

        final String thumbnail = "http://cdn.myanimelist.net/images/anime/9/9453t.jpg";
        entry.setThumbnail(thumbnail);

        // when
        inMemoryFilterListHandler.updateOrCreate(entry);

        // then
        assertEquals(inMemoryFilterListHandler.fetchFilterList().isEmpty(), false);
        assertEquals(inMemoryFilterListHandler.fetchFilterList().get(0).getThumbnail(), thumbnail);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testClearing() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", new InfoLink("http://myanimelist.net/anime/1535"));
        inMemoryFilterListHandler.filterAnime(entry);

        // when
        inMemoryFilterListHandler.clear();

        // then
        assertEquals(inMemoryFilterListHandler.fetchFilterList().isEmpty(), true);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterEntryNotAddedBecauseItAlreadyExists() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", new InfoLink("http://myanimelist.net/anime/1535"));
        inMemoryFilterListHandler.filterAnime(entry);

        // when
        final boolean result = inMemoryFilterListHandler.filterAnime(entry);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryFilterListHandler.fetchFilterList().size(), 1);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterWatchListEntry() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", new InfoLink("http://myanimelist.net/anime/1535"));

        // when
        final boolean result = inMemoryFilterListHandler.filterAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryFilterListHandler.fetchFilterList().size(), 1);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterAnime() {
        // given
        final Anime entry = new Anime("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));
        entry.setEpisodes(37);
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = inMemoryFilterListHandler.filterAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryFilterListHandler.fetchFilterList().size(), 1);
    }
}

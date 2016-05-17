package io.github.manami.persistence.inmemory.filterlist;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.AbstractMinimalEntry;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.WatchListEntry;

public class InMemoryFilterListHandlerTest {

    private InMemoryFilterListHandler inMemoryFilterListHandler;


    @Before
    public void setUp() throws IOException {
        inMemoryFilterListHandler = new InMemoryFilterListHandler();
    }


    @Test
    public void testFilterAnimeIsNull() {
        // given

        // when
        final boolean result = inMemoryFilterListHandler.filterAnime(null);

        // then
        assertThat(result, equalTo(false));
        assertThat(inMemoryFilterListHandler.fetchFilterList().size(), equalTo(0));
    }


    @Test
    public void testFilterAnimeIsEntryWithoutTitle() {
        // given
        final FilterEntry entry = new FilterEntry("", "http://myanimelist.net/anime/1535");

        // when
        final boolean result = inMemoryFilterListHandler.filterAnime(entry);

        // then
        assertThat(result, equalTo(false));
        assertThat(inMemoryFilterListHandler.fetchFilterList().size(), equalTo(0));
    }


    @Test
    public void testFilterAnimeIsEntryWithoutInfoLink() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "");

        // when
        final boolean result = inMemoryFilterListHandler.filterAnime(entry);

        // then
        assertThat(result, equalTo(false));
        assertThat(inMemoryFilterListHandler.fetchFilterList().size(), equalTo(0));
    }


    @Test
    public void testFilterAnimeIsEntryWithoutThumbnail() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "http://myanimelist.net/anime/1535");

        // when
        final boolean result = inMemoryFilterListHandler.filterAnime(entry);

        // then
        assertThat(result, equalTo(true));
        assertThat(inMemoryFilterListHandler.fetchFilterList().size(), equalTo(1));
    }


    @Test
    public void testFilterAnimeIsFullEntry() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");

        // when
        final boolean result = inMemoryFilterListHandler.filterAnime(entry);

        // then
        assertThat(result, equalTo(true));
        assertThat(inMemoryFilterListHandler.fetchFilterList().size(), equalTo(1));
    }


    @Test
    public void testFilterEntryExists() {
        // given
        final String infoLink = "http://myanimelist.net/anime/1535";
        final FilterEntry entry = new FilterEntry("Death Note", infoLink);
        inMemoryFilterListHandler.filterAnime(entry);

        // when
        final boolean result = inMemoryFilterListHandler.filterEntryExists(infoLink);

        // then
        assertThat(result, equalTo(true));
    }


    @Test
    public void testFilterEntryNotExists() {
        // given

        // when
        final boolean result = inMemoryFilterListHandler.filterEntryExists("http://myanimelist.net/anime/1535");

        // then
        assertThat(result, equalTo(false));
    }


    @Test
    public void testFilterAnimeList() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "http://myanimelist.net/anime/1535");
        inMemoryFilterListHandler.filterAnime(entry);

        // when
        final List<FilterEntry> fetchFilterList = inMemoryFilterListHandler.fetchFilterList();

        // then
        assertThat(fetchFilterList.size(), equalTo(1));
    }


    @Test
    public void testRemoveFromFilterListWorks() {
        // given
        final String infoLink = "http://myanimelist.net/anime/1535";
        final FilterEntry entry = new FilterEntry("Death Note", infoLink);
        inMemoryFilterListHandler.filterAnime(entry);

        // when
        final boolean result = inMemoryFilterListHandler.removeFromFilterList(infoLink);

        // then
        assertThat(result, equalTo(true));
        assertThat(inMemoryFilterListHandler.fetchFilterList().isEmpty(), equalTo(true));
    }


    @Test
    public void testRemoveFromFilterListNullAsArgument() {
        // given

        // when
        final boolean result = inMemoryFilterListHandler.removeFromFilterList(null);

        // then
        assertThat(result, equalTo(false));
    }


    @Test
    public void testUpdateOrCreateWithNull() {
        // given

        // when
        inMemoryFilterListHandler.updateOrCreate(null);

        // then
        assertThat(inMemoryFilterListHandler.fetchFilterList().isEmpty(), equalTo(true));
    }


    @Test
    public void testUpdateOrCreateForNewFilterEntry() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");

        // when
        inMemoryFilterListHandler.updateOrCreate(entry);

        // then
        assertThat(inMemoryFilterListHandler.fetchFilterList().isEmpty(), equalTo(false));
        assertThat(inMemoryFilterListHandler.fetchFilterList().get(0), equalTo(entry));
    }


    @Test
    public void testUpdateOrCreateForModifiedFilterEntry() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", AbstractMinimalEntry.NO_IMG_THUMB, "http://myanimelist.net/anime/1535");

        inMemoryFilterListHandler.filterAnime(entry);

        final String thumbnail = "http://cdn.myanimelist.net/images/anime/9/9453t.jpg";
        entry.setThumbnail(thumbnail);

        // when
        inMemoryFilterListHandler.updateOrCreate(entry);

        // then
        assertThat(inMemoryFilterListHandler.fetchFilterList().isEmpty(), equalTo(false));
        assertThat(inMemoryFilterListHandler.fetchFilterList().get(0).getThumbnail(), equalTo(thumbnail));
    }


    @Test
    public void testClearing() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");
        inMemoryFilterListHandler.filterAnime(entry);

        // when
        inMemoryFilterListHandler.clear();

        // then
        assertThat(inMemoryFilterListHandler.fetchFilterList().isEmpty(), equalTo(true));
    }


    @Test
    public void testFilterEntryNotAddedBecauseItAlreadyExists() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");
        inMemoryFilterListHandler.filterAnime(entry);

        // when
        final boolean result = inMemoryFilterListHandler.filterAnime(entry);

        // then
        assertThat(result, equalTo(false));
        assertThat(inMemoryFilterListHandler.fetchFilterList().size(), equalTo(1));
    }


    @Test
    public void testFilterWatchListEntry() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");

        // when
        final boolean result = inMemoryFilterListHandler.filterAnime(entry);

        // then
        assertThat(result, equalTo(true));
        assertThat(inMemoryFilterListHandler.fetchFilterList().size(), equalTo(1));
    }


    @Test
    public void testFilterAnime() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = inMemoryFilterListHandler.filterAnime(entry);

        // then
        assertThat(result, equalTo(true));
        assertThat(inMemoryFilterListHandler.fetchFilterList().size(), equalTo(1));
    }
}

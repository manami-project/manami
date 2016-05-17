package io.github.manami.persistence.inmemory.watchlist;

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

public class InMemoryWatchListHandlerTest {

    private InMemoryWatchListHandler inMemoryWatchListHandler;


    @Before
    public void setUp() throws IOException {
        inMemoryWatchListHandler = new InMemoryWatchListHandler();
    }


    @Test
    public void testFetchWatchList() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", "http://myanimelist.net/anime/1535");
        inMemoryWatchListHandler.watchAnime(entry);

        // when
        final List<WatchListEntry> fetchWatchList = inMemoryWatchListHandler.fetchWatchList();

        // then
        assertThat(fetchWatchList.size(), equalTo(1));
    }


    @Test
    public void testWatchListEntryExists() {
        // given
        final String infoLink = "http://myanimelist.net/anime/1535";
        final WatchListEntry entry = new WatchListEntry("Death Note", infoLink);
        inMemoryWatchListHandler.watchAnime(entry);

        // when
        final boolean result = inMemoryWatchListHandler.watchListEntryExists(infoLink);

        // then
        assertThat(result, equalTo(true));
    }


    @Test
    public void testWatchListEntryNotExists() {
        // given

        // when
        final boolean result = inMemoryWatchListHandler.watchListEntryExists("http://myanimelist.net/anime/1535");

        // then
        assertThat(result, equalTo(false));
    }


    @Test
    public void testWatchAnimeIsNull() {
        // given

        // when
        final boolean result = inMemoryWatchListHandler.watchAnime(null);

        // then
        assertThat(result, equalTo(false));
        assertThat(inMemoryWatchListHandler.fetchWatchList().size(), equalTo(0));
    }


    @Test
    public void testWatchAnimeIsEntryWithoutTitle() {
        // given
        final WatchListEntry entry = new WatchListEntry("", "http://myanimelist.net/anime/1535");

        // when
        final boolean result = inMemoryWatchListHandler.watchAnime(entry);

        // then
        assertThat(result, equalTo(false));
        assertThat(inMemoryWatchListHandler.fetchWatchList().size(), equalTo(0));
    }


    @Test
    public void testWatchAnimeIsEntryWithoutInfoLink() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", "");

        // when
        final boolean result = inMemoryWatchListHandler.watchAnime(entry);

        // then
        assertThat(result, equalTo(false));
        assertThat(inMemoryWatchListHandler.fetchWatchList().size(), equalTo(0));
    }


    @Test
    public void testWatchAnimeIsEntryWithoutThumbnail() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", "http://myanimelist.net/anime/1535");

        // when
        final boolean result = inMemoryWatchListHandler.watchAnime(entry);

        // then
        assertThat(result, equalTo(true));
        assertThat(inMemoryWatchListHandler.fetchWatchList().size(), equalTo(1));
    }


    @Test
    public void testWatchAnimeIsFullEntry() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");

        // when
        final boolean result = inMemoryWatchListHandler.watchAnime(entry);

        // then
        assertThat(result, equalTo(true));
        assertThat(inMemoryWatchListHandler.fetchWatchList().size(), equalTo(1));
    }


    @Test
    public void testRemoveFromWatchListWorks() {
        // given
        final String infoLink = "http://myanimelist.net/anime/1535";
        final WatchListEntry entry = new WatchListEntry("Death Note", infoLink);
        inMemoryWatchListHandler.watchAnime(entry);

        // when
        final boolean result = inMemoryWatchListHandler.removeFromWatchList(infoLink);

        // then
        assertThat(result, equalTo(true));
        assertThat(inMemoryWatchListHandler.fetchWatchList().isEmpty(), equalTo(true));
    }


    @Test
    public void testRemoveFromWatchListNullAsArgument() {
        // given

        // when
        final boolean result = inMemoryWatchListHandler.removeFromWatchList(null);

        // then
        assertThat(result, equalTo(false));
    }


    @Test
    public void testUpdateOrCreateWithNull() {
        // given

        // when
        inMemoryWatchListHandler.updateOrCreate(null);

        // then
        assertThat(inMemoryWatchListHandler.fetchWatchList().isEmpty(), equalTo(true));
    }


    @Test
    public void testUpdateOrCreateForNewWatchListEntry() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");

        // when
        inMemoryWatchListHandler.updateOrCreate(entry);

        // then
        assertThat(inMemoryWatchListHandler.fetchWatchList().isEmpty(), equalTo(false));
        assertThat(inMemoryWatchListHandler.fetchWatchList().get(0), equalTo(entry));
    }


    @Test
    public void testUpdateOrCreateForModifiedWatchListEntry() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", AbstractMinimalEntry.NO_IMG_THUMB, "http://myanimelist.net/anime/1535");

        inMemoryWatchListHandler.watchAnime(entry);

        final String thumbnail = "http://cdn.myanimelist.net/images/anime/9/9453t.jpg";
        entry.setThumbnail(thumbnail);

        // when
        inMemoryWatchListHandler.updateOrCreate(entry);

        // then
        assertThat(inMemoryWatchListHandler.fetchWatchList().isEmpty(), equalTo(false));
        assertThat(inMemoryWatchListHandler.fetchWatchList().get(0).getThumbnail(), equalTo(thumbnail));
    }


    @Test
    public void testClearing() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");
        inMemoryWatchListHandler.watchAnime(entry);

        // when
        inMemoryWatchListHandler.clear();

        // then
        assertThat(inMemoryWatchListHandler.fetchWatchList().isEmpty(), equalTo(true));
    }


    @Test
    public void testWatchListEntryNotAddedBecauseItAlreadyExists() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");
        inMemoryWatchListHandler.watchAnime(entry);

        // when
        final boolean result = inMemoryWatchListHandler.watchAnime(entry);

        // then
        assertThat(result, equalTo(false));
        assertThat(inMemoryWatchListHandler.fetchWatchList().size(), equalTo(1));
    }


    @Test
    public void testWatchFilterEntry() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");

        // when
        final boolean result = inMemoryWatchListHandler.watchAnime(entry);

        // then
        assertThat(result, equalTo(true));
        assertThat(inMemoryWatchListHandler.fetchWatchList().size(), equalTo(1));
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
        final boolean result = inMemoryWatchListHandler.watchAnime(entry);

        // then
        assertThat(result, equalTo(true));
        assertThat(inMemoryWatchListHandler.fetchWatchList().size(), equalTo(1));
    }
}

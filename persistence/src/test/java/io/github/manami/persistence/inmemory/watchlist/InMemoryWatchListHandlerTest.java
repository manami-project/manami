package io.github.manami.persistence.inmemory.watchlist;

import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.AbstractMinimalEntry;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.WatchListEntry;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class InMemoryWatchListHandlerTest {

    private InMemoryWatchListHandler inMemoryWatchListHandler;


    @BeforeMethod
    public void setUp() throws IOException {
        inMemoryWatchListHandler = new InMemoryWatchListHandler();
    }


    @Test(groups = "unitTest")
    public void testFetchWatchList() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));
        inMemoryWatchListHandler.watchAnime(entry);

        // when
        final List<WatchListEntry> fetchWatchList = inMemoryWatchListHandler.fetchWatchList();

        // then
        assertEquals(fetchWatchList.size(), 1);
    }


    @Test(groups = "unitTest")
    public void testWatchListEntryExists() {
        // given
        final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");
        final WatchListEntry entry = new WatchListEntry("Death Note", infoLink);
        inMemoryWatchListHandler.watchAnime(entry);

        // when
        final boolean result = inMemoryWatchListHandler.watchListEntryExists(infoLink);

        // then
        assertEquals(result, true);
    }


    @Test(groups = "unitTest")
    public void testWatchListEntryNotExists() {
        // given

        // when
        final boolean result = inMemoryWatchListHandler.watchListEntryExists(new InfoLink("http://myanimelist.net/anime/1535"));

        // then
        assertEquals(result, false);
    }


    @Test(groups = "unitTest")
    public void testWatchAnimeIsNull() {
        // given

        // when
        final boolean result = inMemoryWatchListHandler.watchAnime(null);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryWatchListHandler.fetchWatchList().size(), 0);
    }


    @Test(groups = "unitTest")
    public void testWatchAnimeIsEntryWithoutTitle() {
        // given
        final WatchListEntry entry = new WatchListEntry("", new InfoLink("http://myanimelist.net/anime/1535"));

        // when
        final boolean result = inMemoryWatchListHandler.watchAnime(entry);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryWatchListHandler.fetchWatchList().size(), 0);
    }


    @Test(groups = "unitTest")
    public void testWatchAnimeIsEntryWithoutInfoLink() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", null);

        // when
        final boolean result = inMemoryWatchListHandler.watchAnime(entry);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryWatchListHandler.fetchWatchList().size(), 0);
    }


    @Test(groups = "unitTest")
    public void testWatchAnimeIsEntryWithoutThumbnail() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));

        // when
        final boolean result = inMemoryWatchListHandler.watchAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryWatchListHandler.fetchWatchList().size(), 1);
    }


    @Test(groups = "unitTest")
    public void testWatchAnimeIsFullEntry() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", new InfoLink("http://myanimelist.net/anime/1535"));

        // when
        final boolean result = inMemoryWatchListHandler.watchAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryWatchListHandler.fetchWatchList().size(), 1);
    }


    @Test(groups = "unitTest")
    public void testRemoveFromWatchListWorks() {
        // given
        final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");
        final WatchListEntry entry = new WatchListEntry("Death Note", infoLink);
        inMemoryWatchListHandler.watchAnime(entry);

        // when
        final boolean result = inMemoryWatchListHandler.removeFromWatchList(infoLink);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryWatchListHandler.fetchWatchList().isEmpty(), true);
    }


    @Test(groups = "unitTest")
    public void testRemoveFromWatchListNullAsArgument() {
        // given

        // when
        final boolean result = inMemoryWatchListHandler.removeFromWatchList(null);

        // then
        assertEquals(result, false);
    }


    @Test(groups = "unitTest")
    public void testUpdateOrCreateWithNull() {
        // given

        // when
        inMemoryWatchListHandler.updateOrCreate(null);

        // then
        assertEquals(inMemoryWatchListHandler.fetchWatchList().isEmpty(), true);
    }


    @Test(groups = "unitTest")
    public void testUpdateOrCreateForNewWatchListEntry() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", new InfoLink("http://myanimelist.net/anime/1535"));

        // when
        inMemoryWatchListHandler.updateOrCreate(entry);

        // then
        assertEquals(inMemoryWatchListHandler.fetchWatchList().isEmpty(), false);
        assertEquals(inMemoryWatchListHandler.fetchWatchList().get(0), entry);
    }


    @Test(groups = "unitTest")
    public void testUpdateOrCreateForModifiedWatchListEntry() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", AbstractMinimalEntry.NO_IMG_THUMB, new InfoLink("http://myanimelist.net/anime/1535"));

        inMemoryWatchListHandler.watchAnime(entry);

        final String thumbnail = "http://cdn.myanimelist.net/images/anime/9/9453t.jpg";
        entry.setThumbnail(thumbnail);

        // when
        inMemoryWatchListHandler.updateOrCreate(entry);

        // then
        assertEquals(inMemoryWatchListHandler.fetchWatchList().isEmpty(), false);
        assertEquals(inMemoryWatchListHandler.fetchWatchList().get(0).getThumbnail(), thumbnail);
    }


    @Test(groups = "unitTest")
    public void testClearing() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", new InfoLink("http://myanimelist.net/anime/1535"));
        inMemoryWatchListHandler.watchAnime(entry);

        // when
        inMemoryWatchListHandler.clear();

        // then
        assertEquals(inMemoryWatchListHandler.fetchWatchList().isEmpty(), true);
    }


    @Test(groups = "unitTest")
    public void testWatchListEntryNotAddedBecauseItAlreadyExists() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", new InfoLink("http://myanimelist.net/anime/1535"));
        inMemoryWatchListHandler.watchAnime(entry);

        // when
        final boolean result = inMemoryWatchListHandler.watchAnime(entry);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryWatchListHandler.fetchWatchList().size(), 1);
    }


    @Test(groups = "unitTest")
    public void testWatchFilterEntry() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", new InfoLink("http://myanimelist.net/anime/1535"));

        // when
        final boolean result = inMemoryWatchListHandler.watchAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryWatchListHandler.fetchWatchList().size(), 1);
    }


    @Test(groups = "unitTest")
    public void testFilterAnime() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink(new InfoLink("http://myanimelist.net/anime/1535"));
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = inMemoryWatchListHandler.watchAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryWatchListHandler.fetchWatchList().size(), 1);
    }
}

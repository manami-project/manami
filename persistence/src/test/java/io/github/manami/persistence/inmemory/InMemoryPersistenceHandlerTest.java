package io.github.manami.persistence.inmemory;

import static com.google.common.collect.Lists.newArrayList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.AbstractMinimalEntry;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.persistence.inmemory.animelist.InMemoryAnimeListHandler;
import io.github.manami.persistence.inmemory.filterlist.InMemoryFilterListHandler;
import io.github.manami.persistence.inmemory.watchlist.InMemoryWatchListHandler;

public class InMemoryPersistenceHandlerTest {

    private InMemoryPersistenceHandler inMemoryPersistenceHandler;


    @BeforeMethod
    public void setUp() throws IOException {
        inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
    }


    @Test(groups = "unitTest")
    public void testFilterAnimeIsNull() {
        // given

        // when
        final boolean result = inMemoryPersistenceHandler.filterAnime(null);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().size(), 0);
    }


    @Test(groups = "unitTest")
    public void testFilterAnimeIsEntryWithoutTitle() {
        // given
        final FilterEntry entry = new FilterEntry("", "http://myanimelist.net/anime/1535");

        // when
        final boolean result = inMemoryPersistenceHandler.filterAnime(entry);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().size(), 0);
    }


    @Test(groups = "unitTest")
    public void testFilterAnimeIsEntryWithoutInfoLink() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "");

        // when
        final boolean result = inMemoryPersistenceHandler.filterAnime(entry);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().size(), 0);
    }


    @Test(groups = "unitTest")
    public void testFilterAnimeIsEntryWithoutThumbnail() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "http://myanimelist.net/anime/1535");

        // when
        final boolean result = inMemoryPersistenceHandler.filterAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().size(), 1);
    }


    @Test(groups = "unitTest")
    public void testFilterAnimeIsFullEntry() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");

        // when
        final boolean result = inMemoryPersistenceHandler.filterAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().size(), 1);
    }


    @Test(groups = "unitTest")
    public void testFilterEntryExists() {
        // given
        final String infoLink = "http://myanimelist.net/anime/1535";
        final FilterEntry entry = new FilterEntry("Death Note", infoLink);
        inMemoryPersistenceHandler.filterAnime(entry);

        // when
        final boolean result = inMemoryPersistenceHandler.filterEntryExists(infoLink);

        // then
        assertEquals(result, true);
    }


    @Test(groups = "unitTest")
    public void testFilterEntryNotExists() {
        // given

        // when
        final boolean result = inMemoryPersistenceHandler.filterEntryExists("http://myanimelist.net/anime/1535");

        // then
        assertEquals(result, false);
    }


    @Test(groups = "unitTest")
    public void testFilterAnimeList() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "http://myanimelist.net/anime/1535");
        inMemoryPersistenceHandler.filterAnime(entry);

        // when
        final List<FilterEntry> fetchFilterList = inMemoryPersistenceHandler.fetchFilterList();

        // then
        assertEquals(fetchFilterList.size(), 1);
    }


    @Test(groups = "unitTest")
    public void testFetchWatchList() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", "http://myanimelist.net/anime/1535");
        inMemoryPersistenceHandler.watchAnime(entry);

        // when
        final List<WatchListEntry> fetchWatchList = inMemoryPersistenceHandler.fetchWatchList();

        // then
        assertEquals(fetchWatchList.size(), 1);
    }


    @Test(groups = "unitTest")
    public void testRemoveFromFilterListWorks() {
        // given
        final String infoLink = "http://myanimelist.net/anime/1535";
        final FilterEntry entry = new FilterEntry("Death Note", infoLink);
        inMemoryPersistenceHandler.filterAnime(entry);

        // when
        final boolean result = inMemoryPersistenceHandler.removeFromFilterList(infoLink);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().isEmpty(), true);
    }


    @Test(groups = "unitTest")
    public void testRemoveFromFilterListNullAsArgument() {
        // given

        // when
        final boolean result = inMemoryPersistenceHandler.removeFromFilterList(null);

        // then
        assertEquals(result, false);
    }


    @Test(groups = "unitTest")
    public void testWatchListEntryExists() {
        // given
        final String infoLink = "http://myanimelist.net/anime/1535";
        final WatchListEntry entry = new WatchListEntry("Death Note", infoLink);
        inMemoryPersistenceHandler.watchAnime(entry);

        // when
        final boolean result = inMemoryPersistenceHandler.watchListEntryExists(infoLink);

        // then
        assertEquals(result, true);
    }


    @Test(groups = "unitTest")
    public void testWatchListEntryNotExists() {
        // given

        // when
        final boolean result = inMemoryPersistenceHandler.watchListEntryExists("http://myanimelist.net/anime/1535");

        // then
        assertEquals(result, false);
    }


    @Test(groups = "unitTest")
    public void testWatchAnimeIsNull() {
        // given

        // when
        final boolean result = inMemoryPersistenceHandler.watchAnime(null);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().size(), 0);
    }


    @Test(groups = "unitTest")
    public void testWatchAnimeIsEntryWithoutTitle() {
        // given
        final WatchListEntry entry = new WatchListEntry("", "http://myanimelist.net/anime/1535");

        // when
        final boolean result = inMemoryPersistenceHandler.watchAnime(entry);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().size(), 0);
    }


    @Test(groups = "unitTest")
    public void testWatchAnimeIsEntryWithoutInfoLink() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", "");

        // when
        final boolean result = inMemoryPersistenceHandler.watchAnime(entry);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().size(), 0);
    }


    @Test(groups = "unitTest")
    public void testWatchAnimeIsEntryWithoutThumbnail() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", "http://myanimelist.net/anime/1535");

        // when
        final boolean result = inMemoryPersistenceHandler.watchAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().size(), 1);
    }


    @Test(groups = "unitTest")
    public void testWatchAnimeIsFullEntry() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");

        // when
        final boolean result = inMemoryPersistenceHandler.watchAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().size(), 1);
    }


    @Test(groups = "unitTest")
    public void testRemoveFromWatchListWorks() {
        // given
        final String infoLink = "http://myanimelist.net/anime/1535";
        final WatchListEntry entry = new WatchListEntry("Death Note", infoLink);
        inMemoryPersistenceHandler.watchAnime(entry);

        // when
        final boolean result = inMemoryPersistenceHandler.removeFromWatchList(infoLink);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().isEmpty(), true);
    }


    @Test(groups = "unitTest")
    public void testRemoveFromWatchListNullAsArgument() {
        // given

        // when
        final boolean result = inMemoryPersistenceHandler.removeFromWatchList(null);

        // then
        assertEquals(result, false);
    }


    @Test(groups = "unitTest")
    public void testaddAnimeIsNull() {
        // given

        // when
        final boolean result = inMemoryPersistenceHandler.addAnime(null);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().size(), 0);
    }


    @Test(groups = "unitTest")
    public void testaddAnimeIsFullEntry() {
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
        final boolean result = inMemoryPersistenceHandler.addAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().size(), 1);
    }


    @Test(groups = "unitTest")
    public void testaddAnimeIsEntryWithoutEpisodes() {
        // given
        final Anime entry = new Anime();
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = inMemoryPersistenceHandler.addAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().size(), 1);
    }


    @Test(groups = "unitTest")
    public void testaddAnimeIsEntryWithoutInfoLink() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = inMemoryPersistenceHandler.addAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().size(), 1);
    }


    @Test(groups = "unitTest")
    public void testaddAnimeIsEntryWithoutLocation() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = inMemoryPersistenceHandler.addAnime(entry);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().size(), 0);
    }


    @Test(groups = "unitTest")
    public void testaddAnimeIsEntryWithoutPicture() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = inMemoryPersistenceHandler.addAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().size(), 1);
    }


    @Test(groups = "unitTest")
    public void testaddAnimeIsEntryWithoutThumbnail() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = inMemoryPersistenceHandler.addAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().size(), 1);
    }


    @Test(groups = "unitTest")
    public void testaddAnimeIsEntryWithoutTitle() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = inMemoryPersistenceHandler.addAnime(entry);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().size(), 0);
    }


    @Test(groups = "unitTest")
    public void testaddAnimeIsEntryWithoutType() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");

        // when
        final boolean result = inMemoryPersistenceHandler.addAnime(entry);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().size(), 0);
    }


    @Test(groups = "unitTest")
    public void testAnimeEntryExists() {
        // given
        final String infoLink = "http://myanimelist.net/anime/1535";
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink(infoLink);
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);
        inMemoryPersistenceHandler.addAnime(entry);

        // when
        final boolean result = inMemoryPersistenceHandler.animeEntryExists(infoLink);

        // then
        assertEquals(result, true);
    }


    @Test(groups = "unitTest")
    public void testAnimeEntryNotExists() {
        // given

        // when
        final boolean result = inMemoryPersistenceHandler.animeEntryExists("http://myanimelist.net/anime/1535");

        // then
        assertEquals(result, false);
    }


    @Test(groups = "unitTest")
    public void testAnimeList() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);
        inMemoryPersistenceHandler.addAnime(entry);

        // when
        final List<Anime> animeList = inMemoryPersistenceHandler.fetchAnimeList();

        // then
        assertEquals(animeList.size(), 1);
    }


    @Test(groups = "unitTest")
    public void testRemoveFromAnimeListWorks() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);
        inMemoryPersistenceHandler.addAnime(entry);

        // when
        final boolean result = inMemoryPersistenceHandler.removeAnime(entry.getId());

        // then
        assertEquals(result, true);
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().isEmpty(), true);
    }


    @Test(groups = "unitTest")
    public void testRemoveFromAnimeListNullAsArgument() {
        // given

        // when
        final boolean result = inMemoryPersistenceHandler.removeAnime(null);

        // then
        assertEquals(result, false);
    }


    @Test(groups = "unitTest")
    public void testThatClearAllWorks() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);
        inMemoryPersistenceHandler.addAnime(entry);

        final FilterEntry filterEntry = new FilterEntry("Gintama", "http://cdn.myanimelist.net/images/anime/3/72078t.jpg", "http://myanimelist.net/anime/28977");
        inMemoryPersistenceHandler.filterAnime(filterEntry);

        final WatchListEntry watchEntry = new WatchListEntry("Steins;Gate", "http://cdn.myanimelist.net/images/anime/5/73199t.jpg", "http://myanimelist.net/anime/9253");
        inMemoryPersistenceHandler.watchAnime(watchEntry);

        // when
        inMemoryPersistenceHandler.clearAll();

        // then
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().isEmpty(), true);
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().isEmpty(), true);
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().isEmpty(), true);
    }


    @Test(groups = "unitTest")
    public void testAddFilterListWithNull() {
        // given

        // when
        inMemoryPersistenceHandler.addFilterList(null);

        // then
        assertNotNull(inMemoryPersistenceHandler.fetchFilterList());
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().isEmpty(), true);
    }


    @Test(groups = "unitTest")
    public void testThatAddFilterListWorks() {
        // given
        final List<FilterEntry> list = newArrayList();

        final FilterEntry entry = new FilterEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");
        list.add(entry);

        final FilterEntry gintama = new FilterEntry("Gintama", "http://cdn.myanimelist.net/images/anime/3/72078t.jpg", "http://myanimelist.net/anime/28977");
        list.add(gintama);

        final FilterEntry steinsGate = new FilterEntry("Steins;Gate", "http://cdn.myanimelist.net/images/anime/5/73199t.jpg", "http://myanimelist.net/anime/9253");
        list.add(steinsGate);

        // when
        inMemoryPersistenceHandler.addFilterList(list);

        // then
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().size(), list.size());
    }


    @Test(groups = "unitTest")
    public void testAddWatchListWithNull() {
        // given

        // when
        inMemoryPersistenceHandler.addWatchList(null);

        // then
        assertNotNull(inMemoryPersistenceHandler.fetchWatchList());
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().isEmpty(), true);
    }


    @Test(groups = "unitTest")
    public void testThatAddWatchListWorks() {
        // given
        final List<WatchListEntry> list = newArrayList();

        final WatchListEntry entry = new WatchListEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");
        list.add(entry);

        final WatchListEntry gintama = new WatchListEntry("Gintama", "http://cdn.myanimelist.net/images/anime/3/72078t.jpg", "http://myanimelist.net/anime/28977");
        list.add(gintama);

        final WatchListEntry steinsGate = new WatchListEntry("Steins;Gate", "http://cdn.myanimelist.net/images/anime/5/73199t.jpg", "http://myanimelist.net/anime/9253");
        list.add(steinsGate);

        // when
        inMemoryPersistenceHandler.addWatchList(list);

        // then
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().size(), list.size());
    }


    @Test(groups = "unitTest")
    public void testThatAddAnimeListWorks() {
        // given
        final List<Anime> list = newArrayList();

        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);
        list.add(entry);

        final Anime steinsGate = new Anime();
        steinsGate.setEpisodes(24);
        steinsGate.setInfoLink("http://myanimelist.net/anime/9253");
        steinsGate.setLocation("/steins_gate");
        steinsGate.setPicture("http://cdn.myanimelist.net/images/anime/5/73199.jpg");
        steinsGate.setThumbnail("http://cdn.myanimelist.net/images/anime/5/73199t.jpg");
        steinsGate.setTitle("Steins;Gate");
        steinsGate.setType(AnimeType.TV);
        list.add(steinsGate);

        // when
        inMemoryPersistenceHandler.addAnimeList(list);

        // then
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().size(), list.size());
    }


    @Test(groups = "unitTest")
    public void testAddAnimeListWithNull() {
        // given

        // when
        inMemoryPersistenceHandler.addAnimeList(null);

        // then
        assertNotNull(inMemoryPersistenceHandler.fetchAnimeList());
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().isEmpty(), true);
    }


    @Test(groups = "unitTest")
    public void testUpdateOrCreateWithNull() {
        // given

        // when
        inMemoryPersistenceHandler.updateOrCreate(null);

        // then
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().isEmpty(), true);
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().isEmpty(), true);
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().isEmpty(), true);
    }


    @Test(groups = "unitTest")
    public void testUpdateOrCreateForNewAnimeEntry() {
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
        inMemoryPersistenceHandler.updateOrCreate(entry);

        // then
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().isEmpty(), false);
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().get(0), entry);
    }


    @Test(groups = "unitTest")
    public void testUpdateOrCreateForModifiedAnimeEntry() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(35);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);

        inMemoryPersistenceHandler.addAnime(entry);

        final int episodes = 37;
        entry.setEpisodes(episodes);

        // when
        inMemoryPersistenceHandler.updateOrCreate(entry);

        // then
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().isEmpty(), false);
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().get(0).getEpisodes(), episodes);
    }


    @Test(groups = "unitTest")
    public void testUpdateOrCreateForNewFilterEntry() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");

        // when
        inMemoryPersistenceHandler.updateOrCreate(entry);

        // then
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().isEmpty(), false);
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().get(0), entry);
    }


    @Test(groups = "unitTest")
    public void testUpdateOrCreateForModifiedFilterEntry() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", AbstractMinimalEntry.NO_IMG_THUMB, "http://myanimelist.net/anime/1535");

        inMemoryPersistenceHandler.filterAnime(entry);

        final String thumbnail = "http://cdn.myanimelist.net/images/anime/9/9453t.jpg";
        entry.setThumbnail(thumbnail);

        // when
        inMemoryPersistenceHandler.updateOrCreate(entry);

        // then
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().isEmpty(), false);
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().get(0).getThumbnail(), thumbnail);
    }


    @Test(groups = "unitTest")
    public void testUpdateOrCreateForNewWatchListEntry() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");

        // when
        inMemoryPersistenceHandler.updateOrCreate(entry);

        // then
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().isEmpty(), false);
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().get(0), entry);
    }


    @Test(groups = "unitTest")
    public void testUpdateOrCreateForModifiedWatchListEntry() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", AbstractMinimalEntry.NO_IMG_THUMB, "http://myanimelist.net/anime/1535");

        inMemoryPersistenceHandler.watchAnime(entry);

        final String thumbnail = "http://cdn.myanimelist.net/images/anime/9/9453t.jpg";
        entry.setThumbnail(thumbnail);

        // when
        inMemoryPersistenceHandler.updateOrCreate(entry);

        // then
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().isEmpty(), false);
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().get(0).getThumbnail(), thumbnail);
    }
}

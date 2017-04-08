package io.github.manami.persistence.inmemory;

import static com.google.common.collect.Lists.newArrayList;
import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
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
import io.github.manami.dto.entities.InfoLink;
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


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterAnimeIsNull() {
        // given

        // when
        final boolean result = inMemoryPersistenceHandler.filterAnime(null);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().size(), 0);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterAnimeIsEntryWithoutTitle() {
        // given
        final FilterEntry entry = new FilterEntry("", new InfoLink("http://myanimelist.net/anime/1535"));

        // when
        final boolean result = inMemoryPersistenceHandler.filterAnime(entry);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().size(), 0);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterAnimeIsEntryWithoutInfoLink() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", new InfoLink(""));

        // when
        final boolean result = inMemoryPersistenceHandler.filterAnime(entry);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().size(), 0);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterAnimeIsEntryWithoutThumbnail() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));

        // when
        final boolean result = inMemoryPersistenceHandler.filterAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().size(), 1);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterAnimeIsFullEntry() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", new InfoLink("http://myanimelist.net/anime/1535"));

        // when
        final boolean result = inMemoryPersistenceHandler.filterAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().size(), 1);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterEntryExists() {
        // given
        final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");
        final FilterEntry entry = new FilterEntry("Death Note", infoLink);
        inMemoryPersistenceHandler.filterAnime(entry);

        // when
        final boolean result = inMemoryPersistenceHandler.filterEntryExists(infoLink);

        // then
        assertEquals(result, true);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterEntryNotExists() {
        // given

        // when
        final boolean result = inMemoryPersistenceHandler.filterEntryExists(new InfoLink("http://myanimelist.net/anime/1535"));

        // then
        assertEquals(result, false);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterAnimeList() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));
        inMemoryPersistenceHandler.filterAnime(entry);

        // when
        final List<FilterEntry> fetchFilterList = inMemoryPersistenceHandler.fetchFilterList();

        // then
        assertEquals(fetchFilterList.size(), 1);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFetchWatchList() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));
        inMemoryPersistenceHandler.watchAnime(entry);

        // when
        final List<WatchListEntry> fetchWatchList = inMemoryPersistenceHandler.fetchWatchList();

        // then
        assertEquals(fetchWatchList.size(), 1);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testRemoveFromFilterListWorks() {
        // given
        final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");
        final FilterEntry entry = new FilterEntry("Death Note", infoLink);
        inMemoryPersistenceHandler.filterAnime(entry);

        // when
        final boolean result = inMemoryPersistenceHandler.removeFromFilterList(infoLink);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().isEmpty(), true);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testRemoveFromFilterListNullAsArgument() {
        // given

        // when
        final boolean result = inMemoryPersistenceHandler.removeFromFilterList(null);

        // then
        assertEquals(result, false);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testWatchListEntryExists() {
        // given
        final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");
        final WatchListEntry entry = new WatchListEntry("Death Note", infoLink);
        inMemoryPersistenceHandler.watchAnime(entry);

        // when
        final boolean result = inMemoryPersistenceHandler.watchListEntryExists(infoLink);

        // then
        assertEquals(result, true);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testWatchListEntryNotExists() {
        // given

        // when
        final boolean result = inMemoryPersistenceHandler.watchListEntryExists(new InfoLink("http://myanimelist.net/anime/1535"));

        // then
        assertEquals(result, false);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testWatchAnimeIsNull() {
        // given

        // when
        final boolean result = inMemoryPersistenceHandler.watchAnime(null);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().size(), 0);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testWatchAnimeIsEntryWithoutTitle() {
        // given
        final WatchListEntry entry = new WatchListEntry("", new InfoLink("http://myanimelist.net/anime/1535"));

        // when
        final boolean result = inMemoryPersistenceHandler.watchAnime(entry);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().size(), 0);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testWatchAnimeIsEntryWithoutInfoLink() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", new InfoLink(""));

        // when
        final boolean result = inMemoryPersistenceHandler.watchAnime(entry);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().size(), 0);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testWatchAnimeIsEntryWithoutThumbnail() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));

        // when
        final boolean result = inMemoryPersistenceHandler.watchAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().size(), 1);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testWatchAnimeIsFullEntry() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", new InfoLink("http://myanimelist.net/anime/1535"));

        // when
        final boolean result = inMemoryPersistenceHandler.watchAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().size(), 1);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testRemoveFromWatchListWorks() {
        // given
        final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");
        final WatchListEntry entry = new WatchListEntry("Death Note", infoLink);
        inMemoryPersistenceHandler.watchAnime(entry);

        // when
        final boolean result = inMemoryPersistenceHandler.removeFromWatchList(infoLink);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().isEmpty(), true);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testRemoveFromWatchListNullAsArgument() {
        // given

        // when
        final boolean result = inMemoryPersistenceHandler.removeFromWatchList(null);

        // then
        assertEquals(result, false);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testaddAnimeIsNull() {
        // given

        // when
        final boolean result = inMemoryPersistenceHandler.addAnime(null);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().size(), 0);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testaddAnimeIsFullEntry() {
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
        final boolean result = inMemoryPersistenceHandler.addAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().size(), 1);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testaddAnimeIsEntryWithoutEpisodes() {
        // given
        final Anime entry = new Anime();
        entry.setInfoLink(new InfoLink("http://myanimelist.net/anime/1535"));
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


    @Test(groups = UNIT_TEST_GROUP)
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


    @Test(groups = UNIT_TEST_GROUP)
    public void testaddAnimeIsEntryWithoutLocation() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink(new InfoLink("http://myanimelist.net/anime/1535"));
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


    @Test(groups = UNIT_TEST_GROUP)
    public void testaddAnimeIsEntryWithoutPicture() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink(new InfoLink("http://myanimelist.net/anime/1535"));
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


    @Test(groups = UNIT_TEST_GROUP)
    public void testaddAnimeIsEntryWithoutThumbnail() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink(new InfoLink("http://myanimelist.net/anime/1535"));
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


    @Test(groups = UNIT_TEST_GROUP)
    public void testaddAnimeIsEntryWithoutTitle() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink(new InfoLink("http://myanimelist.net/anime/1535"));
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


    @Test(groups = UNIT_TEST_GROUP)
    public void testaddAnimeIsEntryWithoutType() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink(new InfoLink("http://myanimelist.net/anime/1535"));
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


    @Test(groups = UNIT_TEST_GROUP)
    public void testAnimeEntryExists() {
        // given
        final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");
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


    @Test(groups = UNIT_TEST_GROUP)
    public void testAnimeEntryNotExists() {
        // given

        // when
        final boolean result = inMemoryPersistenceHandler.animeEntryExists(new InfoLink("http://myanimelist.net/anime/1535"));

        // then
        assertEquals(result, false);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testAnimeList() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink(new InfoLink("http://myanimelist.net/anime/1535"));
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


    @Test(groups = UNIT_TEST_GROUP)
    public void testRemoveFromAnimeListWorks() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink(new InfoLink("http://myanimelist.net/anime/1535"));
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


    @Test(groups = UNIT_TEST_GROUP)
    public void testRemoveFromAnimeListNullAsArgument() {
        // given

        // when
        final boolean result = inMemoryPersistenceHandler.removeAnime(null);

        // then
        assertEquals(result, false);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testThatClearAllWorks() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink(new InfoLink("http://myanimelist.net/anime/1535"));
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);
        inMemoryPersistenceHandler.addAnime(entry);

        final FilterEntry filterEntry = new FilterEntry("Gintama", "http://cdn.myanimelist.net/images/anime/3/72078t.jpg", new InfoLink("http://myanimelist.net/anime/28977"));
        inMemoryPersistenceHandler.filterAnime(filterEntry);

        final WatchListEntry watchEntry = new WatchListEntry("Steins;Gate", "http://cdn.myanimelist.net/images/anime/5/73199t.jpg", new InfoLink("http://myanimelist.net/anime/9253"));
        inMemoryPersistenceHandler.watchAnime(watchEntry);

        // when
        inMemoryPersistenceHandler.clearAll();

        // then
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().isEmpty(), true);
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().isEmpty(), true);
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().isEmpty(), true);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testAddFilterListWithNull() {
        // given

        // when
        inMemoryPersistenceHandler.addFilterList(null);

        // then
        assertNotNull(inMemoryPersistenceHandler.fetchFilterList());
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().isEmpty(), true);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testThatAddFilterListWorks() {
        // given
        final List<FilterEntry> list = newArrayList();

        final FilterEntry entry = new FilterEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", new InfoLink("http://myanimelist.net/anime/1535"));
        list.add(entry);

        final FilterEntry gintama = new FilterEntry("Gintama", "http://cdn.myanimelist.net/images/anime/3/72078t.jpg", new InfoLink("http://myanimelist.net/anime/28977"));
        list.add(gintama);

        final FilterEntry steinsGate = new FilterEntry("Steins;Gate", "http://cdn.myanimelist.net/images/anime/5/73199t.jpg", new InfoLink("http://myanimelist.net/anime/9253"));
        list.add(steinsGate);

        // when
        inMemoryPersistenceHandler.addFilterList(list);

        // then
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().size(), list.size());
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testAddWatchListWithNull() {
        // given

        // when
        inMemoryPersistenceHandler.addWatchList(null);

        // then
        assertNotNull(inMemoryPersistenceHandler.fetchWatchList());
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().isEmpty(), true);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testThatAddWatchListWorks() {
        // given
        final List<WatchListEntry> list = newArrayList();

        final WatchListEntry entry = new WatchListEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", new InfoLink("http://myanimelist.net/anime/1535"));
        list.add(entry);

        final WatchListEntry gintama = new WatchListEntry("Gintama", "http://cdn.myanimelist.net/images/anime/3/72078t.jpg", new InfoLink("http://myanimelist.net/anime/28977"));
        list.add(gintama);

        final WatchListEntry steinsGate = new WatchListEntry("Steins;Gate", "http://cdn.myanimelist.net/images/anime/5/73199t.jpg", new InfoLink("http://myanimelist.net/anime/9253"));
        list.add(steinsGate);

        // when
        inMemoryPersistenceHandler.addWatchList(list);

        // then
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().size(), list.size());
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testThatAddAnimeListWorks() {
        // given
        final List<Anime> list = newArrayList();

        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink(new InfoLink("http://myanimelist.net/anime/1535"));
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);
        list.add(entry);

        final Anime steinsGate = new Anime();
        steinsGate.setEpisodes(24);
        steinsGate.setInfoLink(new InfoLink("http://myanimelist.net/anime/9253"));
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


    @Test(groups = UNIT_TEST_GROUP)
    public void testAddAnimeListWithNull() {
        // given

        // when
        inMemoryPersistenceHandler.addAnimeList(null);

        // then
        assertNotNull(inMemoryPersistenceHandler.fetchAnimeList());
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().isEmpty(), true);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testUpdateOrCreateWithNull() {
        // given

        // when
        inMemoryPersistenceHandler.updateOrCreate(null);

        // then
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().isEmpty(), true);
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().isEmpty(), true);
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().isEmpty(), true);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testUpdateOrCreateForNewAnimeEntry() {
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
        inMemoryPersistenceHandler.updateOrCreate(entry);

        // then
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().isEmpty(), false);
        assertEquals(inMemoryPersistenceHandler.fetchAnimeList().get(0), entry);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testUpdateOrCreateForModifiedAnimeEntry() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(35);
        entry.setInfoLink(new InfoLink("http://myanimelist.net/anime/1535"));
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


    @Test(groups = UNIT_TEST_GROUP)
    public void testUpdateOrCreateForNewFilterEntry() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", new InfoLink("http://myanimelist.net/anime/1535"));

        // when
        inMemoryPersistenceHandler.updateOrCreate(entry);

        // then
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().isEmpty(), false);
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().get(0), entry);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testUpdateOrCreateForModifiedFilterEntry() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", AbstractMinimalEntry.NO_IMG_THUMB, new InfoLink("http://myanimelist.net/anime/1535"));

        inMemoryPersistenceHandler.filterAnime(entry);

        final String thumbnail = "http://cdn.myanimelist.net/images/anime/9/9453t.jpg";
        entry.setThumbnail(thumbnail);

        // when
        inMemoryPersistenceHandler.updateOrCreate(entry);

        // then
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().isEmpty(), false);
        assertEquals(inMemoryPersistenceHandler.fetchFilterList().get(0).getThumbnail(), thumbnail);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testUpdateOrCreateForNewWatchListEntry() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", new InfoLink("http://myanimelist.net/anime/1535"));

        // when
        inMemoryPersistenceHandler.updateOrCreate(entry);

        // then
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().isEmpty(), false);
        assertEquals(inMemoryPersistenceHandler.fetchWatchList().get(0), entry);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testUpdateOrCreateForModifiedWatchListEntry() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", AbstractMinimalEntry.NO_IMG_THUMB, new InfoLink("http://myanimelist.net/anime/1535"));

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

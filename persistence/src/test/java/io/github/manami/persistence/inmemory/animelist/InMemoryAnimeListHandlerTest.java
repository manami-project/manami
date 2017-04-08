package io.github.manami.persistence.inmemory.animelist;

import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;

public class InMemoryAnimeListHandlerTest {

    private InMemoryAnimeListHandler inMemoryAnimeListHandler;


    @BeforeMethod
    public void setUp() throws IOException {
        inMemoryAnimeListHandler = new InMemoryAnimeListHandler();
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testaddAnimeIsNull() {
        // given

        // when
        final boolean result = inMemoryAnimeListHandler.addAnime(null);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryAnimeListHandler.fetchAnimeList().size(), 0);
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
        final boolean result = inMemoryAnimeListHandler.addAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryAnimeListHandler.fetchAnimeList().size(), 1);
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
        final boolean result = inMemoryAnimeListHandler.addAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryAnimeListHandler.fetchAnimeList().size(), 1);
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
        final boolean result = inMemoryAnimeListHandler.addAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryAnimeListHandler.fetchAnimeList().size(), 1);
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
        final boolean result = inMemoryAnimeListHandler.addAnime(entry);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryAnimeListHandler.fetchAnimeList().size(), 0);
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
        final boolean result = inMemoryAnimeListHandler.addAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryAnimeListHandler.fetchAnimeList().size(), 1);
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
        final boolean result = inMemoryAnimeListHandler.addAnime(entry);

        // then
        assertEquals(result, true);
        assertEquals(inMemoryAnimeListHandler.fetchAnimeList().size(), 1);
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
        final boolean result = inMemoryAnimeListHandler.addAnime(entry);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryAnimeListHandler.fetchAnimeList().size(), 0);
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
        final boolean result = inMemoryAnimeListHandler.addAnime(entry);

        // then
        assertEquals(result, false);
        assertEquals(inMemoryAnimeListHandler.fetchAnimeList().size(), 0);
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
        inMemoryAnimeListHandler.addAnime(entry);

        // when
        final boolean result = inMemoryAnimeListHandler.animeEntryExists(infoLink);

        // then
        assertEquals(result, true);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testAnimeEntryNotExists() {
        // given

        // when
        final boolean result = inMemoryAnimeListHandler.animeEntryExists(new InfoLink("http://myanimelist.net/anime/1535"));

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
        inMemoryAnimeListHandler.addAnime(entry);

        // when
        final List<Anime> animeList = inMemoryAnimeListHandler.fetchAnimeList();

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
        inMemoryAnimeListHandler.addAnime(entry);

        // when
        final boolean result = inMemoryAnimeListHandler.removeAnime(entry.getId());

        // then
        assertEquals(result, true);
        assertEquals(inMemoryAnimeListHandler.fetchAnimeList().isEmpty(), true);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testRemoveFromAnimeListNullAsArgument() {
        // given

        // when
        final boolean result = inMemoryAnimeListHandler.removeAnime(null);

        // then
        assertEquals(result, false);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testUpdateOrCreateWithNull() {
        // given

        // when
        inMemoryAnimeListHandler.updateOrCreate(null);

        // then
        assertEquals(inMemoryAnimeListHandler.fetchAnimeList().isEmpty(), true);
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
        inMemoryAnimeListHandler.updateOrCreate(entry);

        // then
        assertEquals(inMemoryAnimeListHandler.fetchAnimeList().isEmpty(), false);
        assertEquals(inMemoryAnimeListHandler.fetchAnimeList().get(0), entry);
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

        inMemoryAnimeListHandler.addAnime(entry);

        final int episodes = 37;
        entry.setEpisodes(episodes);

        // when
        inMemoryAnimeListHandler.updateOrCreate(entry);

        // then
        assertEquals(inMemoryAnimeListHandler.fetchAnimeList().isEmpty(), false);
        assertEquals(inMemoryAnimeListHandler.fetchAnimeList().get(0).getEpisodes(), episodes);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testClearing() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink(new InfoLink("http://myanimelist.net/anime/1535"));
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);

        inMemoryAnimeListHandler.addAnime(entry);

        // when
        inMemoryAnimeListHandler.clear();

        // then
        assertEquals(inMemoryAnimeListHandler.fetchAnimeList().isEmpty(), true);
    }
}

package io.github.manami.persistence.inmemory.animelist;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.Anime;

public class InMemoryAnimeListHandlerTest {

    private InMemoryAnimeListHandler inMemoryAnimeListHandler;


    @Before
    public void setUp() throws IOException {
        inMemoryAnimeListHandler = new InMemoryAnimeListHandler();
    }


    @Test
    public void testaddAnimeIsNull() {
        // given

        // when
        final boolean result = inMemoryAnimeListHandler.addAnime(null);

        // then
        assertThat(result, equalTo(false));
        assertThat(inMemoryAnimeListHandler.fetchAnimeList().size(), equalTo(0));
    }


    @Test
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
        final boolean result = inMemoryAnimeListHandler.addAnime(entry);

        // then
        assertThat(result, equalTo(true));
        assertThat(inMemoryAnimeListHandler.fetchAnimeList().size(), equalTo(1));
    }


    @Test
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
        final boolean result = inMemoryAnimeListHandler.addAnime(entry);

        // then
        assertThat(result, equalTo(true));
        assertThat(inMemoryAnimeListHandler.fetchAnimeList().size(), equalTo(1));
    }


    @Test
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
        assertThat(result, equalTo(true));
        assertThat(inMemoryAnimeListHandler.fetchAnimeList().size(), equalTo(1));
    }


    @Test
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
        final boolean result = inMemoryAnimeListHandler.addAnime(entry);

        // then
        assertThat(result, equalTo(false));
        assertThat(inMemoryAnimeListHandler.fetchAnimeList().size(), equalTo(0));
    }


    @Test
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
        final boolean result = inMemoryAnimeListHandler.addAnime(entry);

        // then
        assertThat(result, equalTo(true));
        assertThat(inMemoryAnimeListHandler.fetchAnimeList().size(), equalTo(1));
    }


    @Test
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
        final boolean result = inMemoryAnimeListHandler.addAnime(entry);

        // then
        assertThat(result, equalTo(true));
        assertThat(inMemoryAnimeListHandler.fetchAnimeList().size(), equalTo(1));
    }


    @Test
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
        final boolean result = inMemoryAnimeListHandler.addAnime(entry);

        // then
        assertThat(result, equalTo(false));
        assertThat(inMemoryAnimeListHandler.fetchAnimeList().size(), equalTo(0));
    }


    @Test
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
        final boolean result = inMemoryAnimeListHandler.addAnime(entry);

        // then
        assertThat(result, equalTo(false));
        assertThat(inMemoryAnimeListHandler.fetchAnimeList().size(), equalTo(0));
    }


    @Test
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
        inMemoryAnimeListHandler.addAnime(entry);

        // when
        final boolean result = inMemoryAnimeListHandler.animeEntryExists(infoLink);

        // then
        assertThat(result, equalTo(true));
    }


    @Test
    public void testAnimeEntryNotExists() {
        // given

        // when
        final boolean result = inMemoryAnimeListHandler.animeEntryExists("http://myanimelist.net/anime/1535");

        // then
        assertThat(result, equalTo(false));
    }


    @Test
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
        inMemoryAnimeListHandler.addAnime(entry);

        // when
        final List<Anime> animeList = inMemoryAnimeListHandler.fetchAnimeList();

        // then
        assertThat(animeList.size(), equalTo(1));
    }


    @Test
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
        inMemoryAnimeListHandler.addAnime(entry);

        // when
        final boolean result = inMemoryAnimeListHandler.removeAnime(entry.getId());

        // then
        assertThat(result, equalTo(true));
        assertThat(inMemoryAnimeListHandler.fetchAnimeList().isEmpty(), equalTo(true));
    }


    @Test
    public void testRemoveFromAnimeListNullAsArgument() {
        // given

        // when
        final boolean result = inMemoryAnimeListHandler.removeAnime(null);

        // then
        assertThat(result, equalTo(false));
    }


    @Test
    public void testUpdateOrCreateWithNull() {
        // given

        // when
        inMemoryAnimeListHandler.updateOrCreate(null);

        // then
        assertThat(inMemoryAnimeListHandler.fetchAnimeList().isEmpty(), equalTo(true));
    }


    @Test
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
        inMemoryAnimeListHandler.updateOrCreate(entry);

        // then
        assertThat(inMemoryAnimeListHandler.fetchAnimeList().isEmpty(), equalTo(false));
        assertThat(inMemoryAnimeListHandler.fetchAnimeList().get(0), equalTo(entry));
    }


    @Test
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

        inMemoryAnimeListHandler.addAnime(entry);

        final int episodes = 37;
        entry.setEpisodes(episodes);

        // when
        inMemoryAnimeListHandler.updateOrCreate(entry);

        // then
        assertThat(inMemoryAnimeListHandler.fetchAnimeList().isEmpty(), equalTo(false));
        assertThat(inMemoryAnimeListHandler.fetchAnimeList().get(0).getEpisodes(), equalTo(episodes));
    }


    @Test
    public void testClearing() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);

        inMemoryAnimeListHandler.addAnime(entry);

        // when
        inMemoryAnimeListHandler.clear();

        // then
        assertThat(inMemoryAnimeListHandler.fetchAnimeList().isEmpty(), equalTo(true));
    }
}

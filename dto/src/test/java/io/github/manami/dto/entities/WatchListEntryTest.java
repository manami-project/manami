package io.github.manami.dto.entities;

import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Optional;

import org.testng.annotations.Test;

import io.github.manami.dto.AnimeType;

public class WatchListEntryTest {

    @Test(groups = UNIT_TEST_GROUP)
    public void testValueOfFromNull() {
        // given

        // when
        final Optional<WatchListEntry> result = WatchListEntry.valueOf(null);

        // then
        assertFalse(result.isPresent());
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testValueOfFromAnime() {
        // given
        final Anime anime = new Anime("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));
        anime.setEpisodes(37);
        anime.setLocation("/anime/series/death_note");
        anime.setPicture("https://myanimelist.cdn-dena.com/images/anime/9/9453.jpg");
        anime.setThumbnail("https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg");
        anime.setType(AnimeType.TV);

        // when
        final Optional<WatchListEntry> result = WatchListEntry.valueOf(anime);

        // then
        assertTrue(result.isPresent());
        assertEquals(result.get().getTitle(), anime.getTitle());
        assertEquals(result.get().getThumbnail(), anime.getThumbnail());
        assertEquals(result.get().getInfoLink(), anime.getInfoLink());
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testValueOfFromFilterEntry() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg", new InfoLink("http://myanimelist.net/anime/1535"));

        // when
        final Optional<WatchListEntry> result = WatchListEntry.valueOf(entry);

        // then
        assertTrue(result.isPresent());
        assertEquals(result.get().getTitle(), entry.getTitle());
        assertEquals(result.get().getThumbnail(), entry.getThumbnail());
        assertEquals(result.get().getInfoLink(), entry.getInfoLink());
    }
}

package io.github.manami.dto.entities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

import io.github.manami.dto.AnimeType;

public class MinimalEntryTest {

    @Test
    public void testWithNull() {
        // given

        // when
        final boolean result = MinimalEntry.isValidMinimalEntry(null);

        // then
        assertThat(result, equalTo(false));
    }


    @Test
    public void testFilterEntryIsValid() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");

        // when
        final boolean result = MinimalEntry.isValidMinimalEntry(entry);

        // then
        assertThat(result, equalTo(true));
    }


    @Test
    public void testFilterEntryNotValidMissingTitle() {
        // given
        final FilterEntry entry = new FilterEntry(null, "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");

        // when
        final boolean result = MinimalEntry.isValidMinimalEntry(entry);

        // then
        assertThat(result, equalTo(false));
    }


    @Test
    public void testFilterEntryIsValidMissingThumbnail() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", null, "http://myanimelist.net/anime/1535");

        // when
        final boolean result = MinimalEntry.isValidMinimalEntry(entry);

        // then
        assertThat(result, equalTo(true));
    }


    @Test
    public void testFilterEntryNotValidMissingInfoLink() {
        // given
        final FilterEntry entry = new FilterEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", null);

        // when
        final boolean result = MinimalEntry.isValidMinimalEntry(entry);

        // then
        assertThat(result, equalTo(false));
    }


    @Test
    public void testWatchListEntryIsValid() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");

        // when
        final boolean result = MinimalEntry.isValidMinimalEntry(entry);

        // then
        assertThat(result, equalTo(true));
    }


    @Test
    public void testWatchListEntryNotValidMissingTitle() {
        // given
        final WatchListEntry entry = new WatchListEntry(null, "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");

        // when
        final boolean result = MinimalEntry.isValidMinimalEntry(entry);

        // then
        assertThat(result, equalTo(false));
    }


    @Test
    public void testWatchListEntryIsValidMissingThumbnail() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", null, "http://myanimelist.net/anime/1535");

        // when
        final boolean result = MinimalEntry.isValidMinimalEntry(entry);

        // then
        assertThat(result, equalTo(true));
    }


    @Test
    public void testWatchListEntryNotValidMissingInfoLink() {
        // given
        final WatchListEntry entry = new WatchListEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", null);

        // when
        final boolean result = MinimalEntry.isValidMinimalEntry(entry);

        // then
        assertThat(result, equalTo(false));
    }


    @Test
    public void testAnimeIsValid() {
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
        final boolean result = MinimalEntry.isValidMinimalEntry(entry);

        // then
        assertThat(result, equalTo(true));
    }


    @Test
    public void testAnimeNotValidMissingTitle() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = MinimalEntry.isValidMinimalEntry(entry);

        // then
        assertThat(result, equalTo(false));
    }


    @Test
    public void testAnimeNotValidMissingThumbnail() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = MinimalEntry.isValidMinimalEntry(entry);

        // then
        assertThat(result, equalTo(false));
    }


    @Test
    public void testAnimeNotValidMissingInfoLink() {
        // given
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = MinimalEntry.isValidMinimalEntry(entry);

        // then
        assertThat(result, equalTo(false));
    }
}

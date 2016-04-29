package io.github.manami.dto.entities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import io.github.manami.dto.AnimeType;

public class AnimeTest {

    private Anime anime;


    @Before
    public void setUp() throws IOException {
        anime = new Anime();
        anime.setEpisodes(37);
        anime.setInfoLink("http://myanimelist.net/anime/1535");
        anime.setLocation("/anime/series/death_note");
        anime.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        anime.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        anime.setTitle("Death Note");
        anime.setType(AnimeType.TV);
    }


    @Test
    public void testCopyEmptyAnime() {
        // given
        final Anime target = new Anime();

        // when
        Anime.copyAnime(anime, target);

        // then
        assertThat(target.getEpisodes(), equalTo(anime.getEpisodes()));
        assertThat(target.getInfoLink(), equalTo(anime.getInfoLink()));
        assertThat(target.getLocation(), equalTo(anime.getLocation()));
        assertThat(target.getPicture(), equalTo(anime.getPicture()));
        assertThat(target.getThumbnail(), equalTo(anime.getThumbnail()));
        assertThat(target.getTitle(), equalTo(anime.getTitle()));
        assertThat(target.getType(), equalTo(anime.getType()));
        assertThat(target.getId(), not(anime.getId()));
    }
}

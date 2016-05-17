package io.github.manami.dto.entities;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import java.util.UUID;

import org.junit.Test;

import io.github.manami.dto.AnimeType;

public class AnimeTest {

    @Test
    public void testCopyEmptyAnime() {
        // given
        final Anime target = new Anime();

        final Anime anime = new Anime();
        anime.setEpisodes(37);
        anime.setInfoLink("http://myanimelist.net/anime/1535");
        anime.setLocation("/anime/series/death_note");
        anime.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        anime.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        anime.setTitle("Death Note");
        anime.setType(AnimeType.TV);

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


    @Test
    public void testConstructorWithUUID() {
        // given
        final UUID uuid = UUID.randomUUID();

        // when
        final Anime result = new Anime(uuid);

        // then
        assertThat(result.getEpisodes(), equalTo(0));
        assertThat(result.getPicture(), equalTo(EMPTY));
        assertThat(result.getThumbnail(), equalTo("http://cdn.myanimelist.net/images/qm_50.gif"));
        assertThat(result.getInfoLink(), equalTo(null));
        assertThat(result.getLocation(), equalTo(EMPTY));
        assertThat(result.getTitle(), equalTo(null));
        assertThat(result.getType(), equalTo(null));
        assertThat(result.getId(), equalTo(uuid));
    }


    @Test
    public void testSetEpisodesDoesNotChangeToNegativeNumber() {
        // given
        final Anime anime = new Anime();

        // when
        anime.setEpisodes(-1);

        // then
        assertThat(anime.getEpisodes(), equalTo(0));
    }


    @Test
    public void testSetEpisodesDoesNotChangeToNegativeNumberForNonDefaultValue() {
        // given
        final Anime anime = new Anime();
        final int episodes = 4;
        anime.setEpisodes(episodes);

        // when
        anime.setEpisodes(-1);

        // then
        assertThat(anime.getEpisodes(), equalTo(episodes));
    }


    @Test
    public void testIsValidAnimeWithNull() {
        // given

        // when
        final boolean result = Anime.isValidAnime(null);

        // then
        assertThat(result, equalTo(false));
    }


    @Test
    public void testIsValidAnimeWithValidEntry() {
        // given
        final Anime anime = new Anime();
        anime.setEpisodes(37);
        anime.setInfoLink("http://myanimelist.net/anime/1535");
        anime.setLocation("/anime/series/death_note");
        anime.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        anime.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        anime.setTitle("Death Note");
        anime.setType(AnimeType.TV);

        // when
        final boolean result = Anime.isValidAnime(anime);

        // then
        assertThat(result, equalTo(true));
    }


    @Test
    public void testIsValidAnimeWithTypeNull() {
        // given
        final Anime anime = new Anime();
        anime.setEpisodes(37);
        anime.setInfoLink("http://myanimelist.net/anime/1535");
        anime.setLocation("/anime/series/death_note");
        anime.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        anime.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        anime.setTitle("Death Note");
        anime.setType(null);

        // when
        final boolean result = Anime.isValidAnime(anime);

        // then
        assertThat(result, equalTo(false));
    }


    @Test
    public void testGetTypeAsStringForNull() {
        // given
        final Anime anime = new Anime();
        anime.setType(null);

        // when
        final String result = anime.getTypeAsString();

        // then
        assertThat(result, equalTo(null));
    }


    @Test
    public void testGetTypeAsStringForTv() {
        // given
        final AnimeType type = AnimeType.TV;
        final Anime anime = new Anime();
        anime.setType(type);

        // when
        final String result = anime.getTypeAsString();

        // then
        assertThat(result, equalTo(type.getValue()));
    }


    @Test
    public void testGetTypeAsStringForMovie() {
        // given
        final AnimeType type = AnimeType.MOVIE;
        final Anime anime = new Anime();
        anime.setType(type);

        // when
        final String result = anime.getTypeAsString();

        // then
        assertThat(result, equalTo(type.getValue()));
    }


    @Test
    public void testGetTypeAsStringForMusic() {
        // given
        final AnimeType type = AnimeType.MUSIC;
        final Anime anime = new Anime();
        anime.setType(type);

        // when
        final String result = anime.getTypeAsString();

        // then
        assertThat(result, equalTo(type.getValue()));
    }


    @Test
    public void testGetTypeAsStringForOna() {
        // given
        final AnimeType type = AnimeType.ONA;
        final Anime anime = new Anime();
        anime.setType(type);

        // when
        final String result = anime.getTypeAsString();

        // then
        assertThat(result, equalTo(type.getValue()));
    }


    @Test
    public void testGetTypeAsStringForOva() {
        // given
        final AnimeType type = AnimeType.OVA;
        final Anime anime = new Anime();
        anime.setType(type);

        // when
        final String result = anime.getTypeAsString();

        // then
        assertThat(result, equalTo(type.getValue()));
    }


    @Test
    public void testGetTypeAsStringForSpecial() {
        // given
        final AnimeType type = AnimeType.SPECIAL;
        final Anime anime = new Anime();
        anime.setType(type);

        // when
        final String result = anime.getTypeAsString();

        // then
        assertThat(result, equalTo(type.getValue()));
    }


    @Test
    public void testCopyNullTarget() {
        // given
        final Anime target = new Anime();

        final Anime anime = new Anime();
        anime.setEpisodes(37);
        anime.setInfoLink("http://myanimelist.net/anime/1535");
        anime.setLocation("/anime/series/death_note");
        anime.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        anime.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        anime.setTitle("Death Note");
        anime.setType(AnimeType.TV);

        // when
        Anime.copyNullTarget(anime, target);

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


    @Test
    public void testCopyNullTargetWithFilledTarget() {
        // given
        final Anime target = new Anime();
        target.setEpisodes(4);
        target.setInfoLink("http://myanimelist.net/anime/10863");
        target.setLocation("/anime/series/steins_gate_special");
        target.setPicture("http://cdn.myanimelist.net/images/anime/7/36531.jpg");
        target.setThumbnail("http://cdn.myanimelist.net/images/anime/7/36531t.jpg");
        target.setTitle("Steins;Gate: Oukoubakko no Poriomania");
        target.setType(AnimeType.SPECIAL);

        final Anime anime = new Anime();
        anime.setEpisodes(37);
        anime.setInfoLink("http://myanimelist.net/anime/1535");
        anime.setLocation("/anime/series/death_note");
        anime.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        anime.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        anime.setTitle("Death Note");
        anime.setType(AnimeType.TV);

        // when
        Anime.copyNullTarget(anime, target);

        // then
        assertThat(target.getEpisodes(), not(anime.getEpisodes()));
        assertThat(target.getInfoLink(), not(anime.getInfoLink()));
        assertThat(target.getLocation(), not(anime.getLocation()));
        assertThat(target.getPicture(), not(anime.getPicture()));
        assertThat(target.getThumbnail(), not(anime.getThumbnail()));
        assertThat(target.getTitle(), not(anime.getTitle()));
        assertThat(target.getType(), not(anime.getType()));
        assertThat(target.getId(), not(anime.getId()));
    }
}

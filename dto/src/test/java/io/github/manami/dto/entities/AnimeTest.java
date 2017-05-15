package io.github.manami.dto.entities;

import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static io.github.manami.dto.entities.Anime.copyAnime;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.manami.dto.AnimeType;
import org.testng.annotations.Test;

public class AnimeTest {

  @Test(groups = UNIT_TEST_GROUP)
  public void testSetEpisodesDoesNotChangeToNegativeNumber() {
    // given
    final Anime anime = new Anime("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));

    // when
    anime.setEpisodes(-1);

    // then
    assertThat(anime.getEpisodes()).isEqualTo(0);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testSetEpisodesDoesNotChangeToNegativeNumberForNonDefaultValue() {
    // given
    final Anime anime = new Anime("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));
    final int episodes = 4;
    anime.setEpisodes(episodes);

    // when
    anime.setEpisodes(-1);

    // then
    assertThat(anime.getEpisodes()).isEqualTo(episodes);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testIsValidAnimeWithNull() {
    // given

    // when
    final boolean result = Anime.isValidAnime(null);

    // then
    assertThat(result).isFalse();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testIsValidAnimeWithValidEntry() {
    // given
    final Anime anime = new Anime("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));
    anime.setEpisodes(37);
    anime.setLocation("/anime/series/death_note");
    anime.setPicture("https://myanimelist.cdn-dena.com/images/anime/9/9453.jpg");
    anime.setThumbnail("https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg");
    anime.setType(AnimeType.TV);

    // when
    final boolean result = Anime.isValidAnime(anime);

    // then
    assertThat(result).isTrue();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testIsValidAnimeWithTypeNull() {
    // given
    final Anime anime = new Anime("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));
    anime.setEpisodes(37);
    anime.setLocation("/anime/series/death_note");
    anime.setPicture("https://myanimelist.cdn-dena.com/images/anime/9/9453.jpg");
    anime.setThumbnail("https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg");
    anime.setType(null);

    // when
    final boolean result = Anime.isValidAnime(anime);

    // then
    assertThat(result).isFalse();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testGetTypeAsStringForNull() {
    // given
    final Anime anime = new Anime("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));
    anime.setType(null);

    // when
    final String result = anime.getTypeAsString();

    // then
    assertThat(result).isNull();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testGetTypeAsStringForTv() {
    // given
    final AnimeType type = AnimeType.TV;
    final Anime anime = new Anime("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));
    anime.setType(type);

    // when
    final String result = anime.getTypeAsString();

    // then
    assertThat(result).isEqualTo(type.getValue());
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testGetTypeAsStringForMovie() {
    // given
    final AnimeType type = AnimeType.MOVIE;
    final Anime anime = new Anime("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));
    anime.setType(type);

    // when
    final String result = anime.getTypeAsString();

    // then
    assertThat(result).isEqualTo(type.getValue());
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testGetTypeAsStringForMusic() {
    // given
    final AnimeType type = AnimeType.MUSIC;
    final Anime anime = new Anime("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));
    anime.setType(type);

    // when
    final String result = anime.getTypeAsString();

    // then
    assertThat(result).isEqualTo(type.getValue());
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testGetTypeAsStringForOna() {
    // given
    final AnimeType type = AnimeType.ONA;
    final Anime anime = new Anime("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));
    anime.setType(type);

    // when
    final String result = anime.getTypeAsString();

    // then
    assertThat(result).isEqualTo(type.getValue());
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testGetTypeAsStringForOva() {
    // given
    final AnimeType type = AnimeType.OVA;
    final Anime anime = new Anime("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));
    anime.setType(type);

    // when
    final String result = anime.getTypeAsString();

    // then
    assertThat(result).isEqualTo(type.getValue());
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testGetTypeAsStringForSpecial() {
    // given
    final AnimeType type = AnimeType.SPECIAL;
    final Anime anime = new Anime("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));
    anime.setType(type);

    // when
    final String result = anime.getTypeAsString();

    // then
    assertThat(result).isEqualTo(type.getValue());
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testCopyAnime() {
    // given
    final Anime anime = new Anime("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));
    anime.setEpisodes(37);
    anime.setLocation("/anime/series/death_note");
    anime.setPicture("https://myanimelist.cdn-dena.com/images/anime/9/9453.jpg");
    anime.setThumbnail("https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg");
    anime.setType(AnimeType.TV);

    // when
    final Anime result = copyAnime(anime);

    // then
    assertThat(result != anime).isTrue();
    assertThat(result.getEpisodes()).isEqualTo(anime.getEpisodes());
    assertThat(result.getInfoLink()).isEqualTo(anime.getInfoLink());
    assertThat(result.getLocation()).isEqualTo(anime.getLocation());
    assertThat(result.getPicture()).isEqualTo(anime.getPicture());
    assertThat(result.getThumbnail()).isEqualTo(anime.getThumbnail());
    assertThat(result.getTitle()).isEqualTo(anime.getTitle());
    assertThat(result.getType()).isEqualTo(anime.getType());
    assertThat(result.getId()).isEqualTo(anime.getId());
  }
}
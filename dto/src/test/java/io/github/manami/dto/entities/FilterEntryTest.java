package io.github.manami.dto.entities;

import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.manami.dto.AnimeType;
import java.util.Optional;
import org.testng.annotations.Test;

public class FilterEntryTest {

  @Test(groups = UNIT_TEST_GROUP)
  public void testValueOfFromNull() {
    // given

    // when
    final Optional<FilterListEntry> result = FilterListEntry.valueOf(null);

    // then
    assertThat(result.isPresent()).isFalse();
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
    final Optional<FilterListEntry> result = FilterListEntry.valueOf(anime);

    // then
    assertThat(result.isPresent()).isTrue();
    assertThat(result.get().getTitle()).isEqualTo(anime.getTitle());
    assertThat(result.get().getThumbnail()).isEqualTo(anime.getThumbnail());
    assertThat(result.get().getInfoLink()).isEqualTo(anime.getInfoLink());
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testValueOfFromWatchListEntry() {
    // given
    final WatchListEntry entry = new WatchListEntry("Death Note",
        "https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg",
        new InfoLink("http://myanimelist.net/anime/1535"));

    // when
    final Optional<FilterListEntry> result = FilterListEntry.valueOf(entry);

    // then
    assertThat(result.isPresent()).isTrue();
    assertThat(result.get().getTitle()).isEqualTo(entry.getTitle());
    assertThat(result.get().getThumbnail()).isEqualTo(entry.getThumbnail());
    assertThat(result.get().getInfoLink()).isEqualTo(entry.getInfoLink());
  }
}

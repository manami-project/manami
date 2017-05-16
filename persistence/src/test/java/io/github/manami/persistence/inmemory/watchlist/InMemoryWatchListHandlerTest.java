package io.github.manami.persistence.inmemory.watchlist;

import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.AbstractMinimalEntry;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.WatchListEntry;
import java.io.IOException;
import java.util.List;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class InMemoryWatchListHandlerTest {

  private InMemoryWatchListHandler inMemoryWatchListHandler;


  @BeforeMethod
  public void setUp() throws IOException {
    inMemoryWatchListHandler = new InMemoryWatchListHandler();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testFetchWatchList() {
    // given
    final WatchListEntry entry = new WatchListEntry("Death Note",
        new InfoLink("http://myanimelist.net/anime/1535"));
    inMemoryWatchListHandler.watchAnime(entry);

    // when
    final List<WatchListEntry> fetchWatchList = inMemoryWatchListHandler.fetchWatchList();

    // then
    assertThat(fetchWatchList.size()).isEqualTo(1);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testWatchListEntryExists() {
    // given
    final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");
    final WatchListEntry entry = new WatchListEntry("Death Note", infoLink);
    inMemoryWatchListHandler.watchAnime(entry);

    // when
    final boolean result = inMemoryWatchListHandler.watchListEntryExists(infoLink);

    // then
    assertThat(result).isTrue();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testWatchListEntryNotExists() {
    // given

    // when
    final boolean result = inMemoryWatchListHandler
        .watchListEntryExists(new InfoLink("http://myanimelist.net/anime/1535"));

    // then
    assertThat(result).isFalse();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testWatchAnimeIsNull() {
    // given

    // when
    final boolean result = inMemoryWatchListHandler.watchAnime(null);

    // then
    assertThat(result).isFalse();
    assertThat(inMemoryWatchListHandler.fetchWatchList().size()).isEqualTo(0);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testWatchAnimeIsEntryWithoutTitle() {
    // given
    final WatchListEntry entry = new WatchListEntry(EMPTY,
        new InfoLink("http://myanimelist.net/anime/1535"));

    // when
    final boolean result = inMemoryWatchListHandler.watchAnime(entry);

    // then
    assertThat(result).isFalse();
    assertThat(inMemoryWatchListHandler.fetchWatchList().size()).isEqualTo(0);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testWatchAnimeIsEntryWithoutInfoLink() {
    // given
    final WatchListEntry entry = new WatchListEntry("Death Note", null);

    // when
    final boolean result = inMemoryWatchListHandler.watchAnime(entry);

    // then
    assertThat(result).isFalse();
    assertThat(inMemoryWatchListHandler.fetchWatchList().size()).isEqualTo(0);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testWatchAnimeIsEntryWithoutThumbnail() {
    // given
    final WatchListEntry entry = new WatchListEntry("Death Note",
        new InfoLink("http://myanimelist.net/anime/1535"));

    // when
    final boolean result = inMemoryWatchListHandler.watchAnime(entry);

    // then
    assertThat(result).isTrue();
    assertThat(inMemoryWatchListHandler.fetchWatchList().size()).isEqualTo(1);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testWatchAnimeIsFullEntry() {
    // given
    final WatchListEntry entry = new WatchListEntry("Death Note",
        "http://cdn.myanimelist.net/images/anime/9/9453t.jpg",
        new InfoLink("http://myanimelist.net/anime/1535"));

    // when
    final boolean result = inMemoryWatchListHandler.watchAnime(entry);

    // then
    assertThat(result).isTrue();
    assertThat(inMemoryWatchListHandler.fetchWatchList().size()).isEqualTo(1);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testRemoveFromWatchListWorks() {
    // given
    final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");
    final WatchListEntry entry = new WatchListEntry("Death Note", infoLink);
    inMemoryWatchListHandler.watchAnime(entry);

    // when
    final boolean result = inMemoryWatchListHandler.removeFromWatchList(infoLink);

    // then
    assertThat(result).isTrue();
    assertThat(inMemoryWatchListHandler.fetchWatchList().isEmpty()).isTrue();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testRemoveFromWatchListNullAsArgument() {
    // given

    // when
    final boolean result = inMemoryWatchListHandler.removeFromWatchList(null);

    // then
    assertThat(result).isFalse();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testUpdateOrCreateWithNull() {
    // given

    // when
    inMemoryWatchListHandler.updateOrCreate(null);

    // then
    assertThat(inMemoryWatchListHandler.fetchWatchList().isEmpty()).isTrue();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testUpdateOrCreateForNewWatchListEntry() {
    // given
    final WatchListEntry entry = new WatchListEntry("Death Note",
        "http://cdn.myanimelist.net/images/anime/9/9453t.jpg",
        new InfoLink("http://myanimelist.net/anime/1535"));

    // when
    inMemoryWatchListHandler.updateOrCreate(entry);

    // then
    assertThat(inMemoryWatchListHandler.fetchWatchList().isEmpty()).isFalse();
    assertThat(inMemoryWatchListHandler.fetchWatchList().get(0)).isEqualTo(entry);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testUpdateOrCreateForModifiedWatchListEntry() {
    // given
    final WatchListEntry entry = new WatchListEntry("Death Note", AbstractMinimalEntry.NO_IMG_THUMB,
        new InfoLink("http://myanimelist.net/anime/1535"));

    inMemoryWatchListHandler.watchAnime(entry);

    final String thumbnail = "http://cdn.myanimelist.net/images/anime/9/9453t.jpg";
    entry.setThumbnail(thumbnail);

    // when
    inMemoryWatchListHandler.updateOrCreate(entry);

    // then
    assertThat(inMemoryWatchListHandler.fetchWatchList().isEmpty()).isFalse();
    assertThat(inMemoryWatchListHandler.fetchWatchList().get(0).getThumbnail())
        .isEqualTo(thumbnail);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testClearing() {
    // given
    final WatchListEntry entry = new WatchListEntry("Death Note",
        "http://cdn.myanimelist.net/images/anime/9/9453t.jpg",
        new InfoLink("http://myanimelist.net/anime/1535"));
    inMemoryWatchListHandler.watchAnime(entry);

    // when
    inMemoryWatchListHandler.clear();

    // then
    assertThat(inMemoryWatchListHandler.fetchWatchList().isEmpty()).isTrue();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testWatchListEntryNotAddedBecauseItAlreadyExists() {
    // given
    final FilterEntry entry = new FilterEntry("Death Note",
        "http://cdn.myanimelist.net/images/anime/9/9453t.jpg",
        new InfoLink("http://myanimelist.net/anime/1535"));
    inMemoryWatchListHandler.watchAnime(entry);

    // when
    final boolean result = inMemoryWatchListHandler.watchAnime(entry);

    // then
    assertThat(result).isFalse();
    assertThat(inMemoryWatchListHandler.fetchWatchList().size()).isEqualTo(1);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testWatchFilterEntry() {
    // given
    final FilterEntry entry = new FilterEntry("Death Note",
        "http://cdn.myanimelist.net/images/anime/9/9453t.jpg",
        new InfoLink("http://myanimelist.net/anime/1535"));

    // when
    final boolean result = inMemoryWatchListHandler.watchAnime(entry);

    // then
    assertThat(result).isTrue();
    assertThat(inMemoryWatchListHandler.fetchWatchList().size()).isEqualTo(1);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testFilterAnime() {
    // given
    final Anime entry = new Anime("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));
    entry.setEpisodes(37);
    entry.setLocation("/death_note");
    entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
    entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
    entry.setType(AnimeType.TV);

    // when
    final boolean result = inMemoryWatchListHandler.watchAnime(entry);

    // then
    assertThat(result).isTrue();
    assertThat(inMemoryWatchListHandler.fetchWatchList().size()).isEqualTo(1);
  }
}

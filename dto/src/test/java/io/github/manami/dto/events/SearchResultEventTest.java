package io.github.manami.dto.events;

import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterListEntry;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.WatchListEntry;
import org.testng.annotations.Test;

public class SearchResultEventTest {

  @Test(groups = UNIT_TEST_GROUP)
  public void testConstructor() {
    // given
    final String searchString = "Death Note";

    // when
    final SearchResultEvent sut = new SearchResultEvent(searchString);

    // then
    assertThat(sut).isNotNull();

    assertThat(sut.getAnimeListSearchResultList()).isNotNull();
    assertThat(sut.getAnimeListSearchResultList().isEmpty()).isTrue();

    assertThat(sut.getFilterListSearchResultList()).isNotNull();
    assertThat(sut.getFilterListSearchResultList().isEmpty()).isTrue();

    assertThat(sut.getWatchListSearchResultList()).isNotNull();
    assertThat(sut.getWatchListSearchResultList().isEmpty()).isTrue();

    assertThat(sut.getSearchString()).isEqualTo(searchString);
  }


  @Test(groups = UNIT_TEST_GROUP, expectedExceptions = UnsupportedOperationException.class)
  public void testAnimeListUnmodifiable() {
    // given
    final String searchString = "Death Note";
    final SearchResultEvent sut = new SearchResultEvent(searchString);
    final Anime anime = new Anime("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));

    // when
    sut.getAnimeListSearchResultList().add(anime);

    // then
  }


  @Test(groups = UNIT_TEST_GROUP, expectedExceptions = UnsupportedOperationException.class)
  public void testFilterListUnmodifiable() {
    // given
    final String searchString = "Death Note";
    final SearchResultEvent sut = new SearchResultEvent(searchString);
    final FilterListEntry entry = new FilterListEntry(searchString,
        new InfoLink("http://myanimelist.net/anime/1535"));

    // when
    sut.getFilterListSearchResultList().add(entry);

    // then
  }


  @Test(groups = UNIT_TEST_GROUP, expectedExceptions = UnsupportedOperationException.class)
  public void testWatchListUnmodifiable() {
    // given
    final String searchString = "Death Note";
    final SearchResultEvent sut = new SearchResultEvent(searchString);
    final WatchListEntry entry = new WatchListEntry(searchString,
        new InfoLink("http://myanimelist.net/anime/1535"));

    // when
    sut.getWatchListSearchResultList().add(entry);

    // then
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testAddingEntryToAnimeList() {
    // given
    final String searchString = "Death Note";
    final Anime anime = new Anime("Death Note", new InfoLink("http://myanimelist.net/anime/1535"));
    final SearchResultEvent sut = new SearchResultEvent(searchString);

    // when
    sut.addAnimeListSearchResult(anime);

    // then
    assertThat(sut.getAnimeListSearchResultList().size()).isEqualTo(1);
    assertThat(sut.getAnimeListSearchResultList().get(0)).isEqualTo(anime);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testAddingNullToAnimeList() {
    // given
    final String searchString = "Death Note";
    final SearchResultEvent sut = new SearchResultEvent(searchString);

    // when
    sut.addAnimeListSearchResult(null);

    // then
    assertThat(sut.getAnimeListSearchResultList().isEmpty()).isTrue();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testAddingEntryToFilterList() {
    // given
    final String searchString = "Death Note";
    final FilterListEntry entry = new FilterListEntry(searchString,
        new InfoLink("http://myanimelist.net/anime/1535"));
    final SearchResultEvent sut = new SearchResultEvent(searchString);

    // when
    sut.addFilterListSearchResult(entry);

    // then
    assertThat(sut.getFilterListSearchResultList().size()).isEqualTo(1);
    assertThat(sut.getFilterListSearchResultList().get(0)).isEqualTo(entry);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testAddingNullToFilterList() {
    // given
    final String searchString = "Death Note";
    final SearchResultEvent sut = new SearchResultEvent(searchString);

    // when
    sut.addFilterListSearchResult(null);

    // then
    assertThat(sut.getFilterListSearchResultList().isEmpty()).isTrue();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testAddingEntryToWatchList() {
    // given
    final String searchString = "Death Note";
    final WatchListEntry entry = new WatchListEntry(searchString,
        new InfoLink("http://myanimelist.net/anime/1535"));
    final SearchResultEvent sut = new SearchResultEvent(searchString);

    // when
    sut.addWatchListSearchResult(entry);

    // then
    assertThat(sut.getWatchListSearchResultList().size()).isEqualTo(1);
    assertThat(sut.getWatchListSearchResultList().get(0)).isEqualTo(entry);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testAddingNullToWatchList() {
    // given
    final String searchString = "Death Note";
    final SearchResultEvent sut = new SearchResultEvent(searchString);

    // when
    sut.addWatchListSearchResult(null);

    // then
    assertThat(sut.getWatchListSearchResultList().isEmpty()).isTrue();
  }
}
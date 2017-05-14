package io.github.manami.cache.strategies.headlessbrowser.extractor.anime.mal;

import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.manami.cache.strategies.headlessbrowser.extractor.util.mal.MyAnimeListNetUtil;
import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;
import java.io.IOException;
import java.nio.file.Files;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MyAnimeListNetAnimeExtractorTest {

  private static final String DEATH_NOTE_URL_NO_PROTOCOL =
      MyAnimeListNetUtil.DOMAIN + "/anime/1535/Death_Note";
  private static final String EXPECTED_DEATH_NOTE_URL = "http://myanimelist.net/anime/1535";
  private static final String TEST_FILE = "meta_information_test_entry.html";
  private MyAnimeListNetAnimeExtractor sut;
  private String rawHtml;


  @BeforeMethod
  public void setUp() throws IOException {
    sut = new MyAnimeListNetAnimeExtractor();
    final ClassPathResource resource = new ClassPathResource(TEST_FILE);
    final StringBuilder strBuilder = new StringBuilder();
    Files.readAllLines(resource.getFile().toPath()).forEach(strBuilder::append);
    rawHtml = strBuilder.toString();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testIsValidInfoLink() throws IOException {
    // given
    final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");
    sut.extractAnimeEntry(infoLink, rawHtml);

    // when
    final boolean result = sut.isValidInfoLink();

    // then
    assertThat(result).isTrue();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testExtractTitle() throws IOException {
    // given
    final String expectedValue = "Death Note";
    final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");

    // when
    final Anime result = sut.extractAnimeEntry(infoLink, rawHtml);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo(expectedValue);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testExtractType() throws IOException {
    // given
    final AnimeType expectedValue = AnimeType.TV;
    final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");

    // when
    final Anime result = sut.extractAnimeEntry(infoLink, rawHtml);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getType()).isEqualTo(expectedValue);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testExtractEpisodes() throws IOException {
    // given
    final int expectedValue = 37;
    final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");

    // when
    final Anime result = sut.extractAnimeEntry(infoLink, rawHtml);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getEpisodes()).isEqualTo(expectedValue);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testExtractPictureLink() throws IOException {
    // given
    final String expectedValue = "https://myanimelist.cdn-dena.com/images/anime/9/9453.jpg";
    final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");

    // when
    final Anime result = sut.extractAnimeEntry(infoLink, rawHtml);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getPicture()).isEqualTo(expectedValue);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testExtractThumbnail() throws IOException {
    // given
    final String expectedValue = "https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg";

    // when
    final Anime result = sut
        .extractAnimeEntry(new InfoLink("http://myanimelist.net/anime/1535"), rawHtml);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getThumbnail()).isEqualTo(expectedValue);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void isResponsibleTrueHttpWww() {
    // given
    final InfoLink infoLink = new InfoLink("http://www." + DEATH_NOTE_URL_NO_PROTOCOL);

    // when
    final boolean result = sut.isResponsible(infoLink);

    // then
    assertThat(result).isTrue();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void isResponsibleTrueHttp() {
    // given
    final InfoLink infoLink = new InfoLink("http://" + DEATH_NOTE_URL_NO_PROTOCOL);

    // when
    final boolean result = sut.isResponsible(infoLink);

    // then
    assertThat(result).isTrue();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void isResponsibleTrueHttpsWww() {
    // given
    final InfoLink infoLink = new InfoLink("https://www." + DEATH_NOTE_URL_NO_PROTOCOL);

    // when
    final boolean result = sut.isResponsible(infoLink);

    // then
    assertThat(result).isTrue();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void isResponsibleTrueHttps() {
    // given
    final InfoLink infoLink = new InfoLink("https://" + DEATH_NOTE_URL_NO_PROTOCOL);

    // when
    final boolean result = sut.isResponsible(infoLink);

    // then
    assertThat(result).isTrue();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void isResponsibleFalse() {
    // given
    final InfoLink infoLink = new InfoLink(
        "https://animenewsnetwork.com/encyclopedia/anime.php?id=6592");

    // when
    final boolean result = sut.isResponsible(infoLink);

    // then
    assertThat(result).isFalse();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void isResponsibleBlank() {
    // given
    final InfoLink urlEmpty = new InfoLink(EMPTY);
    final InfoLink urlWhitespace = new InfoLink(EMPTY);
    final InfoLink urlNull = new InfoLink(null);

    // when
    final boolean resultEmpty = sut.isResponsible(urlEmpty);
    final boolean resultWhitespace = sut.isResponsible(urlWhitespace);
    final boolean resultNull = sut.isResponsible(urlNull);

    // then
    assertThat(resultEmpty).isFalse();
    assertThat(resultWhitespace).isFalse();
    assertThat(resultNull).isFalse();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void normalizeInfoLinkQueryParameter() {
    // given
    final InfoLink infoLink = new InfoLink("https://myanimelist.net/anime.php?id=1535");

    // when
    final InfoLink result = sut.normalizeInfoLink(infoLink);

    // then
    assertThat(result.getUrl()).isEqualTo(EXPECTED_DEATH_NOTE_URL);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void normalizeInfoLinkSearch() {
    // given
    final InfoLink url = new InfoLink(
        "https://myanimelist.net/anime/1535/Death_Note?q=death%20note");

    // when
    final InfoLink result = sut.normalizeInfoLink(url);

    // then
    assertThat(result.getUrl()).isEqualTo(EXPECTED_DEATH_NOTE_URL);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void normalizeInfoLinkDefault() {
    // given
    final InfoLink infoLink = new InfoLink("https://myanimelist.net/anime/1535/Death_Note");

    // when
    final InfoLink result = sut.normalizeInfoLink(infoLink);

    // then
    assertThat(result.getUrl()).isEqualTo(EXPECTED_DEATH_NOTE_URL);
  }


  @Test(groups = UNIT_TEST_GROUP, description = "If you get the expected url already it is being returned as is, but with http instead of https.")
  public void normalizeInfoLinkIdentical() {
    // given
    final InfoLink infoLink = new InfoLink("https://myanimelist.net/anime/1535/Death_Note");

    // when
    final InfoLink result = sut.normalizeInfoLink(infoLink);

    // then
    assertThat(result.getUrl()).isEqualTo(EXPECTED_DEATH_NOTE_URL);
  }


  @Test(groups = UNIT_TEST_GROUP, description = "If the url does not match the expected pattern it is being returned unchanged.")
  public void normalizeInfoLinkDifferentPattern() {
    // given
    final InfoLink infoLink = new InfoLink(" https://myanimelist.net/news?_location=mal_h_m");

    // when
    final InfoLink result = sut.normalizeInfoLink(infoLink);

    // then
    assertThat(result.getUrl()).isEqualTo(infoLink.getUrl());
  }
}

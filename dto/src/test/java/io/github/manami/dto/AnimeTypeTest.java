package io.github.manami.dto;

import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class AnimeTypeTest {

  @Test(groups = UNIT_TEST_GROUP)
  public void testUnknown() {
    // given
    final String defaultString = "akjbfJKdsd";
    final String upperCase = defaultString.toUpperCase();
    final String lowerCase = defaultString.toLowerCase();

    // when
    final AnimeType defaultStringType = AnimeType.findByName(defaultString);
    final AnimeType upperCaseStringType = AnimeType.findByName(upperCase);
    final AnimeType lowerCaseStringType = AnimeType.findByName(lowerCase);

    // then
    assertThat(defaultStringType).isNull();
    assertThat(upperCaseStringType).isNull();
    assertThat(lowerCaseStringType).isNull();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testTv() {
    // given
    final String defaultString = "Tv";
    final String upperCase = defaultString.toUpperCase();
    final String lowerCase = defaultString.toLowerCase();

    // when
    final AnimeType defaultStringType = AnimeType.findByName(defaultString);
    final AnimeType upperCaseStringType = AnimeType.findByName(upperCase);
    final AnimeType lowerCaseStringType = AnimeType.findByName(lowerCase);

    // then
    assertThat(defaultStringType).isEqualTo(AnimeType.TV);
    assertThat(upperCaseStringType).isEqualTo(AnimeType.TV);
    assertThat(lowerCaseStringType).isEqualTo(AnimeType.TV);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testMovie() {
    // given
    final String defaultString = "Movie";
    final String upperCase = defaultString.toUpperCase();
    final String lowerCase = defaultString.toLowerCase();

    // when
    final AnimeType defaultStringType = AnimeType.findByName(defaultString);
    final AnimeType upperCaseStringType = AnimeType.findByName(upperCase);
    final AnimeType lowerCaseStringType = AnimeType.findByName(lowerCase);

    // then
    assertThat(defaultStringType).isEqualTo(AnimeType.MOVIE);
    assertThat(upperCaseStringType).isEqualTo(AnimeType.MOVIE);
    assertThat(lowerCaseStringType).isEqualTo(AnimeType.MOVIE);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testOva() {
    // given
    final String defaultString = "Ova";
    final String upperCase = defaultString.toUpperCase();
    final String lowerCase = defaultString.toLowerCase();

    // when
    final AnimeType defaultStringType = AnimeType.findByName(defaultString);
    final AnimeType upperCaseStringType = AnimeType.findByName(upperCase);
    final AnimeType lowerCaseStringType = AnimeType.findByName(lowerCase);

    // then
    assertThat(defaultStringType).isEqualTo(AnimeType.OVA);
    assertThat(upperCaseStringType).isEqualTo(AnimeType.OVA);
    assertThat(lowerCaseStringType).isEqualTo(AnimeType.OVA);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testSpecial() {
    // given
    final String defaultString = "Special";
    final String upperCase = defaultString.toUpperCase();
    final String lowerCase = defaultString.toLowerCase();

    // when
    final AnimeType defaultStringType = AnimeType.findByName(defaultString);
    final AnimeType upperCaseStringType = AnimeType.findByName(upperCase);
    final AnimeType lowerCaseStringType = AnimeType.findByName(lowerCase);

    // then
    assertThat(defaultStringType).isEqualTo(AnimeType.SPECIAL);
    assertThat(upperCaseStringType).isEqualTo(AnimeType.SPECIAL);
    assertThat(lowerCaseStringType).isEqualTo(AnimeType.SPECIAL);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testOna() {
    // given
    final String defaultString = "Ona";
    final String upperCase = defaultString.toUpperCase();
    final String lowerCase = defaultString.toLowerCase();

    // when
    final AnimeType defaultStringType = AnimeType.findByName(defaultString);
    final AnimeType upperCaseStringType = AnimeType.findByName(upperCase);
    final AnimeType lowerCaseStringType = AnimeType.findByName(lowerCase);

    // then
    assertThat(defaultStringType).isEqualTo(AnimeType.ONA);
    assertThat(upperCaseStringType).isEqualTo(AnimeType.ONA);
    assertThat(lowerCaseStringType).isEqualTo(AnimeType.ONA);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testMusic() {
    // given
    final String defaultString = "Music";
    final String upperCase = defaultString.toUpperCase();
    final String lowerCase = defaultString.toLowerCase();

    // when
    final AnimeType defaultStringType = AnimeType.findByName(defaultString);
    final AnimeType upperCaseStringType = AnimeType.findByName(upperCase);
    final AnimeType lowerCaseStringType = AnimeType.findByName(lowerCase);

    // then
    assertThat(defaultStringType).isEqualTo(AnimeType.MUSIC);
    assertThat(upperCaseStringType).isEqualTo(AnimeType.MUSIC);
    assertThat(lowerCaseStringType).isEqualTo(AnimeType.MUSIC);
  }
}
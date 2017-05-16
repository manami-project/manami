package io.github.manami.persistence.exporter.csv;

import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.manami.persistence.exporter.csv.CsvConfig.CsvConfigType;
import org.testng.annotations.Test;

public class CsvConfigTest {

  @Test(groups = UNIT_TEST_GROUP)
  public void testThatNonMatchableStringReturnsNull() {
    // given

    // when
    final CsvConfigType result = CsvConfigType.findByName("anyNonMatchableString");

    // then
    assertThat(result).isNull();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testAnimeList() {
    // given
    final String exactSameString = "animeList";
    final String upperCase = exactSameString.toUpperCase();
    final String lowerCase = exactSameString.toLowerCase();

    // when
    final CsvConfigType resultExactSame = CsvConfigType.findByName(exactSameString);
    final CsvConfigType resultUpperCase = CsvConfigType.findByName(upperCase);
    final CsvConfigType resultLowerCase = CsvConfigType.findByName(lowerCase);

    // then
    assertThat(resultExactSame).isEqualTo(CsvConfigType.ANIMELIST);
    assertThat(resultUpperCase).isEqualTo(CsvConfigType.ANIMELIST);
    assertThat(resultLowerCase).isEqualTo(CsvConfigType.ANIMELIST);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testWatchList() {
    // given
    final String exactSameString = "watchList";
    final String upperCase = exactSameString.toUpperCase();
    final String lowerCase = exactSameString.toLowerCase();

    // when
    final CsvConfigType resultExactSame = CsvConfigType.findByName(exactSameString);
    final CsvConfigType resultUpperCase = CsvConfigType.findByName(upperCase);
    final CsvConfigType resultLowerCase = CsvConfigType.findByName(lowerCase);

    // then
    assertThat(resultExactSame).isEqualTo(CsvConfigType.WATCHLIST);
    assertThat(resultUpperCase).isEqualTo(CsvConfigType.WATCHLIST);
    assertThat(resultLowerCase).isEqualTo(CsvConfigType.WATCHLIST);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testFilterList() {
    // given
    final String exactSameString = "filterList";
    final String upperCase = exactSameString.toUpperCase();
    final String lowerCase = exactSameString.toLowerCase();

    // when
    final CsvConfigType resultExactSame = CsvConfigType.findByName(exactSameString);
    final CsvConfigType resultUpperCase = CsvConfigType.findByName(upperCase);
    final CsvConfigType resultLowerCase = CsvConfigType.findByName(lowerCase);

    // then
    assertThat(resultExactSame).isEqualTo(CsvConfigType.FILTERLIST);
    assertThat(resultUpperCase).isEqualTo(CsvConfigType.FILTERLIST);
    assertThat(resultLowerCase).isEqualTo(CsvConfigType.FILTERLIST);
  }
}
package io.github.manami.persistence.exporter.csv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

import io.github.manami.persistence.exporter.csv.CsvConfig.CsvConfigType;

public class CsvConfigTest {

    @Test
    public void testThatNonMatchableStringReturnsNull() {
        // given

        // when
        final CsvConfigType result = CsvConfigType.findByName("anyNonMatchableString");

        // then
        assertThat(result, equalTo(null));
    }


    @Test
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
        assertThat(resultExactSame, equalTo(CsvConfigType.ANIMELIST));
        assertThat(resultUpperCase, equalTo(CsvConfigType.ANIMELIST));
        assertThat(resultLowerCase, equalTo(CsvConfigType.ANIMELIST));
    }


    @Test
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
        assertThat(resultExactSame, equalTo(CsvConfigType.WATCHLIST));
        assertThat(resultUpperCase, equalTo(CsvConfigType.WATCHLIST));
        assertThat(resultLowerCase, equalTo(CsvConfigType.WATCHLIST));
    }


    @Test
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
        assertThat(resultExactSame, equalTo(CsvConfigType.FILTERLIST));
        assertThat(resultUpperCase, equalTo(CsvConfigType.FILTERLIST));
        assertThat(resultLowerCase, equalTo(CsvConfigType.FILTERLIST));
    }
}

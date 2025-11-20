package io.github.manamiproject.manami.app.search

import io.github.manamiproject.manami.app.search.FindByCriteriaConfig.ScoreType.*
import io.github.manamiproject.manami.app.search.FindByCriteriaConfig.SearchConjunction.*
import io.github.manamiproject.modb.core.extensions.EMPTY
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class FindByCriteriaConfigTest {

    @Nested
    inner class SearchConjunctionTests {

        @ParameterizedTest
        @ValueSource(strings = ["and", "AND", "AnD"])
        fun `return AND`(value: String) {
            // when
            val result = FindByCriteriaConfig.SearchConjunction.of(value)

            // then
            assertThat(result).isEqualTo(AND)
        }

        @ParameterizedTest
        @ValueSource(strings = ["or", "OR", "oR"])
        fun `return OR`(value: String) {
            // when
            val result = FindByCriteriaConfig.SearchConjunction.of(value)

            // then
            assertThat(result).isEqualTo(OR)
        }

        @ParameterizedTest
        @ValueSource(strings = [EMPTY, "   ", "example"])
        fun `throws exception if given string doesn't match any enum value`(value: String) {
            // when
            val result = assertThrows<IllegalArgumentException> {
                FindByCriteriaConfig.SearchConjunction.of(value)
            }

            // then
            assertThat(result).hasMessage("No value for [$value]")
        }
    }

    @Nested
    inner class ScoreTypeTests {

        @ParameterizedTest
        @ValueSource(strings = [
            "arithmetic_geometric_mean",
            "ARITHMETIC_GEOMETRIC_MEAN",
            "aRiThMeTiC_gEoMeTrIc_MeAn",
            "arithmetic-geometric-mean",
            "ARITHMETIC-GEOMETRIC-MEAN",
            "aRiThMeTiC-gEoMeTrIc-MeAn",
        ])
        fun `return ARITHMETIC_GEOMETRIC_MEAN`(value: String) {
            // when
            val result = FindByCriteriaConfig.ScoreType.of(value)

            // then
            assertThat(result).isEqualTo(ARITHMETIC_GEOMETRIC_MEAN)
        }

        @ParameterizedTest
        @ValueSource(strings = [
            "arithmetic_mean",
            "ARITHMETIC_MEAN",
            "aRiThMeTiC_MeAn",
            "arithmetic-mean",
            "ARITHMETIC-MEAN",
            "aRiThMeTiC-MeAn",
        ])
        fun `return ARITHMETIC_MEAN`(value: String) {
            // when
            val result = FindByCriteriaConfig.ScoreType.of(value)

            // then
            assertThat(result).isEqualTo(ARITHMETIC_MEAN)
        }

        @ParameterizedTest
        @ValueSource(strings = [
            "median",
            "MEDIAN",
            "mEdIaN",
        ])
        fun `return MEDIAN`(value: String) {
            // when
            val result = FindByCriteriaConfig.ScoreType.of(value)

            // then
            assertThat(result).isEqualTo(MEDIAN)
        }

        @ParameterizedTest
        @ValueSource(strings = [EMPTY, "   ", "example"])
        fun `throws exception if given string doesn't match any enum value`(value: String) {
            // when
            val result = assertThrows<IllegalArgumentException> {
                FindByCriteriaConfig.ScoreType.of(value)
            }

            // then
            assertThat(result).hasMessage("No value for [$value]")
        }
    }
}
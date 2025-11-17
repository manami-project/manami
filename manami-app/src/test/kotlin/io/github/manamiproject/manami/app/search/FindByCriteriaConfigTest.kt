package io.github.manamiproject.manami.app.search

import io.github.manamiproject.manami.app.search.FindByCriteriaConfig.SearchConjunction.AND
import io.github.manamiproject.manami.app.search.FindByCriteriaConfig.SearchConjunction.OR
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
}
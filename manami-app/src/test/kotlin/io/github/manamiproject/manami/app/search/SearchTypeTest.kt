package io.github.manamiproject.manami.app.search

import io.github.manamiproject.manami.app.search.SearchType.AND
import io.github.manamiproject.manami.app.search.SearchType.OR
import io.github.manamiproject.modb.core.extensions.EMPTY
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class SearchTypeTest {

    @ParameterizedTest
    @ValueSource(strings = ["and", "AND", "AnD"])
    fun `return AND`(value: String) {
        // when
        val result = SearchType.of(value)

        // then
        assertThat(result).isEqualTo(AND)
    }

    @ParameterizedTest
    @ValueSource(strings = ["or", "OR", "oR"])
    fun `return OR`(value: String) {
        // when
        val result = SearchType.of(value)

        // then
        assertThat(result).isEqualTo(OR)
    }

    @ParameterizedTest
    @ValueSource(strings = [EMPTY, "   ", "example"])
    fun `throws exception if given string doesn't match any enum value`(value: String) {
        // when
        val result = assertThrows<IllegalArgumentException> {
            SearchType.of(value)
        }

        // then
        assertThat(result).hasMessage("No value for [$value]")
    }
}
package io.github.manamiproject.manami.app

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test


internal class ManamiTest {

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = Manami.instance

            // when
            val result = Manami.instance

            // then
            assertThat(result).isExactlyInstanceOf(Manami::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}
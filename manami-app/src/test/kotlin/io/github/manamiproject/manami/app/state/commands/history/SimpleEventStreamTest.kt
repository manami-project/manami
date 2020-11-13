package io.github.manamiproject.manami.app.state.commands.history

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SimpleEventStreamTest {

    @Nested
    inner class PreviousTests {

        @Test
        fun `returns false if EventStream is empty`() {
            // given
            val eventStream = SimpleEventStream<String>()

            // when
            val result = eventStream.previous()

            // then
            assertThat(result).isFalse()
        }

        @Test
        fun `returns true if the EventStream contains exactly one element, because you can always return to the initial state`() {
            // given
            val eventStream = SimpleEventStream<String>().apply {
                add("A")
            }

            // when
            val result = eventStream.previous()

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `returns true if the EventStream contains at least two elements and going back one step is possible`() {
            // given
            val eventStream = SimpleEventStream<String>().apply {
                add("A")
                add("B")
            }

            // when
            val result = eventStream.previous()

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `returns true if calling previous made the cursor reach the end, because you can always go back to the initial state`() {
            // given
            val eventStream = SimpleEventStream<String>().apply {
                add("A")
                add("B")
                previous()
            }

            // when
            val result = eventStream.previous()

            // then
            assertThat(result).isTrue()
        }
    }

    @Nested
    inner class NextTests {

        @Test
        fun `returns false if EventStream is empty`() {
            // given
            val eventStream = SimpleEventStream<String>()

            // when
            val result = eventStream.next()

            // then
            assertThat(result).isFalse()
        }

        @Test
        fun `returns false if the EventStream contains exactly one element`() {
            // given
            val eventStream = SimpleEventStream<String>().apply {
                add("A")
            }

            // when
            val result = eventStream.next()

            // then
            assertThat(result).isFalse()
        }

        @Test
        fun `returns true if the EventStream contains at least two elements, points to a previous element which makes calling next possible`() {
            // given
            val eventStream = SimpleEventStream<String>().apply {
                add("A")
                add("B")
                previous()
            }

            // when
            val result = eventStream.next()

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `returns false if calling next made the cursor reach the end`() {
            // given
            val eventStream = SimpleEventStream<String>().apply {
                add("A")
                add("B")
                previous()
                next()
            }

            // when
            val result = eventStream.next()

            // then
            assertThat(result).isFalse()
        }
    }

    @Nested
    inner class AddTests {

        @Test
        fun `successfully add element to an empty EventStream which causes the cursor to show the current element`() {
            // given
            val eventStream = SimpleEventStream<String>()

            // when
            eventStream.add("A")

            // then
            assertThat(eventStream.element()).isEqualTo("A")
        }

        @Test
        fun `successfully append an element to the end of the EventStream`() {
            // given
            val eventStream = SimpleEventStream<String>().apply {
                add("A")
            }

            // when
            eventStream.add("B")

            // then
            assertThat(eventStream.element()).isEqualTo("B")
        }

        @Test
        fun `add a new element at the cursor's current position and remove any events after`() {
            // given
            val eventStream = SimpleEventStream<String>().apply {
                add("A")
                add("B")
                add("C")
                add("D")
                previous()
                previous()
            }

            // when
            eventStream.add("E")

            // then
            assertThat(eventStream.element()).isEqualTo("E")
            assertThat(eventStream.next()).isFalse()
            eventStream.previous()
            assertThat(eventStream.element()).isEqualTo("B")
        }
    }

    @Nested
    inner class ElementTests {

        @Test
        fun `throws exception if element is called on an empty EventStream`() {
            // given
            val eventStream = SimpleEventStream<String>()

            // when
            val result = assertThrows<IllegalStateException> {
                eventStream.element()
            }

            // then
            assertThat(result).hasMessage("Cannot retrieve element from empty EventStream.")
        }

        @Test
        fun `retrieve element if EventStream contains exactly one element`() {
            // given
            val eventStream = SimpleEventStream<String>().apply {
                add("A")
            }

            // when
            val result = eventStream.element()

            // then
            assertThat(result).isEqualTo("A")
        }

        @Test
        fun `retrieve newly added element if EventStream contains more than one element`() {
            // given
            val eventStream = SimpleEventStream<String>().apply {
                add("A")
                add("B")
            }

            // when
            val result = eventStream.element()

            // then
            assertThat(result).isEqualTo("B")
        }

        @Test
        fun `retrieve correct element after calling previous`() {
            // given
            val eventStream = SimpleEventStream<String>().apply {
                add("A")
                add("B")
                previous()
            }

            // when
            val result = eventStream.element()

            // then
            assertThat(result).isEqualTo("A")
        }

        @Test
        fun `retrieve correct element after calling next`() {
            // given
            val eventStream = SimpleEventStream<String>().apply {
                add("A")
                add("B")
                add("C")
                previous()
                next()
            }

            // when
            val result = eventStream.element()

            // then
            assertThat(result).isEqualTo("C")
        }
    }

    @Nested
    inner class ClearTests {

        @Test
        fun `clear all elements, but leave the initial state`() {
            // given
            val eventStream = SimpleEventStream<String>().apply {
                add("A")
                add("B")
            }

            // when
            eventStream.clear()

            // then
            val result = assertThrows<IllegalStateException> {
                eventStream.element()
            }
            assertThat(result).hasMessage("Cannot retrieve element from empty EventStream.")
        }
    }

    @Nested
    inner class HasPreviousTests {

        @Test
        fun `returns false for empty event stream`() {
            // given
            val eventStream = SimpleEventStream<String>()

            // when
            val result = eventStream.hasPrevious()

            // then
            assertThat(result).isFalse()
        }

        @Test
        fun `returns true for non-empty list having the cursor pointing to the only element because you can always to back to the initial state`() {
            // given
            val eventStream = SimpleEventStream<String>().apply {
                add("A")
            }

            // when
            val result = eventStream.hasPrevious()

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `returns false for non-empty list having the cursor pointing to the initial state`() {
            // given
            val eventStream = SimpleEventStream<String>().apply {
                add("A")
                previous()
            }

            // when
            val result = eventStream.hasPrevious()

            // then
            assertThat(result).isFalse()
        }
    }

    @Nested
    inner class HasNextTests {

        @Test
        fun `returns false for empty event stream`() {
            // given
            val eventStream = SimpleEventStream<String>()

            // when
            val result = eventStream.hasNext()

            // then
            assertThat(result).isFalse()
        }

        @Test
        fun `returns false for non-empty list having the cursor pointing to the only element because it marks the end of the stream`() {
            // given
            val eventStream = SimpleEventStream<String>().apply {
                add("A")
            }

            // when
            val result = eventStream.hasNext()

            // then
            assertThat(result).isFalse()
        }

        @Test
        fun `returns true for non-empty list having the cursor pointing to the initial state`() {
            // given
            val eventStream = SimpleEventStream<String>().apply {
                add("A")
                previous()
            }

            // when
            val result = eventStream.hasNext()

            // then
            assertThat(result).isTrue()
        }
    }

    @Nested
    inner class CropTests {

        @Test
        fun `does nothing on an empty stream`() {
            // given
            val eventStream = SimpleEventStream<String>()

            // when
            eventStream.crop()

            // then
            assertThat(eventStream.hasPrevious()).isFalse()
            assertThat(eventStream.hasNext()).isFalse()
        }

        @Test
        fun `removes all elements past the current cursor position`() {
            // given
            val eventStream = SimpleEventStream<String>().apply {
                add("1")
                add("2")
                add("3")
                add("4")
                add("5")
                previous()
                previous()
                previous()
            }

            // when
            eventStream.crop()

            // then
            assertThat(eventStream.element()).isEqualTo("2")
            assertThat(eventStream.hasNext()).isFalse()
        }
    }
}
package io.github.manamiproject.manami.app.state.events

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.Thread.sleep

internal class EventBusTest {

    @Test
    fun `throws exception if there is no subscriber for the given event`() {
        // when
        val result = assertThrows<IllegalStateException> {
            EventBus.post(TestEvent)
        }

        // then
        assertThat(result).hasMessage("No subscriber for given event [class io.github.manamiproject.manami.app.state.events.TestEvent]")
    }

    @Test
    fun `throws exception if subscriber doesn't provide a function with subscribe annotation and exactly one parameter of type event`() {
        // when
        val result = assertThrows<IllegalStateException> {
            EventBus.subscribe(this)
        }

        // then
        assertThat(result).hasMessage("EventBus subscriber does not provide a function annotated with @Subscribe")
    }

    @Test
    fun `subscriber receives event`() {
        // given
        val testSubscriber = TestSubscriber()

        EventBus.subscribe(testSubscriber)

        // when
        EventBus.post(TestEvent)

        // then
        sleep(500)
        assertThat(testSubscriber.hasBeenInvoked).isTrue()

        EventBus.unsubscribe(testSubscriber)
    }

    @Nested
    inner class UnsubscribeTests {

        @Test
        fun `no subscriber after unsubscribe`() {
            // given
            val testSubscriber = TestSubscriber()

            EventBus.subscribe(testSubscriber)

            // when
            EventBus.unsubscribe(testSubscriber)
            val result = assertThrows<IllegalStateException> {
                EventBus.post(TestEvent)
            }

            // then
            assertThat(testSubscriber.hasBeenInvoked).isFalse()
            assertThat(result).hasMessage("No subscriber for given event [class io.github.manamiproject.manami.app.state.events.TestEvent]")
        }

        @Test
        fun `one subscriber left after unsubscribing`() {
            // given
            val testSubscriber = TestSubscriber()
            val remainingSubscriber = TestSubscriber()

            EventBus.subscribe(testSubscriber)
            EventBus.subscribe(remainingSubscriber)

            // when
            EventBus.unsubscribe(testSubscriber)
            EventBus.post(TestEvent)

            // then
            sleep(500)
            assertThat(testSubscriber.hasBeenInvoked).isFalse()
            assertThat(remainingSubscriber.hasBeenInvoked).isTrue()
        }
    }
}


class TestSubscriber(var hasBeenInvoked: Boolean = false) {

    @Subscribe
    @Suppress("UNUSED_PARAMETER")
    fun receive(test: TestEvent) {
        hasBeenInvoked = true
    }
}

object TestEvent: Event
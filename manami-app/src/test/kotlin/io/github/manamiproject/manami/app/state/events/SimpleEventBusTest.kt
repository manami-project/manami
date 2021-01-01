package io.github.manamiproject.manami.app.state.events

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.Thread.sleep

internal class SimpleEventBusTest {

    @Test
    fun `throws exception if there is no subscriber for the given event`() {
        // when
        val result = assertThrows<IllegalStateException> {
            SimpleEventBus.post(TestEvent)
        }

        // then
        assertThat(result).hasMessage("No subscriber for given event [class io.github.manamiproject.manami.app.state.events.TestEvent]")
    }

    @Test
    fun `throws exception if subscriber doesn't provide a function with subscribe annotation and exactly one parameter of type event`() {
        // when
        val result = assertThrows<IllegalStateException> {
            SimpleEventBus.subscribe(this)
        }

        // then
        assertThat(result).hasMessage("EventBus subscriber does not provide a function annotated with @Subscribe")
    }

    @Test
    fun `subscriber receives event`() {
        // given
        val testSubscriber = TestSubscriber()

        SimpleEventBus.subscribe(testSubscriber)

        // when
        SimpleEventBus.post(TestEvent)

        // then
        sleep(500)
        assertThat(testSubscriber.hasBeenInvoked).isTrue()

        SimpleEventBus.unsubscribe(testSubscriber)
    }

    @Nested
    inner class UnsubscribeTests {

        @Test
        fun `no subscriber after unsubscribe`() {
            // given
            val testSubscriber = TestSubscriber()

            SimpleEventBus.subscribe(testSubscriber)

            // when
            SimpleEventBus.unsubscribe(testSubscriber)
            val result = assertThrows<IllegalStateException> {
                SimpleEventBus.post(TestEvent)
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

            SimpleEventBus.subscribe(testSubscriber)
            SimpleEventBus.subscribe(remainingSubscriber)

            // when
            SimpleEventBus.unsubscribe(testSubscriber)
            SimpleEventBus.post(TestEvent)

            // then
            sleep(500)
            assertThat(testSubscriber.hasBeenInvoked).isFalse()
            assertThat(remainingSubscriber.hasBeenInvoked).isTrue()
            SimpleEventBus.unsubscribe(remainingSubscriber)
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
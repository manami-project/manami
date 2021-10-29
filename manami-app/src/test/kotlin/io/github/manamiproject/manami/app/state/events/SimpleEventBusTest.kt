package io.github.manamiproject.manami.app.state.events

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.Thread.sleep

internal class SimpleEventBusTest {

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
    fun `subscriber receives event - event interface`() {
        // given
        val testSubscriber = TestSubscriberGenericEventParameter()

        SimpleEventBus.subscribe(testSubscriber)

        // when
        SimpleEventBus.post(TestEvent)

        // then
        sleep(500)
        assertThat(testSubscriber.hasBeenInvoked).isTrue()

        SimpleEventBus.unsubscribe(testSubscriber)
    }

    @Test
    fun `subscriber receives event - specific type`() {
        // given
        val testSubscriber = TestSubscriberSpecificEventParameter()

        SimpleEventBus.subscribe(testSubscriber)

        // when
        SimpleEventBus.post(TestEvent)

        // then
        sleep(500)
        assertThat(testSubscriber.hasBeenInvoked).isTrue()

        SimpleEventBus.unsubscribe(testSubscriber)
    }

    @Test
    fun `subscriber receives event in multiple functions`() {
        // given
        val testSubscriber = TestSubscriberMultipleFunctions()

        SimpleEventBus.subscribe(testSubscriber)

        // when
        SimpleEventBus.post(TestEvent)

        // then
        sleep(500)
        assertThat(testSubscriber.hasBeenInvoked1).isTrue()
        assertThat(testSubscriber.hasBeenInvoked2).isTrue()

        SimpleEventBus.unsubscribe(testSubscriber)
    }

    @Nested
    inner class SubscribeTests {

        @Test
        fun `throws exception if no type is passed as parameter in annotation`() {
            // given
            val testSubscriber = TestSubscriberMissingParameter()

            // when
            val result = assertThrows<IllegalStateException> {
                SimpleEventBus.subscribe(testSubscriber)
            }

            // then
            assertThat(result).hasMessage("Annotation @Subscribe does not provide any types")
        }
    }

    @Nested
    inner class UnsubscribeTests {

        @Test
        fun `no subscriber after unsubscribe`() {
            // given
            val testSubscriber = TestSubscriberGenericEventParameter()

            SimpleEventBus.subscribe(testSubscriber)

            // when
            SimpleEventBus.unsubscribe(testSubscriber)
            SimpleEventBus.post(TestEvent)

            // then
            assertThat(testSubscriber.hasBeenInvoked).isFalse()
        }

        @Test
        fun `one subscriber left after unsubscribing`() {
            // given
            val testSubscriber = TestSubscriberGenericEventParameter()
            val remainingSubscriber = TestSubscriberGenericEventParameter()

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

class TestSubscriberMissingParameter {

    @Subscribe
    @Suppress("UNUSED_PARAMETER")
    fun receive(test: Event) {}
}


class TestSubscriberGenericEventParameter(var hasBeenInvoked: Boolean = false) {

    @Subscribe(TestEvent::class)
    @Suppress("UNUSED_PARAMETER")
    fun receive(test: Event) {
        hasBeenInvoked = true
    }
}

class TestSubscriberSpecificEventParameter(var hasBeenInvoked: Boolean = false) {

    @Subscribe(TestEvent::class)
    @Suppress("UNUSED_PARAMETER")
    fun receive(test: TestEvent) {
        hasBeenInvoked = true
    }
}

class TestSubscriberMultipleFunctions(
    var hasBeenInvoked1: Boolean = false,
    var hasBeenInvoked2: Boolean = false,
) {

    @Subscribe(TestEvent::class)
    @Suppress("UNUSED_PARAMETER")
    fun receiveFunction1(test: TestEvent) {
        hasBeenInvoked1 = true
    }

    @Subscribe(TestEvent::class)
    @Suppress("UNUSED_PARAMETER")
    fun receiveFunction2(test: TestEvent) {
        hasBeenInvoked2 = true
    }
}

object TestEvent: Event
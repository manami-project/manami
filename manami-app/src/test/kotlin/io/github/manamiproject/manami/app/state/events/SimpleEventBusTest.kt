package io.github.manamiproject.manami.app.state.events

import io.github.manamiproject.modb.test.shouldNotBeInvoked
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
        assertThat(result).hasMessage("Either the EventBus subscriber does not provide a function annotated with @Subscribe or the respective functions does not provide a single Parameter of a type which implements Event.")
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

    @Test
    fun `receives all events if no event type has been configures in annotation`() {
        // given
        val testSubscriber = TestSubscriberMissingParameter()

        SimpleEventBus.subscribe(testSubscriber)

        // when
        SimpleEventBus.post(TestEvent)
        SimpleEventBus.post(OtherTestEvent)

        // then
        sleep(500)
        assertThat(testSubscriber.receivedEvents.filterIsInstance<TestEvent>()).hasSize(1)
        assertThat(testSubscriber.receivedEvents.filterIsInstance<OtherTestEvent>()).hasSize(1)
    }

    @Nested
    inner class SubscribeTests {

        @Test
        fun `throws exception, because the parameter doesn't implement Event`() {
            // given
            val testSubscriber = TestSubscriberWithHavingParameterOfDifferentTypeThanEvent()

            // when
            val result = assertThrows<IllegalStateException> {
                SimpleEventBus.subscribe(testSubscriber)
            }

            // then
            assertThat(result).hasMessage("Either the EventBus subscriber does not provide a function annotated with @Subscribe or the respective functions does not provide a single Parameter of a type which implements Event.")

            SimpleEventBus.unsubscribe(testSubscriber)
        }

        @Test
        fun `throws exception, because the annotated functions contains more than one parameter`() {
            // given
            val testSubscriber = TestSubscriberMultipleParameters()

            // when
            val result = assertThrows<IllegalStateException> {
                SimpleEventBus.subscribe(testSubscriber)
            }

            // then
            assertThat(result).hasMessage("Either the EventBus subscriber does not provide a function annotated with @Subscribe or the respective functions does not provide a single Parameter of a type which implements Event.")

            SimpleEventBus.unsubscribe(testSubscriber)
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

class TestSubscriberMissingParameter(val receivedEvents: MutableList<Event> = mutableListOf()) {

    @Subscribe
    @Suppress("UNUSED_PARAMETER")
    fun receive(test: Event) {
        receivedEvents.add(test)
    }
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

class TestSubscriberMultipleParameters {

    @Subscribe(TestEvent::class)
    @Suppress("UNUSED_PARAMETER")
    fun receive(param1: TestEvent, param2: Event) {
        shouldNotBeInvoked()
    }
}

class TestSubscriberWithHavingParameterOfDifferentTypeThanEvent {

    @Subscribe(TestEvent::class)
    @Suppress("UNUSED_PARAMETER")
    fun receive(test: Int) {
        shouldNotBeInvoked()
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

object OtherTestEvent: Event
package io.github.manamiproject.manami.app.state.events

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.events.ListChangedEvent.EventType.ADDED
import io.github.manamiproject.manami.app.state.events.ListChangedEvent.EventType.REMOVED
import io.github.manamiproject.manami.app.state.events.ListChangedEvent.ListType.IGNORE_LIST
import io.github.manamiproject.manami.app.state.events.ListChangedEvent.ListType.WATCH_LIST
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI

internal class EventfulListTest {

    @Nested
    inner class AddTests {

        @Test
        fun `successfully add entry and fire an event`() {
            // given
            var firedEvent: ListChangedEvent<*>? = null

            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    firedEvent = (event as ListChangedEvent<*>)
                }
            }

            val list = EventfulList<WatchListEntry>(
                eventBus = testEventBus,
                listType = WATCH_LIST,
            )

            val entry = WatchListEntry(
                link = Link("https://myanimelist.net/anime/1535"),
                title = "Death Note",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg")
            )

            // when
            val result = list.add(entry)

            // then
            assertThat(result).isTrue()
            assertThat(firedEvent?.type).isEqualTo(ADDED)
            assertThat(firedEvent?.list).isEqualTo(WATCH_LIST)
            assertThat(firedEvent?.obj).containsExactly(entry)
            assertThat(list).containsExactly(entry)
        }

        @Test
        fun `do nothing if the entry already exist`() {
            // given
            var firedEvent: ListChangedEvent<*>? = null

            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    firedEvent = (event as ListChangedEvent<*>)
                }
            }

            val existingEntry = WatchListEntry(
                link = Link("https://myanimelist.net/anime/1535"),
                title = "Death Note",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg")
            )

            val list = EventfulList(
                eventBus = testEventBus,
                listType = WATCH_LIST,
                list = mutableListOf(existingEntry)
            )

            // when
            val result = list.add(existingEntry)

            // then
            assertThat(result).isFalse()
            assertThat(firedEvent).isNull()
            assertThat(list).containsExactly(existingEntry)
        }
    }

    @Nested
    inner class AddOnIndexTests {

        @Test
        fun `successfully add entry which doesn't exist in list and fire an event`() {
            // given
            var firedEvent: ListChangedEvent<*>? = null

            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    firedEvent = (event as ListChangedEvent<*>)
                }
            }

            val existingEntry1 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg")
            )
            val existingEntry2 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg")
            )
            val existingEntry3 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )

            val list = EventfulList(
                eventBus = testEventBus,
                listType = WATCH_LIST,
                list = mutableListOf(
                    existingEntry1,
                    existingEntry2,
                    existingEntry3,
                ),
            )

            val newEntry = WatchListEntry(
                link = Link("https://myanimelist.net/anime/1535"),
                title = "Death Note",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg")
            )

            // when
            list.add(1, newEntry)

            // then
            assertThat(firedEvent?.type).isEqualTo(ADDED)
            assertThat(firedEvent?.list).isEqualTo(WATCH_LIST)
            assertThat(firedEvent?.obj).containsExactly(newEntry)
            assertThat(list).containsExactly(existingEntry1, newEntry, existingEntry2, existingEntry3)
        }

        @Test
        fun `move existing entry to specified index, but don't fire an event`() {
            // given
            var firedEvent: ListChangedEvent<*>? = null

            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    firedEvent = (event as ListChangedEvent<*>)
                }
            }

            val existingEntry1 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg")
            )
            val existingEntry2 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg")
            )
            val existingEntry3 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )

            val entryToBeAdded = WatchListEntry(
                link = Link("https://myanimelist.net/anime/1535"),
                title = "Death Note",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg")
            )

            val list = EventfulList(
                eventBus = testEventBus,
                listType = WATCH_LIST,
                list = mutableListOf(
                    existingEntry1,
                    existingEntry2,
                    existingEntry3,
                    entryToBeAdded,
                ),
            )

            // when
            list.add(1, entryToBeAdded)

            // then
            assertThat(firedEvent).isNull()
            assertThat(list).containsExactly(existingEntry1, entryToBeAdded, existingEntry2, existingEntry3)
        }

        @Test
        fun `throws exception if the index hasn't been populated`() {
            // given
            var firedEvent: ListChangedEvent<*>? = null

            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    firedEvent = (event as ListChangedEvent<*>)
                }
            }

            val entry = WatchListEntry(
                link = Link("https://myanimelist.net/anime/1535"),
                title = "Death Note",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg")
            )

            val list = EventfulList(
                eventBus = testEventBus,
                listType = WATCH_LIST,
                list = mutableListOf(entry)
            )

            // when
            val result = assertThrows<IllegalArgumentException> {
                list.add(1, entry)
            }

            // then
            assertThat(firedEvent).isNull()
            assertThat(result).hasMessage("Cannot add on unpopulated index.")
            assertThat(list).containsExactly(entry)
        }
    }

    @Nested
    inner class AddCollectionTests {

        @Test
        fun `successfully add entries which don't exist in list and fire an event`() {
            // given
            var firedEvent: ListChangedEvent<*>? = null

            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    firedEvent = (event as ListChangedEvent<*>)
                }
            }

            val entry1 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg")
            )
            val entry2 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg")
            )
            val entry3 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )

            val list = EventfulList<WatchListEntry>(
                eventBus = testEventBus,
                listType = WATCH_LIST,
            )

            // when
            list.addAll(listOf(entry1, entry2, entry3))

            // then
            assertThat(firedEvent?.type).isEqualTo(ADDED)
            assertThat(firedEvent?.list).isEqualTo(WATCH_LIST)
            assertThat(firedEvent?.obj).containsExactly(entry1, entry2, entry3)
            assertThat(list).containsExactly(entry1, entry2, entry3)
        }

        @Test
        fun `try to add entries which already exist and some which don't exist - event contains only new entries`() {
            // given
            var firedEvent: ListChangedEvent<*>? = null

            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    firedEvent = (event as ListChangedEvent<*>)
                }
            }

            val existingEntry1 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg")
            )
            val existingEntry2 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg")
            )

            val newEntry1 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )
            val newEntry2 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/1535"),
                title = "Death Note",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg")
            )

            val list = EventfulList(
                eventBus = testEventBus,
                listType = WATCH_LIST,
                list = mutableListOf(
                    existingEntry1,
                    existingEntry2,
                ),
            )

            // when
            list.addAll(listOf(newEntry1, newEntry2))

            // then
            assertThat(firedEvent?.type).isEqualTo(ADDED)
            assertThat(firedEvent?.list).isEqualTo(WATCH_LIST)
            assertThat(firedEvent?.obj).containsExactly(newEntry1, newEntry2)
            assertThat(list).containsExactly(existingEntry1, existingEntry2, newEntry1, newEntry2)
        }
    }

    @Nested
    inner class AddCollectionOnIndexTests {

        @Test
        fun `successfully add entries which don't exist in list and fire an event`() {
            // given
            var firedEvent: ListChangedEvent<*>? = null

            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    firedEvent = (event as ListChangedEvent<*>)
                }
            }

            val existingEntry1 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg")
            )
            val existingEntry2 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg")
            )

            val newEntry1 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )
            val newEntry2 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/1535"),
                title = "Death Note",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg")
            )

            val list = EventfulList(
                eventBus = testEventBus,
                listType = WATCH_LIST,
                list = mutableListOf(
                    existingEntry1,
                    existingEntry2,
                )
            )

            // when
            list.addAll(1, listOf(newEntry1, newEntry2))

            // then
            assertThat(firedEvent?.type).isEqualTo(ADDED)
            assertThat(firedEvent?.list).isEqualTo(WATCH_LIST)
            assertThat(firedEvent?.obj).containsExactly(newEntry1, newEntry2)
            assertThat(list).containsExactly(existingEntry1, newEntry1, newEntry2, existingEntry2)
        }

        @Test
        fun `rearrange a list of entries and don't fire an event`() {
            // given
            var firedEvent: ListChangedEvent<*>? = null

            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    firedEvent = (event as ListChangedEvent<*>)
                }
            }

            val existingEntry1 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg")
            )
            val existingEntry2 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg")
            )
            val existingEntry3 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )
            val existingEntry4 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/1535"),
                title = "Death Note",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg")
            )

            val list = EventfulList(
                eventBus = testEventBus,
                listType = WATCH_LIST,
                list = mutableListOf(
                    existingEntry1,
                    existingEntry2,
                    existingEntry3,
                    existingEntry4,
                ),
            )

            // when
            list.addAll(1, listOf(existingEntry4, existingEntry1))

            // then
            assertThat(firedEvent).isNull()
            assertThat(list).containsExactly(existingEntry2, existingEntry4, existingEntry1, existingEntry3)
        }
    }

    @Nested
    inner class RemoveTests {

        @Test
        fun `successfully remove an entry and fire an event`() {
            // given
            var firedEvent: ListChangedEvent<*>? = null

            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    firedEvent = (event as ListChangedEvent<*>)
                }
            }

            val entry = WatchListEntry(
                link = Link("https://myanimelist.net/anime/1535"),
                title = "Death Note",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg")
            )

            val list = EventfulList(
                eventBus = testEventBus,
                listType = WATCH_LIST,
                list = mutableListOf(
                    entry
                )
            )

            // when
            val result = list.remove(entry)

            // then
            assertThat(result).isTrue()
            assertThat(firedEvent?.type).isEqualTo(REMOVED)
            assertThat(firedEvent?.list).isEqualTo(WATCH_LIST)
            assertThat(firedEvent?.obj).containsExactly(entry)
            assertThat(list).isEmpty()
        }

        @Test
        fun `do nothing if the entry doesn't exist`() {
            // given
            var firedEvent: ListChangedEvent<*>? = null

            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    firedEvent = (event as ListChangedEvent<*>)
                }
            }

            val existingEntry = WatchListEntry(
                link = Link("https://myanimelist.net/anime/1535"),
                title = "Death Note",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg")
            )

            val list = EventfulList(
                eventBus = testEventBus,
                listType = WATCH_LIST,
                list = mutableListOf(existingEntry)
            )

            val otherEntry = WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )

            // when
            val result = list.remove(otherEntry)

            // then
            assertThat(result).isFalse()
            assertThat(firedEvent).isNull()
            assertThat(list).containsExactly(existingEntry)
        }
    }

    @Nested
    inner class RemoveAtTests {

        @Test
        fun `remove entry at specific index`() {
            // given
            var firedEvent: ListChangedEvent<*>? = null

            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    firedEvent = (event as ListChangedEvent<*>)
                }
            }

            val existingEntry1 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg")
            )
            val existingEntry2 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg")
            )
            val existingEntry3 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )
            val existingEntry4 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/1535"),
                title = "Death Note",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg")
            )

            val list = EventfulList(
                eventBus = testEventBus,
                listType = WATCH_LIST,
                list = mutableListOf(
                    existingEntry1,
                    existingEntry2,
                    existingEntry3,
                    existingEntry4,
                )
            )

            // when
            list.removeAt(2)

            // then
            assertThat(firedEvent?.type).isEqualTo(REMOVED)
            assertThat(firedEvent?.list).isEqualTo(WATCH_LIST)
            assertThat(firedEvent?.obj).containsExactly(existingEntry3)
            assertThat(list).containsExactly(existingEntry1, existingEntry2, existingEntry4)
        }

        @Test
        fun `throws exception if the index hasn't been populated`() {
            // given
            var firedEvent: ListChangedEvent<*>? = null

            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    firedEvent = (event as ListChangedEvent<*>)
                }
            }

            val entry = WatchListEntry(
                link = Link("https://myanimelist.net/anime/1535"),
                title = "Death Note",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg")
            )

            val list = EventfulList(
                eventBus = testEventBus,
                listType = WATCH_LIST,
                list = mutableListOf(entry)
            )

            // when
            val result = assertThrows<IllegalArgumentException> {
                list.removeAt(1)
            }

            // then
            assertThat(firedEvent).isNull()
            assertThat(result).hasMessage("Cannot remove unpopulated index.")
            assertThat(list).containsExactly(entry)
        }
    }

    @Nested
    inner class RemoveAllTests {

        @Test
        fun `remove all entries`() {
            // given
            var firedEvent: ListChangedEvent<*>? = null

            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    firedEvent = (event as ListChangedEvent<*>)
                }
            }

            val existingEntry1 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg")
            )
            val existingEntry2 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg")
            )
            val existingEntry3 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )
            val existingEntry4 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/1535"),
                title = "Death Note",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg")
            )

            val list = EventfulList(
                eventBus = testEventBus,
                listType = WATCH_LIST,
                list = mutableListOf(
                    existingEntry1,
                    existingEntry2,
                    existingEntry3,
                    existingEntry4,
                )
            )

            // when
            list.removeAll(listOf(existingEntry1, existingEntry3))

            // then
            assertThat(firedEvent?.type).isEqualTo(REMOVED)
            assertThat(firedEvent?.list).isEqualTo(WATCH_LIST)
            assertThat(firedEvent?.obj).containsExactly(existingEntry1, existingEntry3)
            assertThat(list).containsExactly(existingEntry2, existingEntry4)
        }

        @Test
        fun `event doesn't contain entries which don't exist in the list`() {
            // given
            var firedEvent: ListChangedEvent<*>? = null

            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    firedEvent = (event as ListChangedEvent<*>)
                }
            }

            val existingEntry1 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg")
            )
            val existingEntry2 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg")
            )
            val existingEntry3 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )

            val nonExistingEntry = WatchListEntry(
                link = Link("https://myanimelist.net/anime/1535"),
                title = "Death Note",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg")
            )

            val list = EventfulList(
                eventBus = testEventBus,
                listType = WATCH_LIST,
                list = mutableListOf(
                    existingEntry1,
                    existingEntry2,
                    existingEntry3,
                )
            )

            // when
            list.removeAll(listOf(existingEntry1, nonExistingEntry, existingEntry3))

            // then
            assertThat(firedEvent?.type).isEqualTo(REMOVED)
            assertThat(firedEvent?.list).isEqualTo(WATCH_LIST)
            assertThat(firedEvent?.obj).containsExactly(existingEntry1, existingEntry3)
            assertThat(list).containsExactly(existingEntry2)
        }
    }

    @Nested
    inner class RemoveIfTests {

        @Test
        fun `remove entries if predicate is true`() {
            // given
            var firedEvent: ListChangedEvent<*>? = null

            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    firedEvent = (event as ListChangedEvent<*>)
                }
            }

            val existingEntry1 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg")
            )
            val existingEntry2 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg")
            )
            val existingEntry3 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )
            val existingEntry4 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/1535"),
                title = "Death Note",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg")
            )

            val list = EventfulList(
                eventBus = testEventBus,
                listType = WATCH_LIST,
                list = mutableListOf(
                    existingEntry1,
                    existingEntry2,
                    existingEntry3,
                    existingEntry4,
                )
            )

            // when
            list.removeIf { it.title.startsWith("Golden") }

            // then
            assertThat(firedEvent?.type).isEqualTo(REMOVED)
            assertThat(firedEvent?.list).isEqualTo(WATCH_LIST)
            assertThat(firedEvent?.obj).containsExactly(existingEntry1, existingEntry2)
            assertThat(list).containsExactly(existingEntry3, existingEntry4)
        }

        @Test
        fun `do nothing if predicate doesn't apply to anything`() {
            // given
            var firedEvent: ListChangedEvent<*>? = null

            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    firedEvent = (event as ListChangedEvent<*>)
                }
            }

            val existingEntry1 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg")
            )
            val existingEntry2 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg")
            )
            val existingEntry3 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )
            val existingEntry4 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/1535"),
                title = "Death Note",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg")
            )

            val list = EventfulList(
                eventBus = testEventBus,
                listType = WATCH_LIST,
                list = mutableListOf(
                    existingEntry1,
                    existingEntry2,
                    existingEntry3,
                    existingEntry4,
                )
            )

            // when
            list.removeIf { it.title.contains("Samurai") }

            // then
            assertThat(firedEvent).isNull()
            assertThat(list).containsExactly(
                existingEntry1,
                existingEntry2,
                existingEntry3,
                existingEntry4,
            )
        }
    }

    @Nested
    inner class RetainAllTests {

        @Test
        fun `remove entries which are not part of the collection to be retained`() {
            // given
            var firedEvent: ListChangedEvent<*>? = null

            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    firedEvent = (event as ListChangedEvent<*>)
                }
            }

            val existingEntry1 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg")
            )
            val existingEntry2 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg")
            )
            val existingEntry3 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )
            val existingEntry4 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/1535"),
                title = "Death Note",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg")
            )

            val list = EventfulList(
                eventBus = testEventBus,
                listType = WATCH_LIST,
                list = mutableListOf(
                    existingEntry1,
                    existingEntry2,
                    existingEntry3,
                    existingEntry4,
                )
            )

            // when
            list.retainAll(listOf(existingEntry2, existingEntry4))

            // then
            assertThat(firedEvent?.type).isEqualTo(REMOVED)
            assertThat(firedEvent?.list).isEqualTo(WATCH_LIST)
            assertThat(firedEvent?.obj).containsExactly(existingEntry1, existingEntry3)
            assertThat(list).containsExactly(existingEntry2, existingEntry4)
        }

        @Test
        fun `remove all entries from list if entries to be retained doesn't contain existing entries`() {
            // given
            var firedEvent: ListChangedEvent<*>? = null

            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    firedEvent = (event as ListChangedEvent<*>)
                }
            }

            val existingEntry1 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg")
            )
            val existingEntry2 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg")
            )

            val nonExistingEntry3 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )
            val nonExistingEntry4 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/1535"),
                title = "Death Note",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg")
            )

            val list = EventfulList(
                eventBus = testEventBus,
                listType = WATCH_LIST,
                list = mutableListOf(
                    existingEntry1,
                    existingEntry2,
                )
            )

            // when
            list.retainAll(listOf(nonExistingEntry3, nonExistingEntry4))

            // then
            assertThat(firedEvent?.type).isEqualTo(REMOVED)
            assertThat(firedEvent?.list).isEqualTo(WATCH_LIST)
            assertThat(firedEvent?.obj).containsExactly(existingEntry1, existingEntry2)
            assertThat(list).isEmpty()
        }
    }

    @Nested
    inner class SetTests {

        @Test
        fun `successfully replace entry`() {
            // given
            val firedEvent: MutableList<ListChangedEvent<*>> = mutableListOf()

            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    firedEvent.add(event as ListChangedEvent<*>)
                }
            }

            val existingEntry1 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg")
            )
            val existingEntry2 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg")
            )
            val existingEntry3 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )

            val newEntry = WatchListEntry(
                link = Link("https://myanimelist.net/anime/1535"),
                title = "Death Note",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg")
            )

            val list = EventfulList(
                eventBus = testEventBus,
                listType = WATCH_LIST,
                list = mutableListOf(
                    existingEntry1,
                    existingEntry2,
                    existingEntry3,
                )
            )

            // when
            val result = list.set(1, newEntry)

            // then
            assertThat(result).isEqualTo(existingEntry2)

            assertThat(firedEvent[0].type).isEqualTo(REMOVED)
            assertThat(firedEvent[0].list).isEqualTo(WATCH_LIST)
            assertThat(firedEvent[0].obj).containsExactly(existingEntry2)

            assertThat(firedEvent[1].type).isEqualTo(ADDED)
            assertThat(firedEvent[1].list).isEqualTo(WATCH_LIST)
            assertThat(firedEvent[1].obj).containsExactly(newEntry)

            assertThat(list).containsExactly(existingEntry1, newEntry, existingEntry3)
        }

        @Test
        fun `don't fire event if the entry is replaces with the same entry`() {
            // given
            val firedEvent: MutableList<ListChangedEvent<*>> = mutableListOf()

            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    firedEvent.add(event as ListChangedEvent<*>)
                }
            }

            val existingEntry1 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg")
            )
            val existingEntry2 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg")
            )
            val existingEntry3 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )

            val list = EventfulList(
                eventBus = testEventBus,
                listType = WATCH_LIST,
                list = mutableListOf(
                    existingEntry1,
                    existingEntry2,
                    existingEntry3,
                )
            )

            // when
            val result = list.set(1, existingEntry2)

            // then
            assertThat(result).isEqualTo(existingEntry2)
            assertThat(firedEvent).isEmpty()
            assertThat(list).containsExactly(existingEntry1, existingEntry2, existingEntry3)
        }

        @Test
        fun `throws exception if index is not populated`() {
            // given
            var firedEvent: ListChangedEvent<*>? = null

            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    firedEvent = (event as ListChangedEvent<*>)
                }
            }

            val existingEntry = WatchListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg")
            )
            val nonExistingEntry = WatchListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg")
            )

            val list = EventfulList(
                eventBus = testEventBus,
                listType = WATCH_LIST,
                list = mutableListOf(
                    existingEntry
                )
            )

            // when
            val result = assertThrows<IllegalArgumentException> {
                list[4] = nonExistingEntry
            }

            // then
            assertThat(firedEvent).isNull()
            assertThat(result).hasMessage("Cannot replace entry on unpopulated index.")
            assertThat(list).containsExactly(
                existingEntry
            )
        }
    }

    @Nested
    inner class ClearTests {

        @Test
        fun `remove all entries and fire an event which contains all removed entries`() {
            // given
            var firedEvent: ListChangedEvent<*>? = null

            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    firedEvent = (event as ListChangedEvent<*>)
                }
            }

            val entry1 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg")
            )
            val entry2 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg")
            )
            val entry3 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )

            val list = EventfulList(
                eventBus = testEventBus,
                listType = WATCH_LIST,
                list = mutableListOf(
                    entry1,
                    entry2,
                    entry3,
                )
            )

            // when
            list.clear()

            // then
            assertThat(firedEvent?.type).isEqualTo(REMOVED)
            assertThat(firedEvent?.list).isEqualTo(WATCH_LIST)
            assertThat(firedEvent?.obj).containsExactly(entry1, entry2, entry3)
            assertThat(list).isEmpty()
        }

        @Test
        fun `don't fire an event on an empty list`() {
            // given
            var firedEvent: ListChangedEvent<*>? = null

            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    firedEvent = (event as ListChangedEvent<*>)
                }
            }

            val list = EventfulList<WatchListEntry>(
                eventBus = testEventBus,
                listType = WATCH_LIST,
            )

            // when
            list.clear()

            // then
            assertThat(firedEvent).isNull()
            assertThat(list).isEmpty()
        }
    }

    @Test
    fun `toString() lists elements`() {
        // given
        val list = EventfulList(
            listType = WATCH_LIST,
            "A",
            "B",
            "C",
        )

        // when
        val result = list.toString()

        // then
        assertThat(result).isEqualTo("[A, B, C]")
    }

    @Nested
    inner class EqualityTests {

        @Test
        fun `two lists are equal having the same elements, but different list types`() {
            // given
            val list1 = EventfulList(
                listType = WATCH_LIST,
                "A",
                "B",
                "C",
            )

            val list2 = EventfulList(
                listType = IGNORE_LIST,
                "C",
                "B",
                "A",
            )

            // when
            val result = list1 == list2

            // then
            assertThat(result).isFalse()
            assertThat(list1).isNotEqualTo(list2)
        }

        @Test
        fun `two lists are equal having the same elements and the same list type`() {
            // given
            val list1 = EventfulList(
                listType = WATCH_LIST,
                "A",
                "B",
                "C",
            )

            val list2 = EventfulList(
                listType = WATCH_LIST,
                "A",
                "B",
                "C",
            )

            // when
            val result = list1 == list2

            // then
            assertThat(result).isTrue()
            assertThat(list1).isEqualTo(list2)
        }

        @Test
        fun `two lists are not equal having the same elements, but in a different order`() {
            // given
            val list1 = EventfulList(
                listType = WATCH_LIST,
                "A",
                "B",
                "C",
            )

            val list2 = EventfulList(
                listType = WATCH_LIST,
                "C",
                "B",
                "A",
            )

            // when
            val result = list1 == list2

            // then
            assertThat(result).isFalse()
            assertThat(list1).isNotEqualTo(list2)
        }

        @Test
        fun `two lists are not equal having different generics`() {
            // given
            val list1 = EventfulList(
                listType = WATCH_LIST,
                "A",
                "B",
                "C",
            )

            val list2 = EventfulList(
                listType = WATCH_LIST,
                1,
                2,
                3,
            )

            // when
            val result = list1.equals(list2)

            // then
            assertThat(result).isFalse()
            assertThat(list1).isNotEqualTo(list2)
        }

        @Test
        fun `two lists are not equal having different object type`() {
            // given
            val list1 = EventfulList(
                listType = WATCH_LIST,
                "A",
                "B",
                "C",
            )

            // when
            val result = list1.equals(1)

            // then
            assertThat(result).isFalse()
        }
    }
}



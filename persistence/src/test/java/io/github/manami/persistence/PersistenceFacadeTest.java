package io.github.manami.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.eventbus.EventBus;

import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.persistence.inmemory.InMemoryPersistenceHandler;
import io.github.manami.persistence.inmemory.animelist.InMemoryAnimeListHandler;
import io.github.manami.persistence.inmemory.filterlist.InMemoryFilterListHandler;
import io.github.manami.persistence.inmemory.watchlist.InMemoryWatchListHandler;

public class PersistenceFacadeTest {

	private InMemoryAnimeListHandler inMemoryAnimeListHandler;
	private InMemoryFilterListHandler inMemoryFilterListHandler;
	private InMemoryWatchListHandler inMemoryWatchListHandler;
	private PersistenceFacade persistenceFacade;

	@Before
	public void setUp() throws IOException {
		inMemoryAnimeListHandler = new InMemoryAnimeListHandler();
		inMemoryFilterListHandler = new InMemoryFilterListHandler();
		inMemoryWatchListHandler = new InMemoryWatchListHandler();
		final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(
				inMemoryAnimeListHandler, inMemoryFilterListHandler, inMemoryWatchListHandler);
		persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, mock(EventBus.class));
	}

	@Test
	public void testFilterAnimeIsNull() {
		// given

		// when
		persistenceFacade.filterAnime(null);

		// then
		assertThat(persistenceFacade.fetchFilterList().size(), equalTo(0));
	}

	@Test
	public void testFilterAnimeIsEntryWithoutTitle() {
		// given
		final FilterEntry entry = new FilterEntry("", "http://myanimelist.net/anime/1535");

		// when
		persistenceFacade.filterAnime(entry);

		// then
		assertThat(persistenceFacade.fetchFilterList().size(), equalTo(0));
	}

	@Test
	public void testFilterAnimeIsEntryWithoutInfoLink() {
		// given
		final FilterEntry entry = new FilterEntry("Death Note", "");

		// when
		persistenceFacade.filterAnime(entry);

		// then
		assertThat(persistenceFacade.fetchFilterList().size(), equalTo(0));
	}

	@Test
	public void testFilterAnimeIsEntryWithoutThumbnail() {
		// given
		final FilterEntry entry = new FilterEntry("Death Note", "http://myanimelist.net/anime/1535");

		// when
		persistenceFacade.filterAnime(entry);

		// then
		assertThat(persistenceFacade.fetchFilterList().size(), equalTo(1));
	}

	@Test
	public void testFilterAnimeIsEntryFullEntry() {
		// given
		final FilterEntry entry = new FilterEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg",
				"http://myanimelist.net/anime/1535");

		// when
		persistenceFacade.filterAnime(entry);

		// then
		assertThat(persistenceFacade.fetchFilterList().size(), equalTo(1));
	}

	@Test
	public void testFilterEntryExists() {
		// given
		final String infoLink = "http://myanimelist.net/anime/1535";
		final FilterEntry entry = new FilterEntry("Death Note", infoLink);
		persistenceFacade.filterAnime(entry);

		// when
		final boolean result = persistenceFacade.filterEntryExists(infoLink);

		// then
		assertThat(result, equalTo(true));
	}

	@Test
	public void testFilterEntryNotExists() {
		// given

		// when
		final boolean result = persistenceFacade.filterEntryExists("http://myanimelist.net/anime/1535");

		// then
		assertThat(result, equalTo(false));
	}

	@Test
	public void testFilterAnimeList() {
		// given
		final FilterEntry entry = new FilterEntry("Death Note", "http://myanimelist.net/anime/1535");
		persistenceFacade.filterAnime(entry);

		// when
		final List<FilterEntry> fetchFilterList = persistenceFacade.fetchFilterList();

		// then
		assertThat(fetchFilterList.size(), equalTo(1));
	}

	@Test
	public void testFetchWatchList() {
		// given
		final WatchListEntry entry = new WatchListEntry("Death Note", "http://myanimelist.net/anime/1535");
		persistenceFacade.watchAnime(entry);

		// when
		final List<WatchListEntry> fetchWatchList = persistenceFacade.fetchWatchList();

		// then
		assertThat(fetchWatchList.size(), equalTo(1));
	}

	@Test
	public void testRemoveFromFilterListWorks() {
		// given
		final String infoLink = "http://myanimelist.net/anime/1535";
		final FilterEntry entry = new FilterEntry("Death Note", infoLink);
		persistenceFacade.filterAnime(entry);

		// when
		final boolean result = persistenceFacade.removeFromFilterList(infoLink);

		// then
		assertThat(result, equalTo(true));
		assertThat(persistenceFacade.fetchFilterList().isEmpty(), equalTo(true));
	}

	@Test
	public void testRemoveFromFilterListNullAsArgument() {
		// given

		// when
		final boolean result = persistenceFacade.removeFromFilterList(null);

		// then
		assertThat(result, equalTo(false));
	}

	@Test
	public void testWatchListEntryExists() {
		// given
		final String infoLink = "http://myanimelist.net/anime/1535";
		final WatchListEntry entry = new WatchListEntry("Death Note", infoLink);
		persistenceFacade.watchAnime(entry);

		// when
		final boolean result = persistenceFacade.watchListEntryExists(infoLink);

		// then
		assertThat(result, equalTo(true));
	}

	@Test
	public void testWatchListEntryNotExists() {
		// given

		// when
		final boolean result = persistenceFacade.watchListEntryExists("http://myanimelist.net/anime/1535");

		// then
		assertThat(result, equalTo(false));
	}

	@Test
	public void testWatchAnimeIsNull() {
		// given

		// when
		persistenceFacade.watchAnime(null);

		// then
		assertThat(persistenceFacade.fetchWatchList().size(), equalTo(0));
	}

	@Test
	public void testWatchAnimeIsEntryWithoutTitle() {
		// given
		final WatchListEntry entry = new WatchListEntry("", "http://myanimelist.net/anime/1535");

		// when
		persistenceFacade.watchAnime(entry);

		// then
		assertThat(persistenceFacade.fetchWatchList().size(), equalTo(0));
	}

	@Test
	public void testWatchAnimeIsEntryWithoutInfoLink() {
		// given
		final WatchListEntry entry = new WatchListEntry("Death Note", "");

		// when
		persistenceFacade.watchAnime(entry);

		// then
		assertThat(persistenceFacade.fetchWatchList().size(), equalTo(0));
	}

	@Test
	public void testWatchAnimeIsEntryWithoutThumbnail() {
		// given
		final WatchListEntry entry = new WatchListEntry("Death Note", "http://myanimelist.net/anime/1535");

		// when
		persistenceFacade.watchAnime(entry);

		// then
		assertThat(persistenceFacade.fetchWatchList().size(), equalTo(1));
	}

	@Test
	public void testWatchAnimeIsEntryFullEntry() {
		// given
		final WatchListEntry entry = new WatchListEntry("Death Note",
				"http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");

		// when
		persistenceFacade.watchAnime(entry);

		// then
		assertThat(persistenceFacade.fetchWatchList().size(), equalTo(1));
	}

	@Test
	public void testRemoveFromWatchListWorks() {
		// given
		final String infoLink = "http://myanimelist.net/anime/1535";
		final WatchListEntry entry = new WatchListEntry("Death Note", infoLink);
		persistenceFacade.watchAnime(entry);

		// when
		final boolean result = persistenceFacade.removeFromWatchList(infoLink);

		// then
		assertThat(result, equalTo(true));
		assertThat(persistenceFacade.fetchWatchList().isEmpty(), equalTo(true));
	}

	@Test
	public void testRemoveFromWatchListNullAsArgument() {
		// given

		// when
		final boolean result = persistenceFacade.removeFromWatchList(null);

		// then
		assertThat(result, equalTo(false));
	}
}

package io.github.manami.core;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;

import com.google.common.eventbus.EventBus;
import com.google.common.io.Files;

import io.github.manami.cache.Cache;
import io.github.manami.core.commands.CmdAddAnime;
import io.github.manami.core.commands.CmdAddFilterEntry;
import io.github.manami.core.commands.CmdAddWatchListEntry;
import io.github.manami.core.commands.CommandService;
import io.github.manami.core.config.Config;
import io.github.manami.core.services.ServiceRepository;
import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.AbstractMinimalEntry;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.dto.events.AnimeListChangedEvent;
import io.github.manami.persistence.PersistenceFacade;
import io.github.manami.persistence.inmemory.InMemoryPersistenceHandler;
import io.github.manami.persistence.inmemory.animelist.InMemoryAnimeListHandler;
import io.github.manami.persistence.inmemory.filterlist.InMemoryFilterListHandler;
import io.github.manami.persistence.inmemory.watchlist.InMemoryWatchListHandler;

public class ManamiTest {

    private static final String TEST_ANIME_LIST_FILE = "test_anime_list.xml";
    private static final String TEST_RECOMMENDATIONS_FILE = "test_recommendations_list.json";
    private static final String ANIME_LIST_EXPORT_FILE = "test_anime_list_export.json";

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    private Cache cacheMock;
    private Config configMock;
    private EventBus eventBusMock;
    private ServiceRepository serviceRepositoryMock;


    @Before
    public void setUp() throws Exception {
        cacheMock = mock(Cache.class);
        configMock = mock(Config.class);
        eventBusMock = mock(EventBus.class);
        serviceRepositoryMock = mock(ServiceRepository.class);
    }


    @Test
    public void testNewList() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final CommandService cmdService = new CommandService(eventBusMock);

        final Config config = new Config(eventBusMock);
        config.setFile(Paths.get("."));

        final Manami app = new Manami(cacheMock, cmdService, config, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);

        final FilterEntry filterEntry = new FilterEntry("Gintama", "http://cdn.myanimelist.net/images/anime/3/72078t.jpg", "http://myanimelist.net/anime/28977");

        final WatchListEntry watchEntry = new WatchListEntry("Steins;Gate", "http://cdn.myanimelist.net/images/anime/5/73199t.jpg", "http://myanimelist.net/anime/9253");

        cmdService.executeCommand(new CmdAddAnime(entry, app));
        cmdService.executeCommand(new CmdAddFilterEntry(filterEntry, app));
        cmdService.executeCommand(new CmdAddWatchListEntry(watchEntry, app));

        // when
        app.newList();

        // then
        verify(eventBusMock, times(9)).post(any(AnimeListChangedEvent.class));
        assertThat(config.getFile(), equalTo(null));
        assertThat(persistenceFacade.fetchAnimeList().isEmpty(), equalTo(true));
        assertThat(persistenceFacade.fetchWatchList().isEmpty(), equalTo(true));
        assertThat(persistenceFacade.fetchFilterList().isEmpty(), equalTo(true));
    }


    @Test
    public void testOpen() throws SAXException, ParserConfigurationException, IOException {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final CommandService cmdService = new CommandService(eventBusMock);

        final Config config = new Config(eventBusMock);
        config.setFile(Paths.get("."));

        final Manami app = new Manami(cacheMock, cmdService, config, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);
        persistenceFacade.addAnime(entry);

        final FilterEntry filterEntry = new FilterEntry("Gintama", "http://cdn.myanimelist.net/images/anime/3/72078t.jpg", "http://myanimelist.net/anime/28977");
        persistenceFacade.filterAnime(filterEntry);

        final WatchListEntry watchEntry = new WatchListEntry("Steins;Gate", "http://cdn.myanimelist.net/images/anime/5/73199t.jpg", "http://myanimelist.net/anime/9253");
        persistenceFacade.watchAnime(watchEntry);

        final Path file = new ClassPathResource(TEST_ANIME_LIST_FILE).getFile().toPath();

        // when
        app.open(file);

        // then
        verify(eventBusMock, times(9)).post(any(AnimeListChangedEvent.class));
        assertThat(config.getFile(), equalTo(file));

        final List<Anime> fetchAnimeList = persistenceFacade.fetchAnimeList();
        assertThat(fetchAnimeList, not(nullValue()));
        assertThat(fetchAnimeList.isEmpty(), equalTo(false));
        assertThat(fetchAnimeList.size(), equalTo(2));

        final Anime bokuDake = fetchAnimeList.get(0);
        assertThat(bokuDake, not(nullValue()));
        assertThat(bokuDake.getEpisodes(), equalTo(12));
        assertThat(bokuDake.getInfoLink(), equalTo("http://myanimelist.net/anime/31043"));
        assertThat(bokuDake.getLocation(), equalTo("/anime/series/boku_dake_ga_inai_machi"));
        assertThat(bokuDake.getTitle(), equalTo("Boku dake ga Inai Machi"));
        assertThat(bokuDake.getType(), equalTo(AnimeType.TV));

        final Anime rurouniKenshin = fetchAnimeList.get(1);
        assertThat(rurouniKenshin, not(nullValue()));
        assertThat(rurouniKenshin.getEpisodes(), equalTo(4));
        assertThat(rurouniKenshin.getInfoLink(), equalTo("http://myanimelist.net/anime/44"));
        assertThat(rurouniKenshin.getLocation(), equalTo("/anime/series/rurouni_kenshin"));
        assertThat(rurouniKenshin.getTitle(), equalTo("Rurouni Kenshin: Meiji Kenkaku Romantan - Tsuiokuhen"));
        assertThat(rurouniKenshin.getType(), equalTo(AnimeType.OVA));

        final List<WatchListEntry> fetchWatchList = inMemoryPersistenceHandler.fetchWatchList();
        assertThat(fetchWatchList, not(nullValue()));
        assertThat(fetchWatchList.isEmpty(), equalTo(false));
        assertThat(fetchWatchList.size(), equalTo(1));

        final WatchListEntry deathNoteRewrite = fetchWatchList.get(0);
        assertThat(deathNoteRewrite, not(nullValue()));
        assertThat(deathNoteRewrite.getInfoLink(), equalTo("http://myanimelist.net/anime/2994"));
        assertThat(deathNoteRewrite.getThumbnail(), equalTo("http://cdn.myanimelist.net/images/anime/13/8518t.jpg"));
        assertThat(deathNoteRewrite.getTitle(), equalTo("Death Note Rewrite"));

        final List<FilterEntry> fetchFilterList = inMemoryPersistenceHandler.fetchFilterList();
        assertThat(fetchFilterList, not(nullValue()));
        assertThat(fetchFilterList.isEmpty(), equalTo(false));
        assertThat(fetchFilterList.size(), equalTo(1));

        final FilterEntry gintama = fetchFilterList.get(0);
        assertThat(gintama, not(nullValue()));
        assertThat(gintama.getInfoLink(), equalTo("http://myanimelist.net/anime/918"));
        assertThat(gintama.getThumbnail(), equalTo("http://cdn.myanimelist.net/images/anime/2/10038t.jpg"));
        assertThat(gintama.getTitle(), equalTo("Gintama"));
    }


    @Test
    public void testFilterAnimeIsNull() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.filterAnime(null);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(false));
        assertThat(app.fetchFilterList().size(), equalTo(0));
    }


    @Test
    public void testFilterAnimeIsEntryWithoutTitle() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final FilterEntry entry = new FilterEntry("", "http://myanimelist.net/anime/1535");

        // when
        final boolean result = app.filterAnime(entry);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(false));
        assertThat(app.fetchFilterList().size(), equalTo(0));
    }


    @Test
    public void testFilterAnimeIsEntryWithoutInfoLink() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final FilterEntry entry = new FilterEntry("Death Note", "");

        // when
        final boolean result = app.filterAnime(entry);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(false));
        assertThat(app.fetchFilterList().size(), equalTo(0));
    }


    @Test
    public void testFilterAnimeIsEntryWithoutThumbnail() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final FilterEntry entry = new FilterEntry("Death Note", "http://myanimelist.net/anime/1535");

        // when
        final boolean result = app.filterAnime(entry);

        // then
        verify(eventBusMock, times(1)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(true));
        assertThat(app.fetchFilterList().size(), equalTo(1));
    }


    @Test
    public void testFilterAnimeIsFullEntry() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final FilterEntry entry = new FilterEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");

        // when
        final boolean result = app.filterAnime(entry);

        // then
        verify(eventBusMock, times(1)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(true));
        assertThat(app.fetchFilterList().size(), equalTo(1));
    }


    @Test
    public void testFilterEntryExists() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final String infoLink = "http://myanimelist.net/anime/1535";
        final FilterEntry entry = new FilterEntry("Death Note", infoLink);
        app.filterAnime(entry);

        // when
        final boolean result = app.filterEntryExists(infoLink);

        // then
        assertThat(result, equalTo(true));
    }


    @Test
    public void testFilterEntryNotExists() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.filterEntryExists("http://myanimelist.net/anime/1535");

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(false));
    }


    @Test
    public void testFilterAnimeList() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final FilterEntry entry = new FilterEntry("Death Note", "http://myanimelist.net/anime/1535");
        app.filterAnime(entry);

        // when
        final List<FilterEntry> fetchFilterList = app.fetchFilterList();

        // then
        assertThat(fetchFilterList.size(), equalTo(1));
    }


    @Test
    public void testFetchWatchList() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final WatchListEntry entry = new WatchListEntry("Death Note", "http://myanimelist.net/anime/1535");
        app.watchAnime(entry);

        // when
        final List<WatchListEntry> fetchWatchList = app.fetchWatchList();

        // then
        assertThat(fetchWatchList.size(), equalTo(1));
    }


    @Test
    public void testRemoveFromFilterListWorks() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final String infoLink = "http://myanimelist.net/anime/1535";
        final FilterEntry entry = new FilterEntry("Death Note", infoLink);
        app.filterAnime(entry);

        // when
        final boolean result = app.removeFromFilterList(infoLink);

        // then
        verify(eventBusMock, times(2)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(true));
        assertThat(app.fetchFilterList().isEmpty(), equalTo(true));
    }


    @Test
    public void testRemoveFromFilterListNullAsArgument() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.removeFromFilterList(null);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(false));
    }


    @Test
    public void testWatchListEntryExists() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final String infoLink = "http://myanimelist.net/anime/1535";
        final WatchListEntry entry = new WatchListEntry("Death Note", infoLink);
        app.watchAnime(entry);

        // when
        final boolean result = app.watchListEntryExists(infoLink);

        // then
        assertThat(result, equalTo(true));
    }


    @Test
    public void testWatchListEntryNotExists() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.watchListEntryExists("http://myanimelist.net/anime/1535");

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(false));
    }


    @Test
    public void testWatchAnimeIsNull() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.watchAnime(null);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(false));
        assertThat(app.fetchWatchList().size(), equalTo(0));
    }


    @Test
    public void testWatchAnimeIsEntryWithoutTitle() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final WatchListEntry entry = new WatchListEntry("", "http://myanimelist.net/anime/1535");

        // when
        final boolean result = app.watchAnime(entry);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(false));
        assertThat(app.fetchWatchList().size(), equalTo(0));
    }


    @Test
    public void testWatchAnimeIsEntryWithoutInfoLink() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final WatchListEntry entry = new WatchListEntry("Death Note", "");

        // when
        final boolean result = app.watchAnime(entry);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(false));
        assertThat(app.fetchWatchList().size(), equalTo(0));
    }


    @Test
    public void testWatchAnimeIsEntryWithoutThumbnail() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final WatchListEntry entry = new WatchListEntry("Death Note", "http://myanimelist.net/anime/1535");

        // when
        final boolean result = app.watchAnime(entry);

        // then
        verify(eventBusMock, times(1)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(true));
        assertThat(app.fetchWatchList().size(), equalTo(1));
    }


    @Test
    public void testWatchAnimeIsFullEntry() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final WatchListEntry entry = new WatchListEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");

        // when
        final boolean result = app.watchAnime(entry);

        // then
        verify(eventBusMock, times(1)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(true));
        assertThat(app.fetchWatchList().size(), equalTo(1));
    }


    @Test
    public void testRemoveFromWatchListWorks() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final String infoLink = "http://myanimelist.net/anime/1535";
        final WatchListEntry entry = new WatchListEntry("Death Note", infoLink);
        app.watchAnime(entry);

        // when
        final boolean result = app.removeFromWatchList(infoLink);

        // then
        verify(eventBusMock, times(2)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(true));
        assertThat(app.fetchWatchList().isEmpty(), equalTo(true));
    }


    @Test
    public void testRemoveFromWatchListNullAsArgument() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.removeFromWatchList(null);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(false));
    }


    @Test
    public void testaddAnimeIsNull() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.addAnime(null);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(false));
        assertThat(app.fetchAnimeList().size(), equalTo(0));
    }


    @Test
    public void testaddAnimeIsFullEntry() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = app.addAnime(entry);

        // then
        verify(eventBusMock, times(1)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(true));
        assertThat(app.fetchAnimeList().size(), equalTo(1));
    }


    @Test
    public void testaddAnimeIsEntryWithoutEpisodes() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime();
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = app.addAnime(entry);

        // then
        verify(eventBusMock, times(1)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(true));
        assertThat(app.fetchAnimeList().size(), equalTo(1));
    }


    @Test
    public void testaddAnimeIsEntryWithoutInfoLink() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = app.addAnime(entry);

        // then
        verify(eventBusMock, times(1)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(true));
        assertThat(app.fetchAnimeList().size(), equalTo(1));
    }


    @Test
    public void testaddAnimeIsEntryWithoutLocation() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = app.addAnime(entry);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(false));
        assertThat(app.fetchAnimeList().size(), equalTo(0));
    }


    @Test
    public void testaddAnimeIsEntryWithoutPicture() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = app.addAnime(entry);

        // then
        verify(eventBusMock, times(1)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(true));
        assertThat(app.fetchAnimeList().size(), equalTo(1));
    }


    @Test
    public void testaddAnimeIsEntryWithoutThumbnail() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = app.addAnime(entry);

        // then
        verify(eventBusMock, times(1)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(true));
        assertThat(app.fetchAnimeList().size(), equalTo(1));
    }


    @Test
    public void testaddAnimeIsEntryWithoutTitle() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = app.addAnime(entry);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(false));
        assertThat(app.fetchAnimeList().size(), equalTo(0));
    }


    @Test
    public void testaddAnimeIsEntryWithoutType() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");

        // when
        final boolean result = app.addAnime(entry);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(false));
        assertThat(app.fetchAnimeList().size(), equalTo(0));
    }


    @Test
    public void testAnimeEntryExists() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final String infoLink = "http://myanimelist.net/anime/1535";
        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink(infoLink);
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);
        app.addAnime(entry);

        // when
        final boolean result = app.animeEntryExists(infoLink);

        // then
        assertThat(result, equalTo(true));
    }


    @Test
    public void testAnimeEntryNotExists() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.animeEntryExists("http://myanimelist.net/anime/1535");

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(false));
    }


    @Test
    public void testAnimeList() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);
        app.addAnime(entry);

        // when
        final List<Anime> animeList = app.fetchAnimeList();

        // then
        assertThat(animeList.size(), equalTo(1));
    }


    @Test
    public void testRemoveFromAnimeListWorks() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);
        app.addAnime(entry);

        // when
        final boolean result = app.removeAnime(entry.getId());

        // then
        verify(eventBusMock, times(2)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(true));
        assertThat(app.fetchAnimeList().isEmpty(), equalTo(true));
    }


    @Test
    public void testRemoveFromAnimeListNullAsArgument() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.removeAnime(null);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result, equalTo(false));
    }


    @Test
    public void testUpdateOrCreateWithNull() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        app.updateOrCreate(null);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(app.fetchAnimeList().isEmpty(), equalTo(true));
        assertThat(app.fetchWatchList().isEmpty(), equalTo(true));
        assertThat(app.fetchFilterList().isEmpty(), equalTo(true));
    }


    @Test
    public void testUpdateOrCreateForNewAnimeEntry() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime();
        entry.setEpisodes(37);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);

        // when
        app.updateOrCreate(entry);

        // then
        verify(eventBusMock, times(1)).post(any(AnimeListChangedEvent.class));
        assertThat(app.fetchAnimeList().isEmpty(), equalTo(false));
        assertThat(app.fetchAnimeList().get(0), equalTo(entry));
    }


    @Test
    public void testUpdateOrCreateForModifiedAnimeEntry() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime();
        entry.setEpisodes(35);
        entry.setInfoLink("http://myanimelist.net/anime/1535");
        entry.setLocation("/death_note");
        entry.setPicture("http://cdn.myanimelist.net/images/anime/9/9453.jpg");
        entry.setThumbnail("http://cdn.myanimelist.net/images/anime/9/9453t.jpg");
        entry.setTitle("Death Note");
        entry.setType(AnimeType.TV);

        app.addAnime(entry);

        final int episodes = 37;
        entry.setEpisodes(episodes);

        // when
        app.updateOrCreate(entry);

        // then
        verify(eventBusMock, times(2)).post(any(AnimeListChangedEvent.class));
        assertThat(app.fetchAnimeList().isEmpty(), equalTo(false));
        assertThat(app.fetchAnimeList().get(0).getEpisodes(), equalTo(episodes));
    }


    @Test
    public void testUpdateOrCreateForNewFilterEntry() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final FilterEntry entry = new FilterEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");

        // when
        app.updateOrCreate(entry);

        // then
        verify(eventBusMock, times(1)).post(any(AnimeListChangedEvent.class));
        assertThat(app.fetchFilterList().isEmpty(), equalTo(false));
        assertThat(app.fetchFilterList().get(0), equalTo(entry));
    }


    @Test
    public void testUpdateOrCreateForModifiedFilterEntry() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final FilterEntry entry = new FilterEntry("Death Note", AbstractMinimalEntry.NO_IMG_THUMB, "http://myanimelist.net/anime/1535");

        app.filterAnime(entry);

        final String thumbnail = "http://cdn.myanimelist.net/images/anime/9/9453t.jpg";
        entry.setThumbnail(thumbnail);

        // when
        app.updateOrCreate(entry);

        // then
        verify(eventBusMock, times(2)).post(any(AnimeListChangedEvent.class));
        assertThat(app.fetchFilterList().isEmpty(), equalTo(false));
        assertThat(app.fetchFilterList().get(0).getThumbnail(), equalTo(thumbnail));
    }


    @Test
    public void testUpdateOrCreateForNewWatchListEntry() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final WatchListEntry entry = new WatchListEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");

        // when
        app.updateOrCreate(entry);

        // then
        verify(eventBusMock, times(1)).post(any(AnimeListChangedEvent.class));
        assertThat(app.fetchWatchList().isEmpty(), equalTo(false));
        assertThat(app.fetchWatchList().get(0), equalTo(entry));
    }


    @Test
    public void testUpdateOrCreateForModifiedWatchListEntry() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final WatchListEntry entry = new WatchListEntry("Death Note", AbstractMinimalEntry.NO_IMG_THUMB, "http://myanimelist.net/anime/1535");

        app.watchAnime(entry);

        final String thumbnail = "http://cdn.myanimelist.net/images/anime/9/9453t.jpg";
        entry.setThumbnail(thumbnail);

        // when
        app.updateOrCreate(entry);

        // then
        verify(eventBusMock, times(2)).post(any(AnimeListChangedEvent.class));
        assertThat(app.fetchWatchList().isEmpty(), equalTo(false));
        assertThat(app.fetchWatchList().get(0).getThumbnail(), equalTo(thumbnail));
    }


    @Test
    public void testThatPredefinedListIsExportedCorrectly() throws SAXException, ParserConfigurationException, IOException {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final List<Anime> list = newArrayList();

        final Anime bokuDake = new Anime();
        bokuDake.setEpisodes(12);
        bokuDake.setInfoLink("http://myanimelist.net/anime/31043");
        bokuDake.setLocation("/anime/series/boku_dake_ga_inai_machi");
        bokuDake.setTitle("Boku dake ga Inai Machi");
        bokuDake.setType(AnimeType.TV);
        list.add(bokuDake);

        final Anime rurouniKenshin = new Anime();
        rurouniKenshin.setEpisodes(4);
        rurouniKenshin.setInfoLink("http://myanimelist.net/anime/44");
        rurouniKenshin.setLocation("/anime/series/rurouni_kenshin");
        rurouniKenshin.setTitle("Rurouni Kenshin: Meiji Kenkaku Romantan - Tsuiokuhen");
        rurouniKenshin.setType(AnimeType.OVA);
        list.add(rurouniKenshin);

        final ClassPathResource resource = new ClassPathResource(TEST_RECOMMENDATIONS_FILE);
        final StringBuilder expectedFileBuilder = new StringBuilder();
        Files.readLines(resource.getFile(), Charset.forName("UTF-8")).forEach(expectedFileBuilder::append);

        final File file = testFolder.newFile(ANIME_LIST_EXPORT_FILE);

        // when
        app.exportList(list, file.toPath());

        // then
        final StringBuilder exportedFileBuilder = new StringBuilder();
        Files.readLines(file, Charset.forName("UTF-8")).forEach(exportedFileBuilder::append);

        assertThat(expectedFileBuilder.toString(), equalTo(exportedFileBuilder.toString()));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testThatExportListIsNull() throws SAXException, ParserConfigurationException, IOException {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final File file = testFolder.newFile(ANIME_LIST_EXPORT_FILE);

        // when
        app.exportList(null, file.toPath());

        // then
    }


    @Test(expected = IllegalArgumentException.class)
    public void testThatExportFileIsNull() throws SAXException, ParserConfigurationException, IOException {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final List<Anime> list = newArrayList();

        final Anime bokuDake = new Anime();
        bokuDake.setEpisodes(12);
        bokuDake.setInfoLink("http://myanimelist.net/anime/31043");
        bokuDake.setLocation("/anime/series/boku_dake_ga_inai_machi");
        bokuDake.setTitle("Boku dake ga Inai Machi");
        bokuDake.setType(AnimeType.TV);
        list.add(bokuDake);

        final Anime rurouniKenshin = new Anime();
        rurouniKenshin.setEpisodes(4);
        rurouniKenshin.setInfoLink("http://myanimelist.net/anime/44");
        rurouniKenshin.setLocation("/anime/series/rurouni_kenshin");
        rurouniKenshin.setTitle("Rurouni Kenshin: Meiji Kenkaku Romantan - Tsuiokuhen");
        rurouniKenshin.setType(AnimeType.OVA);
        list.add(rurouniKenshin);

        // when
        app.exportList(list, null);

        // then
    }
}

package io.github.manami.core;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import com.google.common.eventbus.EventBus;

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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ManamiTest {

    private static final String TEST_ANIME_LIST_FILE_XML = "test_anime_list.xml";
    private static final String TEST_ANIME_LIST_FILE_JSON = "test_anime_list.json";
    private static final String TEST_ANIME_LIST_FILE_CSV = "test_anime_list.csv";
    private static final String TEST_MAL_LIST_FILE = "mal_export.xml";
    private static final String TEST_RECOMMENDATIONS_FILE = "test_recommendations_list.json";

    private Path tempFolder;
    private Cache cacheMock;
    private Config configMock;
    private EventBus eventBusMock;
    private String separator;
    private ServiceRepository serviceRepositoryMock;
    private PersistenceFacade persistenceFacadeMock;


    @BeforeMethod
    public void setUp() throws Exception {
        cacheMock = mock(Cache.class);
        configMock = mock(Config.class);
        eventBusMock = mock(EventBus.class);
        serviceRepositoryMock = mock(ServiceRepository.class);
        persistenceFacadeMock = mock(PersistenceFacade.class);
        separator = FileSystems.getDefault().getSeparator();
        tempFolder = Files.createTempDirectory(String.valueOf(System.currentTimeMillis()));
    }


    @AfterMethod
    public void tearDown() throws Exception {
        if (Files.isDirectory(tempFolder)) {
            Files.list(tempFolder).forEach(file -> {
                try {
                    Files.delete(file);
                } catch (final IOException e) {
                    log.error("Unable to delete file in temp folder: {}", file);
                }
            });

            Files.delete(tempFolder);
        }
    }


    @Test(groups = "unitTest")
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
        assertNull(config.getFile());
        assertTrue(persistenceFacade.fetchAnimeList().isEmpty());
        assertTrue(persistenceFacade.fetchWatchList().isEmpty());
        assertTrue(persistenceFacade.fetchFilterList().isEmpty());
    }


    @Test(groups = "unitTest")
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

        final Path file = new ClassPathResource(TEST_ANIME_LIST_FILE_XML).getFile().toPath();

        // when
        app.open(file);

        // then
        verify(eventBusMock, times(9)).post(any(AnimeListChangedEvent.class));
        assertEquals(config.getFile(), file);

        final List<Anime> fetchAnimeList = persistenceFacade.fetchAnimeList();
        assertNotNull(fetchAnimeList);
        assertFalse(fetchAnimeList.isEmpty());
        assertEquals(fetchAnimeList.size(), 2);

        final Anime bokuDake = fetchAnimeList.get(0);
        assertNotNull(bokuDake);
        assertEquals(bokuDake.getEpisodes(), 12);
        assertEquals(bokuDake.getInfoLink(), "http://myanimelist.net/anime/31043");
        assertEquals(bokuDake.getLocation(), "/anime/series/boku_dake_ga_inai_machi");
        assertEquals(bokuDake.getTitle(), "Boku dake ga Inai Machi");
        assertEquals(bokuDake.getType(), AnimeType.TV);

        final Anime rurouniKenshin = fetchAnimeList.get(1);
        assertNotNull(rurouniKenshin);
        assertEquals(rurouniKenshin.getEpisodes(), 4);
        assertEquals(rurouniKenshin.getInfoLink(), "http://myanimelist.net/anime/44");
        assertEquals(rurouniKenshin.getLocation(), "/anime/series/rurouni_kenshin");
        assertEquals(rurouniKenshin.getTitle(), "Rurouni Kenshin: Meiji Kenkaku Romantan - Tsuiokuhen");
        assertEquals(rurouniKenshin.getType(), AnimeType.OVA);

        final List<WatchListEntry> fetchWatchList = inMemoryPersistenceHandler.fetchWatchList();
        assertNotNull(fetchWatchList);
        assertFalse(fetchWatchList.isEmpty());
        assertEquals(fetchWatchList.size(), 1);

        final WatchListEntry deathNoteRewrite = fetchWatchList.get(0);
        assertNotNull(deathNoteRewrite);
        assertEquals(deathNoteRewrite.getInfoLink(), "http://myanimelist.net/anime/2994");
        assertEquals(deathNoteRewrite.getThumbnail(), "http://cdn.myanimelist.net/images/anime/13/8518t.jpg");
        assertEquals(deathNoteRewrite.getTitle(), "Death Note Rewrite");

        final List<FilterEntry> fetchFilterList = inMemoryPersistenceHandler.fetchFilterList();
        assertNotNull(fetchFilterList);
        assertFalse(fetchFilterList.isEmpty());
        assertEquals(fetchFilterList.size(), 1);

        final FilterEntry gintama = fetchFilterList.get(0);
        assertNotNull(gintama);
        assertEquals(gintama.getInfoLink(), "http://myanimelist.net/anime/918");
        assertEquals(gintama.getThumbnail(), "http://cdn.myanimelist.net/images/anime/2/10038t.jpg");
        assertEquals(gintama.getTitle(), "Gintama");
    }


    @Test(groups = "unitTest")
    public void testFilterAnimeIsNull() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.filterAnime(null);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertFalse(result);
        assertEquals(app.fetchFilterList().size(), 0);
    }


    @Test(groups = "unitTest")
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
        assertFalse(result);
        assertEquals(app.fetchFilterList().size(), 0);
    }


    @Test(groups = "unitTest")
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
        assertFalse(result);
        assertEquals(app.fetchFilterList().size(), 0);
    }


    @Test(groups = "unitTest")
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
        assertTrue(result);
        assertEquals(app.fetchFilterList().size(), 1);
    }


    @Test(groups = "unitTest")
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
        assertTrue(result);
        assertEquals(app.fetchFilterList().size(), 1);
    }


    @Test(groups = "unitTest")
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
        assertTrue(result);
    }


    @Test(groups = "unitTest")
    public void testFilterEntryNotExists() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.filterEntryExists("http://myanimelist.net/anime/1535");

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertFalse(result);
    }


    @Test(groups = "unitTest")
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
        assertEquals(fetchFilterList.size(), 1);
    }


    @Test(groups = "unitTest")
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
        assertEquals(fetchWatchList.size(), 1);
    }


    @Test(groups = "unitTest")
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
        assertTrue(result);
        assertTrue(app.fetchFilterList().isEmpty());
    }


    @Test(groups = "unitTest")
    public void testRemoveFromFilterListNullAsArgument() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.removeFromFilterList(null);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertFalse(result);
    }


    @Test(groups = "unitTest")
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
        assertTrue(result);
    }


    @Test(groups = "unitTest")
    public void testWatchListEntryNotExists() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.watchListEntryExists("http://myanimelist.net/anime/1535");

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertFalse(result);
    }


    @Test(groups = "unitTest")
    public void testWatchAnimeIsNull() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.watchAnime(null);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertFalse(result);
        assertEquals(app.fetchWatchList().size(), 0);
    }


    @Test(groups = "unitTest")
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
        assertFalse(result);
        assertEquals(app.fetchWatchList().size(), 0);
    }


    @Test(groups = "unitTest")
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
        assertFalse(result);
        assertEquals(app.fetchWatchList().size(), 0);
    }


    @Test(groups = "unitTest")
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
        assertTrue(result);
        assertEquals(app.fetchWatchList().size(), 1);
    }


    @Test(groups = "unitTest")
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
        assertTrue(result);
        assertEquals(app.fetchWatchList().size(), 1);
    }


    @Test(groups = "unitTest")
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
        assertTrue(result);
        assertTrue(app.fetchWatchList().isEmpty());
    }


    @Test(groups = "unitTest")
    public void testRemoveFromWatchListNullAsArgument() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.removeFromWatchList(null);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertFalse(result);
    }


    @Test(groups = "unitTest")
    public void testaddAnimeIsNull() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.addAnime(null);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertFalse(result);
        assertEquals(app.fetchAnimeList().size(), 0);
    }


    @Test(groups = "unitTest")
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
        assertTrue(result);
        assertEquals(app.fetchAnimeList().size(), 1);
    }


    @Test(groups = "unitTest")
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
        assertTrue(result);
        assertEquals(app.fetchAnimeList().size(), 1);
    }


    @Test(groups = "unitTest")
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
        assertTrue(result);
        assertEquals(app.fetchAnimeList().size(), 1);
    }


    @Test(groups = "unitTest")
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
        assertFalse(result);
        assertEquals(app.fetchAnimeList().size(), 0);
    }


    @Test(groups = "unitTest")
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
        assertTrue(result);
        assertEquals(app.fetchAnimeList().size(), 1);
    }


    @Test(groups = "unitTest")
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
        assertTrue(result);
        assertEquals(app.fetchAnimeList().size(), 1);
    }


    @Test(groups = "unitTest")
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
        assertFalse(result);
        assertEquals(app.fetchAnimeList().size(), 0);
    }


    @Test(groups = "unitTest")
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
        assertFalse(result);
        assertEquals(app.fetchAnimeList().size(), 0);
    }


    @Test(groups = "unitTest")
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
        assertTrue(result);
    }


    @Test(groups = "unitTest")
    public void testAnimeEntryNotExists() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.animeEntryExists("http://myanimelist.net/anime/1535");

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertFalse(result);
    }


    @Test(groups = "unitTest")
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
        assertEquals(animeList.size(), 1);
    }


    @Test(groups = "unitTest")
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
        assertTrue(result);
        assertTrue(app.fetchAnimeList().isEmpty());
    }


    @Test(groups = "unitTest")
    public void testRemoveFromAnimeListNullAsArgument() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.removeAnime(null);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertFalse(result);
    }


    @Test(groups = "unitTest")
    public void testUpdateOrCreateWithNull() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        app.updateOrCreate(null);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertTrue(app.fetchAnimeList().isEmpty());
        assertTrue(app.fetchWatchList().isEmpty());
        assertTrue(app.fetchFilterList().isEmpty());
    }


    @Test(groups = "unitTest")
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
        assertFalse(app.fetchAnimeList().isEmpty());
        assertEquals(app.fetchAnimeList().get(0), entry);
    }


    @Test(groups = "unitTest")
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
        assertFalse(app.fetchAnimeList().isEmpty());
        assertEquals(app.fetchAnimeList().get(0).getEpisodes(), episodes);
    }


    @Test(groups = "unitTest")
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
        assertFalse(app.fetchFilterList().isEmpty());
        assertEquals(app.fetchFilterList().get(0), entry);
    }


    @Test(groups = "unitTest")
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
        assertFalse(app.fetchFilterList().isEmpty());
        assertEquals(app.fetchFilterList().get(0).getThumbnail(), thumbnail);
    }


    @Test(groups = "unitTest")
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
        assertFalse(app.fetchWatchList().isEmpty());
        assertEquals(app.fetchWatchList().get(0), entry);
    }


    @Test(groups = "unitTest")
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
        assertFalse(app.fetchWatchList().isEmpty());
        assertEquals(app.fetchWatchList().get(0).getThumbnail(), thumbnail);
    }


    @Test(groups = "unitTest")
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
        Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8).forEach(expectedFileBuilder::append);

        final Path file = Files.createFile(Paths.get(tempFolder + separator + "tempfile.json"));

        // when
        app.exportList(list, file);

        // then
        final StringBuilder exportedFileBuilder = new StringBuilder();
        Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8).forEach(exportedFileBuilder::append);

        assertEquals(expectedFileBuilder.toString(), exportedFileBuilder.toString());
    }


    @Test(groups = "unitTest")
    public void testThatPredefinedListIsNotExportedIfFileSuffixIsNotJson() throws SAXException, ParserConfigurationException, IOException {
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

        final Path file = Files.createFile(Paths.get(tempFolder + separator + "tempfile.xml"));

        // when
        app.exportList(list, file);

        // then
        final StringBuilder exportedFileBuilder = new StringBuilder();
        Files.readAllLines(file, StandardCharsets.UTF_8).forEach(exportedFileBuilder::append);

        assertTrue(isBlank(exportedFileBuilder.toString()));
    }


    @Test(groups = "unitTest", expectedExceptions = IllegalArgumentException.class)
    public void testThatExportListIsNull() throws SAXException, ParserConfigurationException, IOException {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Path file = Files.createFile(Paths.get(tempFolder + separator + "tempfile.xml"));

        // when
        app.exportList(null, file);

        // then
    }


    @Test(groups = "unitTest", expectedExceptions = IllegalArgumentException.class)
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


    @Test(groups = "unitTest")
    public void testSearchStringIsBlank() {
        // given
        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacadeMock, serviceRepositoryMock, eventBusMock);

        // when
        app.search(null);
        app.search("");
        app.search("   ");

        // then
        verify(serviceRepositoryMock, times(0)).startService(any());
    }


    @Test(groups = "unitTest")
    public void testSearch() {
        // given
        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacadeMock, serviceRepositoryMock, eventBusMock);

        // when
        app.search("Death Note");

        // then
        verify(serviceRepositoryMock, times(1)).startService(any());
    }


    @Test(groups = "unitTest")
    public void testThaExportedWorksCorrectlyForCsv() throws SAXException, ParserConfigurationException, IOException {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime bokuDake = new Anime();
        bokuDake.setEpisodes(12);
        bokuDake.setInfoLink("http://myanimelist.net/anime/31043");
        bokuDake.setLocation("/anime/series/boku_dake_ga_inai_machi");
        bokuDake.setTitle("Boku dake ga Inai Machi");
        bokuDake.setType(AnimeType.TV);
        persistenceFacade.addAnime(bokuDake);

        final Anime rurouniKenshin = new Anime();
        rurouniKenshin.setEpisodes(4);
        rurouniKenshin.setInfoLink("http://myanimelist.net/anime/44");
        rurouniKenshin.setLocation("/anime/series/rurouni_kenshin");
        rurouniKenshin.setTitle("Rurouni Kenshin: Meiji Kenkaku Romantan - Tsuiokuhen");
        rurouniKenshin.setType(AnimeType.OVA);
        persistenceFacade.addAnime(rurouniKenshin);

        final WatchListEntry deathNoteRewrite = new WatchListEntry("Death Note Rewrite", "http://cdn.myanimelist.net/images/anime/13/8518t.jpg", "http://myanimelist.net/anime/2994");
        persistenceFacade.watchAnime(deathNoteRewrite);

        final FilterEntry gintama = new FilterEntry("Gintama", "http://cdn.myanimelist.net/images/anime/2/10038t.jpg", "http://myanimelist.net/anime/918");
        persistenceFacade.filterAnime(gintama);

        final ClassPathResource resource = new ClassPathResource(TEST_ANIME_LIST_FILE_CSV);
        final StringBuilder expectedFileBuilder = new StringBuilder();
        Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8).forEach(expectedFileBuilder::append);

        final Path file = Files.createFile(Paths.get(tempFolder + separator + "tempfile.csv"));

        // when
        app.export(file);

        // then
        final StringBuilder exportedFileBuilder = new StringBuilder();
        Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8).forEach(exportedFileBuilder::append);

        assertEquals(expectedFileBuilder.toString(), exportedFileBuilder.toString());
    }


    @Test(groups = "unitTest")
    public void testThaExportedWorksCorrectlyForJson() throws SAXException, ParserConfigurationException, IOException {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime bokuDake = new Anime();
        bokuDake.setEpisodes(12);
        bokuDake.setInfoLink("http://myanimelist.net/anime/31043");
        bokuDake.setLocation("/anime/series/boku_dake_ga_inai_machi");
        bokuDake.setTitle("Boku dake ga Inai Machi");
        bokuDake.setType(AnimeType.TV);
        persistenceFacade.addAnime(bokuDake);

        final Anime rurouniKenshin = new Anime();
        rurouniKenshin.setEpisodes(4);
        rurouniKenshin.setInfoLink("http://myanimelist.net/anime/44");
        rurouniKenshin.setLocation("/anime/series/rurouni_kenshin");
        rurouniKenshin.setTitle("Rurouni Kenshin: Meiji Kenkaku Romantan - Tsuiokuhen");
        rurouniKenshin.setType(AnimeType.OVA);
        persistenceFacade.addAnime(rurouniKenshin);

        final WatchListEntry deathNoteRewrite = new WatchListEntry("Death Note Rewrite", "http://cdn.myanimelist.net/images/anime/13/8518t.jpg", "http://myanimelist.net/anime/2994");
        persistenceFacade.watchAnime(deathNoteRewrite);

        final FilterEntry gintama = new FilterEntry("Gintama", "http://cdn.myanimelist.net/images/anime/2/10038t.jpg", "http://myanimelist.net/anime/918");
        persistenceFacade.filterAnime(gintama);

        final ClassPathResource resource = new ClassPathResource(TEST_ANIME_LIST_FILE_JSON);
        final StringBuilder expectedFileBuilder = new StringBuilder();
        Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8).forEach(expectedFileBuilder::append);

        final Path file = Files.createFile(Paths.get(tempFolder + separator + "tempfile.json"));

        // when
        app.export(file);

        // then
        final StringBuilder exportedFileBuilder = new StringBuilder();
        Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8).forEach(exportedFileBuilder::append);

        assertEquals(expectedFileBuilder.toString(), exportedFileBuilder.toString());
    }


    @Test(groups = "unitTest")
    public void testThaExportedIsNotExecutedBecauseFileSuffixDoesNotMatchAnyKnownFileType() throws SAXException, ParserConfigurationException, IOException {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime bokuDake = new Anime();
        bokuDake.setEpisodes(12);
        bokuDake.setInfoLink("http://myanimelist.net/anime/31043");
        bokuDake.setLocation("/anime/series/boku_dake_ga_inai_machi");
        bokuDake.setTitle("Boku dake ga Inai Machi");
        bokuDake.setType(AnimeType.TV);
        persistenceFacade.addAnime(bokuDake);

        final Anime rurouniKenshin = new Anime();
        rurouniKenshin.setEpisodes(4);
        rurouniKenshin.setInfoLink("http://myanimelist.net/anime/44");
        rurouniKenshin.setLocation("/anime/series/rurouni_kenshin");
        rurouniKenshin.setTitle("Rurouni Kenshin: Meiji Kenkaku Romantan - Tsuiokuhen");
        rurouniKenshin.setType(AnimeType.OVA);
        persistenceFacade.addAnime(rurouniKenshin);

        final WatchListEntry deathNoteRewrite = new WatchListEntry("Death Note Rewrite", "http://cdn.myanimelist.net/images/anime/13/8518t.jpg", "http://myanimelist.net/anime/2994");
        persistenceFacade.watchAnime(deathNoteRewrite);

        final FilterEntry gintama = new FilterEntry("Gintama", "http://cdn.myanimelist.net/images/anime/2/10038t.jpg", "http://myanimelist.net/anime/918");
        persistenceFacade.filterAnime(gintama);

        final Path file = Files.createFile(Paths.get(tempFolder + separator + "tempfile.test"));

        // when
        app.export(file);

        // then
        final StringBuilder exportedFileBuilder = new StringBuilder();
        Files.readAllLines(file, StandardCharsets.UTF_8).forEach(exportedFileBuilder::append);

        assertTrue(isBlank(exportedFileBuilder.toString()));
    }


    @Test(groups = "unitTest")
    public void testThatImportWorksCorrectlyForJson() throws SAXException, ParserConfigurationException, IOException {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);
        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);
        final ClassPathResource resource = new ClassPathResource(TEST_ANIME_LIST_FILE_JSON);

        // when
        app.importFile(resource.getFile().toPath());

        // then
        final List<Anime> fetchAnimeList = persistenceFacade.fetchAnimeList();
        assertNotNull(fetchAnimeList);
        assertFalse(fetchAnimeList.isEmpty());
        assertEquals(fetchAnimeList.size(), 2);

        final Anime bokuDake = fetchAnimeList.get(0);
        assertNotNull(bokuDake);
        assertEquals(bokuDake.getEpisodes(), 12);
        assertEquals(bokuDake.getInfoLink(), "http://myanimelist.net/anime/31043");
        assertEquals(bokuDake.getLocation(), "/anime/series/boku_dake_ga_inai_machi");
        assertEquals(bokuDake.getTitle(), "Boku dake ga Inai Machi");
        assertEquals(bokuDake.getType(), AnimeType.TV);

        final Anime rurouniKenshin = fetchAnimeList.get(1);
        assertNotNull(rurouniKenshin);
        assertEquals(rurouniKenshin.getEpisodes(), 4);
        assertEquals(rurouniKenshin.getInfoLink(), "http://myanimelist.net/anime/44");
        assertEquals(rurouniKenshin.getLocation(), "/anime/series/rurouni_kenshin");
        assertEquals(rurouniKenshin.getTitle(), "Rurouni Kenshin: Meiji Kenkaku Romantan - Tsuiokuhen");
        assertEquals(rurouniKenshin.getType(), AnimeType.OVA);
    }


    @Test(groups = "unitTest")
    public void testThatImportWorksCorrectlyForCsv() throws SAXException, ParserConfigurationException, IOException {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);
        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);
        final ClassPathResource resource = new ClassPathResource(TEST_ANIME_LIST_FILE_CSV);

        // when
        app.importFile(resource.getFile().toPath());

        // then
        final List<Anime> fetchAnimeList = persistenceFacade.fetchAnimeList();
        assertNotNull(fetchAnimeList);
        assertFalse(fetchAnimeList.isEmpty());
        assertEquals(fetchAnimeList.size(), 2);

        final Anime bokuDake = fetchAnimeList.get(0);
        assertNotNull(bokuDake);
        assertEquals(bokuDake.getEpisodes(), 12);
        assertEquals(bokuDake.getInfoLink(), "http://myanimelist.net/anime/31043");
        assertEquals(bokuDake.getLocation(), "/anime/series/boku_dake_ga_inai_machi");
        assertEquals(bokuDake.getTitle(), "Boku dake ga Inai Machi");
        assertEquals(bokuDake.getType(), AnimeType.TV);

        final Anime rurouniKenshin = fetchAnimeList.get(1);
        assertNotNull(rurouniKenshin);
        assertEquals(rurouniKenshin.getEpisodes(), 4);
        assertEquals(rurouniKenshin.getInfoLink(), "http://myanimelist.net/anime/44");
        assertEquals(rurouniKenshin.getLocation(), "/anime/series/rurouni_kenshin");
        assertEquals(rurouniKenshin.getTitle(), "Rurouni Kenshin: Meiji Kenkaku Romantan - Tsuiokuhen");
        assertEquals(rurouniKenshin.getType(), AnimeType.OVA);
    }


    @Test(groups = "unitTest")
    public void testThatImportWorksCorrectlyForMalXml() throws SAXException, ParserConfigurationException, IOException {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);
        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);
        final ClassPathResource resource = new ClassPathResource(TEST_MAL_LIST_FILE);

        // when
        app.importFile(resource.getFile().toPath());

        // then
        final List<Anime> fetchAnimeList = persistenceFacade.fetchAnimeList();
        assertNotNull(fetchAnimeList);
        assertFalse(fetchAnimeList.isEmpty());
        assertEquals(fetchAnimeList.size(), 2);

        final Anime deathNote = fetchAnimeList.get(0);
        assertNotNull(deathNote);
        assertEquals(deathNote.getEpisodes(), 37);
        assertEquals(deathNote.getInfoLink(), "http://myanimelist.net/anime/1535");
        assertEquals(deathNote.getLocation(), "/");
        assertEquals(deathNote.getTitle(), "Death Note");
        assertEquals(deathNote.getType(), AnimeType.TV);

        final Anime rurouniKenshin = fetchAnimeList.get(1);
        assertNotNull(rurouniKenshin);
        assertEquals(rurouniKenshin.getEpisodes(), 94);
        assertEquals(rurouniKenshin.getInfoLink(), "http://myanimelist.net/anime/45");
        assertEquals(rurouniKenshin.getLocation(), "/");
        assertEquals(rurouniKenshin.getTitle(), "Rurouni Kenshin: Meiji Kenkaku Romantan");
        assertEquals(rurouniKenshin.getType(), AnimeType.TV);

        final List<WatchListEntry> fetchWatchList = persistenceFacade.fetchWatchList();
        assertNotNull(fetchWatchList);
        assertFalse(fetchWatchList.isEmpty());
        assertEquals(fetchWatchList.size(), 2);

        final WatchListEntry akatsukiNoYona = fetchWatchList.get(0);
        assertNotNull(akatsukiNoYona);
        assertEquals(akatsukiNoYona.getInfoLink(), "http://myanimelist.net/anime/25013");
        assertEquals(akatsukiNoYona.getTitle(), "Akatsuki no Yona");
        assertEquals(akatsukiNoYona.getThumbnail(), "http://cdn.myanimelist.net/images/qm_50.gif");

        final WatchListEntry aldnohaZero = fetchWatchList.get(1);
        assertNotNull(aldnohaZero);
        assertEquals(aldnohaZero.getInfoLink(), "http://myanimelist.net/anime/27655");
        assertEquals(aldnohaZero.getTitle(), "Aldnoah.Zero 2nd Season");
        assertEquals(aldnohaZero.getThumbnail(), "http://cdn.myanimelist.net/images/qm_50.gif");

        final List<FilterEntry> fetchFilterList = persistenceFacade.fetchFilterList();
        assertNotNull(fetchFilterList);
        assertFalse(fetchFilterList.isEmpty());
        assertEquals(fetchFilterList.size(), 2);

        final FilterEntry matanteiLokiRagnarok = fetchFilterList.get(0);
        assertNotNull(matanteiLokiRagnarok);
        assertEquals(matanteiLokiRagnarok.getInfoLink(), "http://myanimelist.net/anime/335");
        assertEquals(matanteiLokiRagnarok.getThumbnail(), "http://cdn.myanimelist.net/images/qm_50.gif");
        assertEquals(matanteiLokiRagnarok.getTitle(), "Matantei Loki Ragnarok");

        final FilterEntry saiunkokuMonogatari = fetchFilterList.get(1);
        assertNotNull(saiunkokuMonogatari);
        assertEquals(saiunkokuMonogatari.getInfoLink(), "http://myanimelist.net/anime/957");
        assertEquals(saiunkokuMonogatari.getThumbnail(), "http://cdn.myanimelist.net/images/qm_50.gif");
        assertEquals(saiunkokuMonogatari.getTitle(), "Saiunkoku Monogatari");
    }


    @Test(groups = "unitTest")
    public void testThatImportDoesNotWorkForAnyOtherFile() throws SAXException, ParserConfigurationException, IOException {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);
        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);
        final ClassPathResource resource = new ClassPathResource("animelist.dtd");

        // when
        app.importFile(resource.getFile().toPath());

        // then
        final List<Anime> animeList = persistenceFacade.fetchAnimeList();
        assertNotNull(animeList);
        assertTrue(animeList.isEmpty());

        final List<WatchListEntry> watchList = persistenceFacade.fetchWatchList();
        assertNotNull(watchList);
        assertTrue(watchList.isEmpty());

        final List<FilterEntry> filterList = persistenceFacade.fetchFilterList();
        assertNotNull(filterList);
        assertTrue(filterList.isEmpty());
    }
}

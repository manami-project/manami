package io.github.manami.core;

import static com.google.common.collect.Lists.newArrayList;
import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
import io.github.manami.dto.entities.FilterListEntry;
import io.github.manami.dto.entities.InfoLink;
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


    @Test(groups = UNIT_TEST_GROUP)
    public void testNewList() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final CommandService cmdService = new CommandService(eventBusMock);

        final Config config = new Config(eventBusMock);
        config.setFile(Paths.get("."));

        final Manami app = new Manami(cacheMock, cmdService, config, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime("Death Note", new InfoLink("https://myanimelist.net/anime/1535"));
        entry.setEpisodes(37);
        entry.setLocation("/death_note");
        entry.setPicture("https://myanimelist.cdn-dena.com/images/anime/9/9453.jpg");
        entry.setThumbnail("https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg");
        entry.setType(AnimeType.TV);

        final FilterListEntry filterListEntry = new FilterListEntry("Gintama", "https://myanimelist.cdn-dena.com/images/anime/3/72078t.jpg", new InfoLink("https://myanimelist.net/anime/28977"));

        final WatchListEntry watchEntry = new WatchListEntry("Steins;Gate", "https://myanimelist.cdn-dena.com/images/anime/5/73199t.jpg", new InfoLink("https://myanimelist.net/anime/9253"));

        cmdService.executeCommand(new CmdAddAnime(entry, app));
        cmdService.executeCommand(new CmdAddFilterEntry(filterListEntry, app));
        cmdService.executeCommand(new CmdAddWatchListEntry(watchEntry, app));

        // when
        app.newList();

        // then
        verify(eventBusMock, times(9)).post(any(AnimeListChangedEvent.class));
        assertThat(config.getFile()).isNull();
        assertThat(persistenceFacade.fetchAnimeList().isEmpty()).isTrue();
        assertThat(persistenceFacade.fetchWatchList().isEmpty()).isTrue();
        assertThat(persistenceFacade.fetchFilterList().isEmpty()).isTrue();
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testOpen() throws SAXException, ParserConfigurationException, IOException {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final CommandService cmdService = new CommandService(eventBusMock);

        final Config config = new Config(eventBusMock);
        config.setFile(Paths.get("."));

        final Manami app = new Manami(cacheMock, cmdService, config, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime("Death Note", new InfoLink("https://myanimelist.net/anime/1535"));
        entry.setEpisodes(37);
        entry.setLocation("/death_note");
        entry.setPicture("https://myanimelist.cdn-dena.com/images/anime/9/9453.jpg");
        entry.setThumbnail("https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg");
        entry.setType(AnimeType.TV);
        persistenceFacade.addAnime(entry);

        final FilterListEntry filterListEntry = new FilterListEntry("Gintama", "https://myanimelist.cdn-dena.com/images/anime/3/72078t.jpg", new InfoLink("https://myanimelist.net/anime/28977"));
        persistenceFacade.filterAnime(filterListEntry);

        final WatchListEntry watchEntry = new WatchListEntry("Steins;Gate", "https://myanimelist.cdn-dena.com/images/anime/5/73199t.jpg", new InfoLink("https://myanimelist.net/anime/9253"));
        persistenceFacade.watchAnime(watchEntry);

        final Path file = new ClassPathResource(TEST_ANIME_LIST_FILE_XML).getFile().toPath();

        // when
        app.open(file);

        // then
        verify(eventBusMock, times(9)).post(any(AnimeListChangedEvent.class));
        assertThat(config.getFile()).isEqualTo(file);

        final List<Anime> fetchAnimeList = persistenceFacade.fetchAnimeList();
        assertThat(fetchAnimeList).isNotNull();
        assertThat(fetchAnimeList.isEmpty()).isFalse();
        assertThat(fetchAnimeList.size()).isEqualTo(2);

        final Anime bokuDake = fetchAnimeList.get(0);
        assertThat(bokuDake).isNotNull();
        assertThat(bokuDake.getEpisodes()).isEqualTo(12);
        assertThat(bokuDake.getInfoLink().getUrl()).isEqualTo("https://myanimelist.net/anime/31043");
        assertThat(bokuDake.getLocation()).isEqualTo("/anime/series/boku_dake_ga_inai_machi");
        assertThat(bokuDake.getTitle()).isEqualTo("Boku dake ga Inai Machi");
        assertThat(bokuDake.getType()).isEqualTo(AnimeType.TV);

        final Anime rurouniKenshin = fetchAnimeList.get(1);
        assertThat(rurouniKenshin).isNotNull();
        assertThat(rurouniKenshin.getEpisodes()).isEqualTo(4);
        assertThat(rurouniKenshin.getInfoLink().getUrl()).isEqualTo("https://myanimelist.net/anime/44");
        assertThat(rurouniKenshin.getLocation()).isEqualTo("/anime/series/rurouni_kenshin");
        assertThat(rurouniKenshin.getTitle()).isEqualTo("Rurouni Kenshin: Meiji Kenkaku Romantan - Tsuiokuhen");
        assertThat(rurouniKenshin.getType()).isEqualTo(AnimeType.OVA);

        final List<WatchListEntry> fetchWatchList = inMemoryPersistenceHandler.fetchWatchList();
        assertThat(fetchWatchList).isNotNull();
        assertThat(fetchWatchList.isEmpty()).isFalse();
        assertThat(fetchWatchList.size()).isEqualTo(1);

        final WatchListEntry deathNoteRewrite = fetchWatchList.get(0);
        assertThat(deathNoteRewrite);
        assertThat(deathNoteRewrite.getInfoLink().getUrl()).isEqualTo("https://myanimelist.net/anime/2994");
        assertThat(deathNoteRewrite.getThumbnail()).isEqualTo("https://myanimelist.cdn-dena.com/images/anime/13/8518t.jpg");
        assertThat(deathNoteRewrite.getTitle()).isEqualTo("Death Note Rewrite");

        final List<FilterListEntry> fetchFilterList = inMemoryPersistenceHandler.fetchFilterList();
        assertThat(fetchFilterList).isNotNull();
        assertThat(fetchFilterList.isEmpty()).isFalse();
        assertThat(fetchFilterList.size()).isEqualTo(1);

        final FilterListEntry gintama = fetchFilterList.get(0);
        assertThat(gintama).isNotNull();
        assertThat(gintama.getInfoLink().getUrl()).isEqualTo("https://myanimelist.net/anime/918");
        assertThat(gintama.getThumbnail()).isEqualTo("https://myanimelist.cdn-dena.com/images/anime/2/10038t.jpg");
        assertThat(gintama.getTitle()).isEqualTo("Gintama");
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterAnimeIsNull() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.filterAnime(null);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isFalse();
        assertThat(app.fetchFilterList().size()).isEqualTo(0);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterAnimeIsEntryWithoutTitle() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final FilterListEntry entry = new FilterListEntry(EMPTY, new InfoLink("https://myanimelist.net/anime/1535"));

        // when
        final boolean result = app.filterAnime(entry);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isFalse();
        assertThat(app.fetchFilterList().size()).isEqualTo(0);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterAnimeIsEntryWithoutInfoLink() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final FilterListEntry entry = new FilterListEntry("Death Note", new InfoLink(EMPTY));

        // when
        final boolean result = app.filterAnime(entry);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isFalse();
        assertThat(app.fetchFilterList().size()).isEqualTo(0);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterAnimeIsEntryWithoutThumbnail() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final FilterListEntry entry = new FilterListEntry("Death Note", new InfoLink("https://myanimelist.net/anime/1535"));

        // when
        final boolean result = app.filterAnime(entry);

        // then
        verify(eventBusMock, times(1)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isTrue();
        assertThat(app.fetchFilterList().size()).isEqualTo(1);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterAnimeIsFullEntry() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final FilterListEntry entry = new FilterListEntry("Death Note", "https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg", new InfoLink("https://myanimelist.net/anime/1535"));

        // when
        final boolean result = app.filterAnime(entry);

        // then
        verify(eventBusMock, times(1)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isTrue();
        assertThat(app.fetchFilterList().size()).isEqualTo(1);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterEntryExists() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final InfoLink infoLink = new InfoLink("https://myanimelist.net/anime/1535");
        final FilterListEntry entry = new FilterListEntry("Death Note", infoLink);
        app.filterAnime(entry);

        // when
        final boolean result = app.filterEntryExists(infoLink);

        // then
        assertThat(result).isTrue();
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterEntryNotExists() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.filterEntryExists(new InfoLink("https://myanimelist.net/anime/1535"));

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isFalse();
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFilterAnimeList() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final FilterListEntry entry = new FilterListEntry("Death Note", new InfoLink("https://myanimelist.net/anime/1535"));
        app.filterAnime(entry);

        // when
        final List<FilterListEntry> fetchFilterList = app.fetchFilterList();

        // then
        assertThat(fetchFilterList.size()).isEqualTo(1);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFetchWatchList() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final WatchListEntry entry = new WatchListEntry("Death Note", new InfoLink("https://myanimelist.net/anime/1535"));
        app.watchAnime(entry);

        // when
        final List<WatchListEntry> fetchWatchList = app.fetchWatchList();

        // then
        assertThat(fetchWatchList.size()).isEqualTo(1);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testRemoveFromFilterListWorks() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final InfoLink infoLink = new InfoLink("https://myanimelist.net/anime/1535");
        final FilterListEntry entry = new FilterListEntry("Death Note", infoLink);
        app.filterAnime(entry);

        // when
        final boolean result = app.removeFromFilterList(infoLink);

        // then
        verify(eventBusMock, times(2)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isTrue();
        assertThat(app.fetchFilterList().isEmpty()).isTrue();
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testRemoveFromFilterListNullAsArgument() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.removeFromFilterList(null);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isFalse();
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testWatchListEntryExists() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final InfoLink infoLink = new InfoLink("https://myanimelist.net/anime/1535");
        final WatchListEntry entry = new WatchListEntry("Death Note", infoLink);
        app.watchAnime(entry);

        // when
        final boolean result = app.watchListEntryExists(infoLink);

        // then
        assertThat(result).isTrue();
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testWatchListEntryNotExists() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.watchListEntryExists(new InfoLink("https://myanimelist.net/anime/1535"));

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isFalse();
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testWatchAnimeIsNull() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.watchAnime(null);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isFalse();
        assertThat(app.fetchWatchList().size()).isEqualTo(0);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testWatchAnimeIsEntryWithoutTitle() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final WatchListEntry entry = new WatchListEntry(EMPTY, new InfoLink("https://myanimelist.net/anime/1535"));

        // when
        final boolean result = app.watchAnime(entry);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isFalse();
        assertThat(app.fetchWatchList().size()).isEqualTo(0);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testWatchAnimeIsEntryWithoutInfoLink() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final WatchListEntry entry = new WatchListEntry("Death Note", new InfoLink(EMPTY));

        // when
        final boolean result = app.watchAnime(entry);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isFalse();
        assertThat(app.fetchWatchList().size()).isEqualTo(0);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testWatchAnimeIsEntryWithoutThumbnail() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final WatchListEntry entry = new WatchListEntry("Death Note", new InfoLink("https://myanimelist.net/anime/1535"));

        // when
        final boolean result = app.watchAnime(entry);

        // then
        verify(eventBusMock, times(1)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isTrue();
        assertThat(app.fetchWatchList().size()).isEqualTo(1);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testWatchAnimeIsFullEntry() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final WatchListEntry entry = new WatchListEntry("Death Note", "https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg", new InfoLink("https://myanimelist.net/anime/1535"));

        // when
        final boolean result = app.watchAnime(entry);

        // then
        verify(eventBusMock, times(1)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isTrue();
        assertThat(app.fetchWatchList().size()).isEqualTo(1);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testRemoveFromWatchListWorks() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final InfoLink infoLink = new InfoLink("https://myanimelist.net/anime/1535");
        final WatchListEntry entry = new WatchListEntry("Death Note", infoLink);
        app.watchAnime(entry);

        // when
        final boolean result = app.removeFromWatchList(infoLink);

        // then
        verify(eventBusMock, times(2)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isTrue();
        assertThat(app.fetchWatchList().isEmpty()).isTrue();
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testRemoveFromWatchListNullAsArgument() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.removeFromWatchList(null);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isFalse();
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testaddAnimeIsNull() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.addAnime(null);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isFalse();
        assertThat(app.fetchAnimeList().size()).isEqualTo(0);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testaddAnimeIsFullEntry() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime("Death Note", new InfoLink("https://myanimelist.net/anime/1535"));
        entry.setEpisodes(37);
        entry.setLocation("/death_note");
        entry.setPicture("https://myanimelist.cdn-dena.com/images/anime/9/9453.jpg");
        entry.setThumbnail("https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = app.addAnime(entry);

        // then
        verify(eventBusMock, times(1)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isTrue();
        assertThat(app.fetchAnimeList().size()).isEqualTo(1);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testaddAnimeIsEntryWithoutEpisodes() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime("Death Note", new InfoLink("https://myanimelist.net/anime/1535"));
        entry.setLocation("/death_note");
        entry.setPicture("https://myanimelist.cdn-dena.com/images/anime/9/9453.jpg");
        entry.setThumbnail("https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = app.addAnime(entry);

        // then
        verify(eventBusMock, times(1)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isTrue();
        assertThat(app.fetchAnimeList().size()).isEqualTo(1);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testaddAnimeIsEntryWithoutInfoLink() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime("Death Note", null);
        entry.setEpisodes(37);
        entry.setLocation("/death_note");
        entry.setPicture("https://myanimelist.cdn-dena.com/images/anime/9/9453.jpg");
        entry.setThumbnail("https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = app.addAnime(entry);

        // then
        verify(eventBusMock, times(1)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isTrue();
        assertThat(app.fetchAnimeList().size()).isEqualTo(1);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testaddAnimeIsEntryWithoutLocation() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime("Death Note", new InfoLink("https://myanimelist.net/anime/1535"));
        entry.setEpisodes(37);
        entry.setPicture("https://myanimelist.cdn-dena.com/images/anime/9/9453.jpg");
        entry.setThumbnail("https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = app.addAnime(entry);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isFalse();
        assertThat(app.fetchAnimeList().size()).isEqualTo(0);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testaddAnimeIsEntryWithoutPicture() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime("Death Note", new InfoLink("https://myanimelist.net/anime/1535"));
        entry.setEpisodes(37);
        entry.setLocation("/death_note");
        entry.setThumbnail("https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = app.addAnime(entry);

        // then
        verify(eventBusMock, times(1)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isTrue();
        assertThat(app.fetchAnimeList().size()).isEqualTo(1);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testaddAnimeIsEntryWithoutThumbnail() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime("Death Note", new InfoLink("https://myanimelist.net/anime/1535"));
        entry.setEpisodes(37);
        entry.setLocation("/death_note");
        entry.setPicture("https://myanimelist.cdn-dena.com/images/anime/9/9453.jpg");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = app.addAnime(entry);

        // then
        verify(eventBusMock, times(1)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isTrue();
        assertThat(app.fetchAnimeList().size()).isEqualTo(1);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testaddAnimeIsEntryWithoutTitle() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime(null, new InfoLink("https://myanimelist.net/anime/1535"));
        entry.setEpisodes(37);
        entry.setLocation("/death_note");
        entry.setPicture("https://myanimelist.cdn-dena.com/images/anime/9/9453.jpg");
        entry.setThumbnail("https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg");
        entry.setType(AnimeType.TV);

        // when
        final boolean result = app.addAnime(entry);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isFalse();
        assertThat(app.fetchAnimeList().size()).isEqualTo(0);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testaddAnimeIsEntryWithoutType() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime("Death Note", new InfoLink("https://myanimelist.net/anime/1535"));
        entry.setEpisodes(37);
        entry.setLocation("/death_note");
        entry.setPicture("https://myanimelist.cdn-dena.com/images/anime/9/9453.jpg");
        entry.setThumbnail("https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg");

        // when
        final boolean result = app.addAnime(entry);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isFalse();
        assertThat(app.fetchAnimeList().size()).isEqualTo(0);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testAnimeEntryExists() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final InfoLink infoLink = new InfoLink("https://myanimelist.net/anime/1535");
        final Anime entry = new Anime("Death Note", infoLink);
        entry.setEpisodes(37);
        entry.setLocation("/death_note");
        entry.setPicture("https://myanimelist.cdn-dena.com/images/anime/9/9453.jpg");
        entry.setThumbnail("https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg");
        entry.setType(AnimeType.TV);
        app.addAnime(entry);

        // when
        final boolean result = app.animeEntryExists(infoLink);

        // then
        assertThat(result).isTrue();
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testAnimeEntryNotExists() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.animeEntryExists(new InfoLink("https://myanimelist.net/anime/1535"));

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isFalse();
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testAnimeList() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime("Death Note", new InfoLink("https://myanimelist.net/anime/1535"));
        entry.setEpisodes(37);
        entry.setLocation("/death_note");
        entry.setPicture("https://myanimelist.cdn-dena.com/images/anime/9/9453.jpg");
        entry.setThumbnail("https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg");
        entry.setType(AnimeType.TV);
        app.addAnime(entry);

        // when
        final List<Anime> animeList = app.fetchAnimeList();

        // then
        assertThat(animeList.size()).isEqualTo(1);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testRemoveFromAnimeListWorks() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime("Death Note", new InfoLink("https://myanimelist.net/anime/1535"));
        entry.setEpisodes(37);
        entry.setLocation("/death_note");
        entry.setPicture("https://myanimelist.cdn-dena.com/images/anime/9/9453.jpg");
        entry.setThumbnail("https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg");
        entry.setType(AnimeType.TV);
        app.addAnime(entry);

        // when
        final boolean result = app.removeAnime(entry.getId());

        // then
        verify(eventBusMock, times(2)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isTrue();
        assertThat(app.fetchAnimeList().isEmpty()).isTrue();
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testRemoveFromAnimeListNullAsArgument() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        final boolean result = app.removeAnime(null);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(result).isFalse();
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testUpdateOrCreateWithNull() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        // when
        app.updateOrCreate(null);

        // then
        verify(eventBusMock, times(0)).post(any(AnimeListChangedEvent.class));
        assertThat(app.fetchAnimeList().isEmpty()).isTrue();
        assertThat(app.fetchWatchList().isEmpty()).isTrue();
        assertThat(app.fetchFilterList().isEmpty()).isTrue();
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testUpdateOrCreateForNewAnimeEntry() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime("Death Note", new InfoLink("https://myanimelist.net/anime/1535"));
        entry.setEpisodes(37);
        entry.setLocation("/death_note");
        entry.setPicture("https://myanimelist.cdn-dena.com/images/anime/9/9453.jpg");
        entry.setThumbnail("https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg");
        entry.setType(AnimeType.TV);

        // when
        app.updateOrCreate(entry);

        // then
        verify(eventBusMock, times(1)).post(any(AnimeListChangedEvent.class));
        assertThat(app.fetchAnimeList().isEmpty()).isFalse();
        assertThat(app.fetchAnimeList().get(0)).isEqualTo(entry);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testUpdateOrCreateForModifiedAnimeEntry() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime entry = new Anime("Death Note", new InfoLink("https://myanimelist.net/anime/1535"));
        entry.setEpisodes(35);
        entry.setLocation("/death_note");
        entry.setPicture("https://myanimelist.cdn-dena.com/images/anime/9/9453.jpg");
        entry.setThumbnail("https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg");
        entry.setType(AnimeType.TV);

        app.addAnime(entry);

        final int episodes = 37;
        entry.setEpisodes(episodes);

        // when
        app.updateOrCreate(entry);

        // then
        verify(eventBusMock, times(2)).post(any(AnimeListChangedEvent.class));
        assertThat(app.fetchAnimeList().isEmpty()).isFalse();
        assertThat(app.fetchAnimeList().get(0).getEpisodes()).isEqualTo(episodes);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testUpdateOrCreateForNewFilterEntry() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final FilterListEntry entry = new FilterListEntry("Death Note", "https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg", new InfoLink("https://myanimelist.net/anime/1535"));

        // when
        app.updateOrCreate(entry);

        // then
        verify(eventBusMock, times(1)).post(any(AnimeListChangedEvent.class));
        assertThat(app.fetchFilterList().isEmpty()).isFalse();
        assertThat(app.fetchFilterList().get(0)).isEqualTo(entry);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testUpdateOrCreateForModifiedFilterEntry() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final FilterListEntry entry = new FilterListEntry("Death Note", AbstractMinimalEntry.NO_IMG_THUMB, new InfoLink("https://myanimelist.net/anime/1535"));

        app.filterAnime(entry);

        final String thumbnail = "https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg";
        entry.setThumbnail(thumbnail);

        // when
        app.updateOrCreate(entry);

        // then
        verify(eventBusMock, times(2)).post(any(AnimeListChangedEvent.class));
        assertThat(app.fetchFilterList().isEmpty()).isFalse();
        assertThat(app.fetchFilterList().get(0).getThumbnail()).isEqualTo(thumbnail);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testUpdateOrCreateForNewWatchListEntry() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final WatchListEntry entry = new WatchListEntry("Death Note", "https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg", new InfoLink("https://myanimelist.net/anime/1535"));

        // when
        app.updateOrCreate(entry);

        // then
        verify(eventBusMock, times(1)).post(any(AnimeListChangedEvent.class));
        assertThat(app.fetchWatchList().isEmpty()).isFalse();
        assertThat(app.fetchWatchList().get(0)).isEqualTo(entry);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testUpdateOrCreateForModifiedWatchListEntry() {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final WatchListEntry entry = new WatchListEntry("Death Note", AbstractMinimalEntry.NO_IMG_THUMB, new InfoLink("https://myanimelist.net/anime/1535"));

        app.watchAnime(entry);

        final String thumbnail = "https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg";
        entry.setThumbnail(thumbnail);

        // when
        app.updateOrCreate(entry);

        // then
        verify(eventBusMock, times(2)).post(any(AnimeListChangedEvent.class));
        assertThat(app.fetchWatchList().isEmpty()).isFalse();
        assertThat(app.fetchWatchList().get(0).getThumbnail()).isEqualTo(thumbnail);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testThatPredefinedListIsExportedCorrectly() throws SAXException, ParserConfigurationException, IOException {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final List<Anime> list = newArrayList();

        final Anime bokuDake = new Anime("Boku dake ga Inai Machi", new InfoLink("https://myanimelist.net/anime/31043"));
        bokuDake.setEpisodes(12);
        bokuDake.setLocation("/anime/series/boku_dake_ga_inai_machi");
        bokuDake.setType(AnimeType.TV);
        list.add(bokuDake);

        final Anime rurouniKenshin = new Anime("Rurouni Kenshin: Meiji Kenkaku Romantan - Tsuiokuhen", new InfoLink("https://myanimelist.net/anime/44"));
        rurouniKenshin.setEpisodes(4);
        rurouniKenshin.setLocation("/anime/series/rurouni_kenshin");
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

        assertThat(expectedFileBuilder.toString()).isEqualTo(exportedFileBuilder.toString());
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testThatPredefinedListIsNotExportedIfFileSuffixIsNotJson() throws SAXException, ParserConfigurationException, IOException {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final List<Anime> list = newArrayList();

        final Anime bokuDake = new Anime("Boku dake ga Inai Machi", new InfoLink("https://myanimelist.net/anime/31043"));
        bokuDake.setEpisodes(12);
        bokuDake.setLocation("/anime/series/boku_dake_ga_inai_machi");
        bokuDake.setType(AnimeType.TV);
        list.add(bokuDake);

        final Anime rurouniKenshin = new Anime("Rurouni Kenshin: Meiji Kenkaku Romantan - Tsuiokuhen", new InfoLink("https://myanimelist.net/anime/44"));
        rurouniKenshin.setEpisodes(4);
        rurouniKenshin.setLocation("/anime/series/rurouni_kenshin");
        rurouniKenshin.setType(AnimeType.OVA);
        list.add(rurouniKenshin);

        final Path file = Files.createFile(Paths.get(tempFolder + separator + "tempfile.xml"));

        // when
        app.exportList(list, file);

        // then
        final StringBuilder exportedFileBuilder = new StringBuilder();
        Files.readAllLines(file, StandardCharsets.UTF_8).forEach(exportedFileBuilder::append);

        assertThat(isBlank(exportedFileBuilder.toString())).isTrue();
    }


    @Test(groups = UNIT_TEST_GROUP, expectedExceptions = IllegalArgumentException.class)
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


    @Test(groups = UNIT_TEST_GROUP, expectedExceptions = IllegalArgumentException.class)
    public void testThatExportFileIsNull() throws SAXException, ParserConfigurationException, IOException {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final List<Anime> list = newArrayList();

        final Anime bokuDake = new Anime("Boku dake ga Inai Machi", new InfoLink("https://myanimelist.net/anime/31043"));
        bokuDake.setEpisodes(12);
        bokuDake.setLocation("/anime/series/boku_dake_ga_inai_machi");
        bokuDake.setType(AnimeType.TV);
        list.add(bokuDake);

        final Anime rurouniKenshin = new Anime("Rurouni Kenshin: Meiji Kenkaku Romantan - Tsuiokuhen", new InfoLink("https://myanimelist.net/anime/44"));
        rurouniKenshin.setEpisodes(4);
        rurouniKenshin.setLocation("/anime/series/rurouni_kenshin");
        rurouniKenshin.setType(AnimeType.OVA);
        list.add(rurouniKenshin);

        // when
        app.exportList(list, null);

        // then
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testSearchStringIsBlank() {
        // given
        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacadeMock, serviceRepositoryMock, eventBusMock);

        // when
        app.search(null);
        app.search(EMPTY);
        app.search("   ");

        // then
        verify(serviceRepositoryMock, times(0)).startService(any());
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testSearch() {
        // given
        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacadeMock, serviceRepositoryMock, eventBusMock);

        // when
        app.search("Death Note");

        // then
        verify(serviceRepositoryMock, times(1)).startService(any());
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testThaExportedWorksCorrectlyForCsv() throws SAXException, ParserConfigurationException, IOException {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime bokuDake = new Anime("Boku dake ga Inai Machi", new InfoLink("https://myanimelist.net/anime/31043"));
        bokuDake.setEpisodes(12);
        bokuDake.setLocation("/anime/series/boku_dake_ga_inai_machi");
        bokuDake.setType(AnimeType.TV);
        persistenceFacade.addAnime(bokuDake);

        final Anime rurouniKenshin = new Anime("Rurouni Kenshin: Meiji Kenkaku Romantan - Tsuiokuhen", new InfoLink("https://myanimelist.net/anime/44"));
        rurouniKenshin.setEpisodes(4);
        rurouniKenshin.setLocation("/anime/series/rurouni_kenshin");
        rurouniKenshin.setType(AnimeType.OVA);
        persistenceFacade.addAnime(rurouniKenshin);

        final WatchListEntry deathNoteRewrite = new WatchListEntry("Death Note Rewrite", "https://myanimelist.cdn-dena.com/images/anime/13/8518t.jpg", new InfoLink("https://myanimelist.net/anime/2994"));
        persistenceFacade.watchAnime(deathNoteRewrite);

        final FilterListEntry gintama = new FilterListEntry("Gintama", "https://myanimelist.cdn-dena.com/images/anime/2/10038t.jpg", new InfoLink("https://myanimelist.net/anime/918"));
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

        assertThat(expectedFileBuilder.toString()).isEqualTo(exportedFileBuilder.toString());
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testThaExportedWorksCorrectlyForJson() throws SAXException, ParserConfigurationException, IOException {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime bokuDake = new Anime("Boku dake ga Inai Machi", new InfoLink("https://myanimelist.net/anime/31043"));
        bokuDake.setEpisodes(12);
        bokuDake.setLocation("/anime/series/boku_dake_ga_inai_machi");
        bokuDake.setType(AnimeType.TV);
        persistenceFacade.addAnime(bokuDake);

        final Anime rurouniKenshin = new Anime("Rurouni Kenshin: Meiji Kenkaku Romantan - Tsuiokuhen", new InfoLink("https://myanimelist.net/anime/44"));
        rurouniKenshin.setEpisodes(4);
        rurouniKenshin.setLocation("/anime/series/rurouni_kenshin");
        rurouniKenshin.setType(AnimeType.OVA);
        persistenceFacade.addAnime(rurouniKenshin);

        final WatchListEntry deathNoteRewrite = new WatchListEntry("Death Note Rewrite", "https://myanimelist.cdn-dena.com/images/anime/13/8518t.jpg", new InfoLink("https://myanimelist.net/anime/2994"));
        persistenceFacade.watchAnime(deathNoteRewrite);

        final FilterListEntry gintama = new FilterListEntry("Gintama", "https://myanimelist.cdn-dena.com/images/anime/2/10038t.jpg", new InfoLink("https://myanimelist.net/anime/918"));
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

        assertThat(expectedFileBuilder.toString()).isEqualTo(exportedFileBuilder.toString());
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testThaExportedIsNotExecutedBecauseFileSuffixDoesNotMatchAnyKnownFileType() throws SAXException, ParserConfigurationException, IOException {
        // given
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, eventBusMock);

        final Manami app = new Manami(cacheMock, new CommandService(eventBusMock), configMock, persistenceFacade, serviceRepositoryMock, eventBusMock);

        final Anime bokuDake = new Anime("Boku dake ga Inai Machi", new InfoLink("https://myanimelist.net/anime/31043"));
        bokuDake.setEpisodes(12);
        bokuDake.setLocation("/anime/series/boku_dake_ga_inai_machi");
        bokuDake.setType(AnimeType.TV);
        persistenceFacade.addAnime(bokuDake);

        final Anime rurouniKenshin = new Anime("Rurouni Kenshin: Meiji Kenkaku Romantan - Tsuiokuhen", new InfoLink("https://myanimelist.net/anime/44"));
        rurouniKenshin.setEpisodes(4);
        rurouniKenshin.setLocation("/anime/series/rurouni_kenshin");
        rurouniKenshin.setType(AnimeType.OVA);
        persistenceFacade.addAnime(rurouniKenshin);

        final WatchListEntry deathNoteRewrite = new WatchListEntry("Death Note Rewrite", "https://myanimelist.cdn-dena.com/images/anime/13/8518t.jpg", new InfoLink("https://myanimelist.net/anime/2994"));
        persistenceFacade.watchAnime(deathNoteRewrite);

        final FilterListEntry gintama = new FilterListEntry("Gintama", "https://myanimelist.cdn-dena.com/images/anime/2/10038t.jpg", new InfoLink("https://myanimelist.net/anime/918"));
        persistenceFacade.filterAnime(gintama);

        final Path file = Files.createFile(Paths.get(tempFolder + separator + "tempfile.test"));

        // when
        app.export(file);

        // then
        final StringBuilder exportedFileBuilder = new StringBuilder();
        Files.readAllLines(file, StandardCharsets.UTF_8).forEach(exportedFileBuilder::append);

        assertThat(isBlank(exportedFileBuilder.toString())).isTrue();
    }


    @Test(groups = UNIT_TEST_GROUP)
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
        assertThat(fetchAnimeList).isNotNull();
        assertThat(fetchAnimeList.isEmpty()).isFalse();
        assertThat(fetchAnimeList.size()).isEqualTo(2);

        final Anime bokuDake = fetchAnimeList.get(0);
        assertThat(bokuDake).isNotNull();
        assertThat(bokuDake.getEpisodes()).isEqualTo(12);
        assertThat(bokuDake.getInfoLink().getUrl()).isEqualTo("https://myanimelist.net/anime/31043");
        assertThat(bokuDake.getLocation()).isEqualTo("/anime/series/boku_dake_ga_inai_machi");
        assertThat(bokuDake.getTitle()).isEqualTo("Boku dake ga Inai Machi");
        assertThat(bokuDake.getType()).isEqualTo(AnimeType.TV);

        final Anime rurouniKenshin = fetchAnimeList.get(1);
        assertThat(rurouniKenshin).isNotNull();
        assertThat(rurouniKenshin.getEpisodes()).isEqualTo(4);
        assertThat(rurouniKenshin.getInfoLink().getUrl()).isEqualTo("https://myanimelist.net/anime/44");
        assertThat(rurouniKenshin.getLocation()).isEqualTo("/anime/series/rurouni_kenshin");
        assertThat(rurouniKenshin.getTitle()).isEqualTo("Rurouni Kenshin: Meiji Kenkaku Romantan - Tsuiokuhen");
        assertThat(rurouniKenshin.getType()).isEqualTo(AnimeType.OVA);
    }


    @Test(groups = UNIT_TEST_GROUP)
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
        assertThat(fetchAnimeList).isNotNull();
        assertThat(fetchAnimeList.isEmpty()).isFalse();
        assertThat(fetchAnimeList.size()).isEqualTo(2);

        final Anime bokuDake = fetchAnimeList.get(0);
        assertThat(bokuDake).isNotNull();
        assertThat(bokuDake.getEpisodes()).isEqualTo(12);
        assertThat(bokuDake.getInfoLink().getUrl()).isEqualTo("https://myanimelist.net/anime/31043");
        assertThat(bokuDake.getLocation()).isEqualTo("/anime/series/boku_dake_ga_inai_machi");
        assertThat(bokuDake.getTitle()).isEqualTo("Boku dake ga Inai Machi");
        assertThat(bokuDake.getType()).isEqualTo(AnimeType.TV);

        final Anime rurouniKenshin = fetchAnimeList.get(1);
        assertThat(rurouniKenshin).isNotNull();
        assertThat(rurouniKenshin.getEpisodes()).isEqualTo(4);
        assertThat(rurouniKenshin.getInfoLink().getUrl()).isEqualTo("https://myanimelist.net/anime/44");
        assertThat(rurouniKenshin.getLocation()).isEqualTo("/anime/series/rurouni_kenshin");
        assertThat(rurouniKenshin.getTitle()).isEqualTo("Rurouni Kenshin: Meiji Kenkaku Romantan - Tsuiokuhen");
        assertThat(rurouniKenshin.getType()).isEqualTo(AnimeType.OVA);
    }


    @Test(groups = UNIT_TEST_GROUP)
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
        assertThat(fetchAnimeList).isNotNull();
        assertThat(fetchAnimeList.isEmpty()).isFalse();
        assertThat(fetchAnimeList.size()).isEqualTo(2);

        final Anime deathNote = fetchAnimeList.get(0);
        assertThat(deathNote).isNotNull();
        assertThat(deathNote.getEpisodes()).isEqualTo(37);
        assertThat(deathNote.getInfoLink().getUrl()).isEqualTo("https://myanimelist.net/anime/1535");
        assertThat(deathNote.getLocation()).isEqualTo("/");
        assertThat(deathNote.getTitle()).isEqualTo("Death Note");
        assertThat(deathNote.getType()).isEqualTo(AnimeType.TV);

        final Anime rurouniKenshin = fetchAnimeList.get(1);
        assertThat(rurouniKenshin).isNotNull();
        assertThat(rurouniKenshin.getEpisodes()).isEqualTo(94);
        assertThat(rurouniKenshin.getInfoLink().getUrl()).isEqualTo("https://myanimelist.net/anime/45");
        assertThat(rurouniKenshin.getLocation()).isEqualTo("/");
        assertThat(rurouniKenshin.getTitle()).isEqualTo("Rurouni Kenshin: Meiji Kenkaku Romantan");
        assertThat(rurouniKenshin.getType()).isEqualTo(AnimeType.TV);

        final List<WatchListEntry> fetchWatchList = persistenceFacade.fetchWatchList();
        assertThat(fetchWatchList).isNotNull();
        assertThat(fetchWatchList.isEmpty()).isFalse();
        assertThat(fetchWatchList.size()).isEqualTo(2);

        final WatchListEntry akatsukiNoYona = fetchWatchList.get(0);
        assertThat(akatsukiNoYona).isNotNull();
        assertThat(akatsukiNoYona.getInfoLink().getUrl()).isEqualTo("https://myanimelist.net/anime/25013");
        assertThat(akatsukiNoYona.getTitle()).isEqualTo("Akatsuki no Yona");
        assertThat(akatsukiNoYona.getThumbnail()).isEqualTo("https://myanimelist.cdn-dena.com/images/qm_50.gif");

        final WatchListEntry aldnohaZero = fetchWatchList.get(1);
        assertThat(aldnohaZero).isNotNull();
        assertThat(aldnohaZero.getInfoLink().getUrl()).isEqualTo("https://myanimelist.net/anime/27655");
        assertThat(aldnohaZero.getTitle()).isEqualTo("Aldnoah.Zero 2nd Season");
        assertThat(aldnohaZero.getThumbnail()).isEqualTo("https://myanimelist.cdn-dena.com/images/qm_50.gif");

        final List<FilterListEntry> fetchFilterList = persistenceFacade.fetchFilterList();
        assertThat(fetchFilterList).isNotNull();
        assertThat(fetchFilterList.isEmpty()).isFalse();
        assertThat(fetchFilterList.size()).isEqualTo(2);

        final FilterListEntry matanteiLokiRagnarok = fetchFilterList.get(0);
        assertThat(matanteiLokiRagnarok).isNotNull();
        assertThat(matanteiLokiRagnarok.getInfoLink().getUrl()).isEqualTo("https://myanimelist.net/anime/335");
        assertThat(matanteiLokiRagnarok.getThumbnail()).isEqualTo("https://myanimelist.cdn-dena.com/images/qm_50.gif");
        assertThat(matanteiLokiRagnarok.getTitle()).isEqualTo("Matantei Loki Ragnarok");

        final FilterListEntry saiunkokuMonogatari = fetchFilterList.get(1);
        assertThat(saiunkokuMonogatari).isNotNull();
        assertThat(saiunkokuMonogatari.getInfoLink().getUrl()).isEqualTo("https://myanimelist.net/anime/957");
        assertThat(saiunkokuMonogatari.getThumbnail()).isEqualTo("https://myanimelist.cdn-dena.com/images/qm_50.gif");
        assertThat(saiunkokuMonogatari.getTitle()).isEqualTo("Saiunkoku Monogatari");
    }


    @Test(groups = UNIT_TEST_GROUP)
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
        assertThat(animeList).isNotNull();
        assertThat(animeList.isEmpty()).isTrue();

        final List<WatchListEntry> watchList = persistenceFacade.fetchWatchList();
        assertThat(watchList).isNotNull();
        assertThat(watchList.isEmpty()).isTrue();

        final List<FilterListEntry> filterList = persistenceFacade.fetchFilterList();
        assertThat(filterList).isNotNull();
        assertThat(filterList.isEmpty()).isTrue();
    }
}

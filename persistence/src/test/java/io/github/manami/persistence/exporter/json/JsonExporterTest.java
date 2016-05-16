package io.github.manami.persistence.exporter.json;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
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

import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.persistence.PersistenceFacade;
import io.github.manami.persistence.inmemory.InMemoryPersistenceHandler;
import io.github.manami.persistence.inmemory.animelist.InMemoryAnimeListHandler;
import io.github.manami.persistence.inmemory.filterlist.InMemoryFilterListHandler;
import io.github.manami.persistence.inmemory.watchlist.InMemoryWatchListHandler;

public class JsonExporterTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    private static final String TEST_ANIME_LIST_FILE = "test_anime_list.json";
    private static final String TEST_RECOMMENDATIONS_FILE = "test_recommendations_list.json";
    private static final String ANIME_LIST_EXPORT_FILE = "test_anime_list_export.json";
    private JsonExporter jsonExporter;
    private File file;
    private InMemoryAnimeListHandler inMemoryAnimeListHandler;
    private InMemoryFilterListHandler inMemoryFilterListHandler;
    private InMemoryWatchListHandler inMemoryWatchListHandler;


    @Before
    public void setUp() throws IOException {
        inMemoryAnimeListHandler = new InMemoryAnimeListHandler();
        inMemoryFilterListHandler = new InMemoryFilterListHandler();
        inMemoryWatchListHandler = new InMemoryWatchListHandler();
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(inMemoryAnimeListHandler, inMemoryFilterListHandler, inMemoryWatchListHandler);
        final PersistenceFacade persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, mock(EventBus.class));
        jsonExporter = new JsonExporter(persistenceFacade);
        file = testFolder.newFile(ANIME_LIST_EXPORT_FILE);
    }


    @Test
    public void testThatAnimeListIsExportedCorrectly() throws SAXException, ParserConfigurationException, IOException {
        // given
        final Anime bokuDake = new Anime();
        bokuDake.setEpisodes(12);
        bokuDake.setInfoLink("http://myanimelist.net/anime/31043");
        bokuDake.setLocation("/anime/series/boku_dake_ga_inai_machi");
        bokuDake.setTitle("Boku dake ga Inai Machi");
        bokuDake.setType(AnimeType.TV);
        inMemoryAnimeListHandler.addAnime(bokuDake);

        final Anime rurouniKenshin = new Anime();
        rurouniKenshin.setEpisodes(4);
        rurouniKenshin.setInfoLink("http://myanimelist.net/anime/44");
        rurouniKenshin.setLocation("/anime/series/rurouni_kenshin");
        rurouniKenshin.setTitle("Rurouni Kenshin: Meiji Kenkaku Romantan - Tsuiokuhen");
        rurouniKenshin.setType(AnimeType.OVA);
        inMemoryAnimeListHandler.addAnime(rurouniKenshin);

        final WatchListEntry deathNoteRewrite = new WatchListEntry("Death Note Rewrite", "http://cdn.myanimelist.net/images/anime/13/8518t.jpg", "http://myanimelist.net/anime/2994");
        inMemoryWatchListHandler.watchAnime(deathNoteRewrite);

        final FilterEntry gintama = new FilterEntry("Gintama", "http://cdn.myanimelist.net/images/anime/2/10038t.jpg", "http://myanimelist.net/anime/918");
        inMemoryFilterListHandler.filterAnime(gintama);

        final ClassPathResource resource = new ClassPathResource(TEST_ANIME_LIST_FILE);
        final StringBuilder expectedFileBuilder = new StringBuilder();
        Files.readLines(resource.getFile(), Charset.forName("UTF-8")).forEach(expectedFileBuilder::append);

        // when
        jsonExporter.exportAll(file.toPath());

        // then
        final StringBuilder exportedFileBuilder = new StringBuilder();
        Files.readLines(file, Charset.forName("UTF-8")).forEach(exportedFileBuilder::append);

        assertThat(expectedFileBuilder.toString(), equalTo(exportedFileBuilder.toString()));
    }


    @Test
    public void testThatPredefinedListIsExportedCorrectly() throws SAXException, ParserConfigurationException, IOException {
        // given
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

        // when
        jsonExporter.exportList(list, file.toPath());

        // then
        final StringBuilder exportedFileBuilder = new StringBuilder();
        Files.readLines(file, Charset.forName("UTF-8")).forEach(exportedFileBuilder::append);

        assertThat(expectedFileBuilder.toString(), equalTo(exportedFileBuilder.toString()));
    }
}

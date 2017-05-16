package io.github.manami.persistence.exporter.json;

import static com.google.common.collect.Lists.newArrayList;
import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.eventbus.EventBus;
import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.persistence.PersistenceFacade;
import io.github.manami.persistence.inmemory.InMemoryPersistenceHandler;
import io.github.manami.persistence.inmemory.animelist.InMemoryAnimeListHandler;
import io.github.manami.persistence.inmemory.filterlist.InMemoryFilterListHandler;
import io.github.manami.persistence.inmemory.watchlist.InMemoryWatchListHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

@Slf4j
public class JsonExporterTest {

  private static final String TEST_ANIME_LIST_FILE = "test_anime_list.json";
  private static final String TEST_RECOMMENDATIONS_FILE = "test_recommendations_list.json";
  private static final String ANIME_LIST_EXPORT_FILE = "test_anime_list_export.json";
  private JsonExporter jsonExporter;
  private Path file;
  private Path tempFolder;
  private PersistenceFacade persistenceFacade;


  @BeforeMethod
  public void setUp() throws IOException {
    final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(
        new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(),
        new InMemoryWatchListHandler());
    persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, mock(EventBus.class));
    jsonExporter = new JsonExporter(persistenceFacade);
    tempFolder = Files.createTempDirectory(String.valueOf(System.currentTimeMillis()));
    final String separator = FileSystems.getDefault().getSeparator();
    file = Files.createFile(Paths.get(tempFolder + separator + ANIME_LIST_EXPORT_FILE));
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
  public void testThatAnimeListIsExportedCorrectly()
      throws SAXException, ParserConfigurationException, IOException {
    // given
    final Anime bokuDake = new Anime("Boku dake ga Inai Machi",
        new InfoLink("http://myanimelist.net/anime/31043"));
    bokuDake.setEpisodes(12);
    bokuDake.setLocation("/anime/series/boku_dake_ga_inai_machi");
    bokuDake.setType(AnimeType.TV);
    persistenceFacade.addAnime(bokuDake);

    final Anime rurouniKenshin = new Anime("Rurouni Kenshin: Meiji Kenkaku Romantan - Tsuiokuhen",
        new InfoLink("http://myanimelist.net/anime/44"));
    rurouniKenshin.setEpisodes(4);
    rurouniKenshin.setLocation("/anime/series/rurouni_kenshin");
    rurouniKenshin.setType(AnimeType.OVA);
    persistenceFacade.addAnime(rurouniKenshin);

    final WatchListEntry deathNoteRewrite = new WatchListEntry("Death Note Rewrite",
        "http://cdn.myanimelist.net/images/anime/13/8518t.jpg",
        new InfoLink("http://myanimelist.net/anime/2994"));
    persistenceFacade.watchAnime(deathNoteRewrite);

    final FilterEntry gintama = new FilterEntry("Gintama",
        "http://cdn.myanimelist.net/images/anime/2/10038t.jpg",
        new InfoLink("http://myanimelist.net/anime/918"));
    persistenceFacade.filterAnime(gintama);

    final ClassPathResource resource = new ClassPathResource(TEST_ANIME_LIST_FILE);
    final StringBuilder expectedFileBuilder = new StringBuilder();
    Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8)
        .forEach(expectedFileBuilder::append);

    // when
    jsonExporter.exportAll(file);

    // then
    final StringBuilder exportedFileBuilder = new StringBuilder();
    Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8)
        .forEach(exportedFileBuilder::append);

    assertThat(expectedFileBuilder.toString()).isEqualTo(exportedFileBuilder.toString());
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testThatPredefinedListIsExportedCorrectly()
      throws SAXException, ParserConfigurationException, IOException {
    // given
    final List<Anime> list = newArrayList();

    final Anime bokuDake = new Anime("Boku dake ga Inai Machi",
        new InfoLink("http://myanimelist.net/anime/31043"));
    bokuDake.setEpisodes(12);
    bokuDake.setLocation("/anime/series/boku_dake_ga_inai_machi");
    bokuDake.setType(AnimeType.TV);
    list.add(bokuDake);

    final Anime rurouniKenshin = new Anime("Rurouni Kenshin: Meiji Kenkaku Romantan - Tsuiokuhen",
        new InfoLink("http://myanimelist.net/anime/44"));
    rurouniKenshin.setEpisodes(4);
    rurouniKenshin.setLocation("/anime/series/rurouni_kenshin");
    rurouniKenshin.setType(AnimeType.OVA);
    list.add(rurouniKenshin);

    final ClassPathResource resource = new ClassPathResource(TEST_RECOMMENDATIONS_FILE);
    final StringBuilder expectedFileBuilder = new StringBuilder();
    Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8)
        .forEach(expectedFileBuilder::append);

    // when
    jsonExporter.exportList(list, file);

    // then
    final StringBuilder exportedFileBuilder = new StringBuilder();
    Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8)
        .forEach(exportedFileBuilder::append);

    assertThat(expectedFileBuilder.toString()).isEqualTo(exportedFileBuilder.toString());
  }
}
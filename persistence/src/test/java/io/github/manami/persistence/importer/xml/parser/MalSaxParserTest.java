package io.github.manami.persistence.importer.xml.parser;

import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.eventbus.EventBus;
import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.persistence.PersistenceFacade;
import io.github.manami.persistence.importer.xml.XmlImporter;
import io.github.manami.persistence.importer.xml.XmlImporter.XmlStrategy;
import io.github.manami.persistence.inmemory.InMemoryPersistenceHandler;
import io.github.manami.persistence.inmemory.animelist.InMemoryAnimeListHandler;
import io.github.manami.persistence.inmemory.filterlist.InMemoryFilterListHandler;
import io.github.manami.persistence.inmemory.watchlist.InMemoryWatchListHandler;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

public class MalSaxParserTest {

  private static final String MAL_EXPORT_FILE = "mal_export.xml";
  private XmlImporter xmlImporter;
  private Path file;
  private PersistenceFacade persistenceFacade;


  @BeforeMethod
  public void setUp() throws IOException {
    final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(
        new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(),
        new InMemoryWatchListHandler());
    persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, mock(EventBus.class));
    xmlImporter = new XmlImporter(XmlStrategy.MAL, persistenceFacade);
    final ClassPathResource resource = new ClassPathResource(MAL_EXPORT_FILE);
    file = resource.getFile().toPath();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testThatAnimeListIsParsedCorrectly()
      throws SAXException, ParserConfigurationException, IOException {
    // given

    // when
    xmlImporter.importFile(file);

    // then
    final List<Anime> fetchAnimeList = persistenceFacade.fetchAnimeList();
    assertThat(fetchAnimeList).isNotNull();
    assertThat(fetchAnimeList.isEmpty()).isFalse();
    assertThat(fetchAnimeList.size()).isEqualTo(2);

    final Anime deathNote = fetchAnimeList.get(0);
    assertThat(deathNote).isNotNull();
    assertThat(deathNote.getEpisodes()).isEqualTo(37);
    assertThat(deathNote.getInfoLink().getUrl()).isEqualTo("http://myanimelist.net/anime/1535");
    assertThat(deathNote.getLocation()).isEqualTo("/");
    assertThat(deathNote.getTitle()).isEqualTo("Death Note");
    assertThat(deathNote.getType()).isEqualTo(AnimeType.TV);

    final Anime rurouniKenshin = fetchAnimeList.get(1);
    assertThat(rurouniKenshin).isNotNull();
    assertThat(rurouniKenshin.getEpisodes()).isEqualTo(94);
    assertThat(rurouniKenshin.getInfoLink().getUrl()).isEqualTo("http://myanimelist.net/anime/45");
    assertThat(rurouniKenshin.getLocation()).isEqualTo("/");
    assertThat(rurouniKenshin.getTitle()).isEqualTo("Rurouni Kenshin: Meiji Kenkaku Romantan");
    assertThat(rurouniKenshin.getType()).isEqualTo(AnimeType.TV);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testThatWatchListIsParsedCorrectly()
      throws SAXException, ParserConfigurationException, IOException {
    // given

    // when
    xmlImporter.importFile(file);

    // then
    final List<WatchListEntry> fetchWatchList = persistenceFacade.fetchWatchList();
    assertThat(fetchWatchList).isNotNull();
    assertThat(fetchWatchList.isEmpty()).isFalse();
    assertThat(fetchWatchList.size()).isEqualTo(2);

    final WatchListEntry akatsukiNoYona = fetchWatchList.get(0);
    assertThat(akatsukiNoYona).isNotNull();
    assertThat(akatsukiNoYona.getInfoLink().getUrl())
        .isEqualTo("http://myanimelist.net/anime/25013");
    assertThat(akatsukiNoYona.getTitle()).isEqualTo("Akatsuki no Yona");
    assertThat(akatsukiNoYona.getThumbnail())
        .isEqualTo("https://myanimelist.cdn-dena.com/images/qm_50.gif");

    final WatchListEntry aldnoahZero = fetchWatchList.get(1);
    assertThat(aldnoahZero).isNotNull();
    assertThat(aldnoahZero.getInfoLink().getUrl()).isEqualTo("http://myanimelist.net/anime/27655");
    assertThat(aldnoahZero.getTitle()).isEqualTo("Aldnoah.Zero 2nd Season");
    assertThat(aldnoahZero.getThumbnail())
        .isEqualTo("https://myanimelist.cdn-dena.com/images/qm_50.gif");
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testThatFilterListIsParsedCorrectly()
      throws SAXException, ParserConfigurationException, IOException {
    // given

    // when
    xmlImporter.importFile(file);

    // then
    final List<FilterEntry> fetchFilterList = persistenceFacade.fetchFilterList();
    assertThat(fetchFilterList).isNotNull();
    assertThat(fetchFilterList.isEmpty()).isFalse();
    assertThat(fetchFilterList.size()).isEqualTo(2);

    final FilterEntry matanteiLokiRagnarok = fetchFilterList.get(0);
    assertThat(matanteiLokiRagnarok).isNotNull();
    assertThat(matanteiLokiRagnarok.getInfoLink().getUrl())
        .isEqualTo("http://myanimelist.net/anime/335");
    assertThat(matanteiLokiRagnarok.getThumbnail())
        .isEqualTo("https://myanimelist.cdn-dena.com/images/qm_50.gif");
    assertThat(matanteiLokiRagnarok.getTitle()).isEqualTo("Matantei Loki Ragnarok");

    final FilterEntry saiunkokuMonogatari = fetchFilterList.get(1);
    assertThat(saiunkokuMonogatari).isNotNull();
    assertThat(saiunkokuMonogatari.getInfoLink().getUrl())
        .isEqualTo("http://myanimelist.net/anime/957");
    assertThat(saiunkokuMonogatari.getThumbnail())
        .isEqualTo("https://myanimelist.cdn-dena.com/images/qm_50.gif");
    assertThat(saiunkokuMonogatari.getTitle()).isEqualTo("Saiunkoku Monogatari");
  }
}

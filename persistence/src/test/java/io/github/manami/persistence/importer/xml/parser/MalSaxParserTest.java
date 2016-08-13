package io.github.manami.persistence.importer.xml.parser;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

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

public class MalSaxParserTest {

    private static final String MAL_EXPORT_FILE = "mal_export.xml";
    private XmlImporter xmlImporter;
    private Path file;
    private PersistenceFacade persistenceFacade;


    @BeforeMethod
    public void setUp() throws IOException {
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, mock(EventBus.class));
        xmlImporter = new XmlImporter(XmlStrategy.MAL, persistenceFacade);
        final ClassPathResource resource = new ClassPathResource(MAL_EXPORT_FILE);
        file = resource.getFile().toPath();
    }


    @Test(groups = "unitTest")
    public void testThatAnimeListIsParsedCorrectly() throws SAXException, ParserConfigurationException, IOException {
        // given

        // when
        xmlImporter.importFile(file);

        // then
        final List<Anime> fetchAnimeList = persistenceFacade.fetchAnimeList();
        assertNotNull(fetchAnimeList);
        assertEquals(fetchAnimeList.isEmpty(), false);
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
    }


    @Test(groups = "unitTest")
    public void testThatWatchListIsParsedCorrectly() throws SAXException, ParserConfigurationException, IOException {
        // given

        // when
        xmlImporter.importFile(file);

        // then
        final List<WatchListEntry> fetchWatchList = persistenceFacade.fetchWatchList();
        assertNotNull(fetchWatchList);
        assertEquals(fetchWatchList.isEmpty(), false);
        assertEquals(fetchWatchList.size(), 2);

        final WatchListEntry akatsukiNoYona = fetchWatchList.get(0);
        assertNotNull(akatsukiNoYona);
        assertEquals(akatsukiNoYona.getInfoLink(), "http://myanimelist.net/anime/25013");
        assertEquals(akatsukiNoYona.getTitle(), "Akatsuki no Yona");
        assertEquals(akatsukiNoYona.getThumbnail(), "http://cdn.myanimelist.net/images/qm_50.gif");

        final WatchListEntry aldnoahZero = fetchWatchList.get(1);
        assertNotNull(aldnoahZero);
        assertEquals(aldnoahZero.getInfoLink(), "http://myanimelist.net/anime/27655");
        assertEquals(aldnoahZero.getTitle(), "Aldnoah.Zero 2nd Season");
        assertEquals(aldnoahZero.getThumbnail(), "http://cdn.myanimelist.net/images/qm_50.gif");
    }


    @Test(groups = "unitTest")
    public void testThatFilterListIsParsedCorrectly() throws SAXException, ParserConfigurationException, IOException {
        // given

        // when
        xmlImporter.importFile(file);

        // then
        final List<FilterEntry> fetchFilterList = persistenceFacade.fetchFilterList();
        assertNotNull(fetchFilterList);
        assertEquals(fetchFilterList.isEmpty(), false);
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
}

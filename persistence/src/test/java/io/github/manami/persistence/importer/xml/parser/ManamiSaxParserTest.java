package io.github.manami.persistence.importer.xml.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
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

public class ManamiSaxParserTest {

    private static final String TEST_ANIME_LIST_FILE = "test_anime_list.xml";
    private XmlImporter xmlImporter;
    private Path file;
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
        xmlImporter = new XmlImporter(XmlStrategy.MANAMI, persistenceFacade);
        final ClassPathResource resource = new ClassPathResource(TEST_ANIME_LIST_FILE);
        file = resource.getFile().toPath();
    }


    @Test
    public void testThatAnimeListIsParsedCorrectly() throws SAXException, ParserConfigurationException, IOException {
        // given

        // when
        xmlImporter.importFile(file);

        // then
        final List<Anime> fetchAnimeList = inMemoryAnimeListHandler.fetchAnimeList();
        assertThat(fetchAnimeList, not(nullValue()));
        assertThat(fetchAnimeList.isEmpty(), equalTo(false));
        assertThat(fetchAnimeList.size(), equalTo(2));
        final Anime bokuDake = fetchAnimeList.get(0);
        final Anime rurouniKenshin = fetchAnimeList.get(1);
        assertThat(bokuDake, not(nullValue()));
        assertThat(bokuDake.getEpisodes(), equalTo(12));
        assertThat(bokuDake.getInfoLink(), equalTo("http://myanimelist.net/anime/31043"));
        assertThat(bokuDake.getLocation(), equalTo("/anime/series/boku_dake_ga_inai_machi"));
        assertThat(bokuDake.getTitle(), equalTo("Boku dake ga Inai Machi"));
        assertThat(bokuDake.getType(), equalTo(AnimeType.TV));
        assertThat(rurouniKenshin, not(nullValue()));
        assertThat(rurouniKenshin.getEpisodes(), equalTo(4));
        assertThat(rurouniKenshin.getInfoLink(), equalTo("http://myanimelist.net/anime/44"));
        assertThat(rurouniKenshin.getLocation(), equalTo("/anime/series/rurouni_kenshin"));
        assertThat(rurouniKenshin.getTitle(), equalTo("Rurouni Kenshin: Meiji Kenkaku Romantan - Tsuiokuhen"));
        assertThat(rurouniKenshin.getType(), equalTo(AnimeType.OVA));
    }


    @Test
    public void testThatWatchListIsParsedCorrectly() throws SAXException, ParserConfigurationException, IOException {
        // given

        // when
        xmlImporter.importFile(file);

        // then
        final List<WatchListEntry> fetchWatchList = inMemoryWatchListHandler.fetchWatchList();
        assertThat(fetchWatchList, not(nullValue()));
        assertThat(fetchWatchList.isEmpty(), equalTo(false));
        assertThat(fetchWatchList.size(), equalTo(1));
        final WatchListEntry deathNoteRewrite = fetchWatchList.get(0);
        assertThat(deathNoteRewrite, not(nullValue()));
        assertThat(deathNoteRewrite.getInfoLink(), equalTo("http://myanimelist.net/anime/2994"));
        assertThat(deathNoteRewrite.getThumbnail(), equalTo("http://cdn.myanimelist.net/images/anime/13/8518t.jpg"));
        assertThat(deathNoteRewrite.getTitle(), equalTo("Death Note Rewrite"));
    }


    @Test
    public void testThatFilterListIsParsedCorrectly() throws SAXException, ParserConfigurationException, IOException {
        // given

        // when
        xmlImporter.importFile(file);

        // then
        final List<FilterEntry> fetchFilterList = inMemoryFilterListHandler.fetchFilterList();
        assertThat(fetchFilterList, not(nullValue()));
        assertThat(fetchFilterList.isEmpty(), equalTo(false));
        assertThat(fetchFilterList.size(), equalTo(1));
        final FilterEntry gintama = fetchFilterList.get(0);
        assertThat(gintama, not(nullValue()));
        assertThat(gintama.getInfoLink(), equalTo("http://myanimelist.net/anime/918"));
        assertThat(gintama.getThumbnail(), equalTo("http://cdn.myanimelist.net/images/anime/2/10038t.jpg"));
        assertThat(gintama.getTitle(), equalTo("Gintama"));
    }
}

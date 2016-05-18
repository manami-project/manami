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

public class MalSaxParserTest {

    private static final String MAL_EXPORT_FILE = "mal_export.xml";
    private XmlImporter xmlImporter;
    private Path file;
    private PersistenceFacade persistenceFacade;


    @Before
    public void setUp() throws IOException {
        final InMemoryPersistenceHandler inMemoryPersistenceHandler = new InMemoryPersistenceHandler(new InMemoryAnimeListHandler(), new InMemoryFilterListHandler(), new InMemoryWatchListHandler());
        persistenceFacade = new PersistenceFacade(inMemoryPersistenceHandler, mock(EventBus.class));
        xmlImporter = new XmlImporter(XmlStrategy.MAL, persistenceFacade);
        final ClassPathResource resource = new ClassPathResource(MAL_EXPORT_FILE);
        file = resource.getFile().toPath();
    }


    @Test
    public void testThatAnimeListIsParsedCorrectly() throws SAXException, ParserConfigurationException, IOException {
        // given

        // when
        xmlImporter.importFile(file);

        // then
        final List<Anime> fetchAnimeList = persistenceFacade.fetchAnimeList();
        assertThat(fetchAnimeList, not(nullValue()));
        assertThat(fetchAnimeList.isEmpty(), equalTo(false));
        assertThat(fetchAnimeList.size(), equalTo(2));

        final Anime deathNote = fetchAnimeList.get(0);
        assertThat(deathNote, not(nullValue()));
        assertThat(deathNote.getEpisodes(), equalTo(37));
        assertThat(deathNote.getInfoLink(), equalTo("http://myanimelist.net/anime/1535"));
        assertThat(deathNote.getLocation(), equalTo("/"));
        assertThat(deathNote.getTitle(), equalTo("Death Note"));
        assertThat(deathNote.getType(), equalTo(AnimeType.TV));

        final Anime rurouniKenshin = fetchAnimeList.get(1);
        assertThat(rurouniKenshin, not(nullValue()));
        assertThat(rurouniKenshin.getEpisodes(), equalTo(94));
        assertThat(rurouniKenshin.getInfoLink(), equalTo("http://myanimelist.net/anime/45"));
        assertThat(rurouniKenshin.getLocation(), equalTo("/"));
        assertThat(rurouniKenshin.getTitle(), equalTo("Rurouni Kenshin: Meiji Kenkaku Romantan"));
        assertThat(rurouniKenshin.getType(), equalTo(AnimeType.TV));
    }


    @Test
    public void testThatWatchListIsParsedCorrectly() throws SAXException, ParserConfigurationException, IOException {
        // given

        // when
        xmlImporter.importFile(file);

        // then
        final List<WatchListEntry> fetchWatchList = persistenceFacade.fetchWatchList();
        assertThat(fetchWatchList, not(nullValue()));
        assertThat(fetchWatchList.isEmpty(), equalTo(false));
        assertThat(fetchWatchList.size(), equalTo(2));

        final WatchListEntry akatsukiNoYona = fetchWatchList.get(0);
        assertThat(akatsukiNoYona, not(nullValue()));
        assertThat(akatsukiNoYona.getInfoLink(), equalTo("http://myanimelist.net/anime/25013"));
        assertThat(akatsukiNoYona.getTitle(), equalTo("Akatsuki no Yona"));
        assertThat(akatsukiNoYona.getThumbnail(), equalTo("http://cdn.myanimelist.net/images/qm_50.gif"));

        final WatchListEntry rurouniKenshin = fetchWatchList.get(1);
        assertThat(rurouniKenshin, not(nullValue()));
        assertThat(rurouniKenshin.getInfoLink(), equalTo("http://myanimelist.net/anime/27655"));
        assertThat(rurouniKenshin.getTitle(), equalTo("Aldnoah.Zero 2nd Season"));
        assertThat(rurouniKenshin.getThumbnail(), equalTo("http://cdn.myanimelist.net/images/qm_50.gif"));
    }


    @Test
    public void testThatFilterListIsParsedCorrectly() throws SAXException, ParserConfigurationException, IOException {
        // given

        // when
        xmlImporter.importFile(file);

        // then
        final List<FilterEntry> fetchFilterList = persistenceFacade.fetchFilterList();
        assertThat(fetchFilterList, not(nullValue()));
        assertThat(fetchFilterList.isEmpty(), equalTo(false));
        assertThat(fetchFilterList.size(), equalTo(2));

        final FilterEntry matanteiLokiRagnarok = fetchFilterList.get(0);
        assertThat(matanteiLokiRagnarok, not(nullValue()));
        assertThat(matanteiLokiRagnarok.getInfoLink(), equalTo("http://myanimelist.net/anime/335"));
        assertThat(matanteiLokiRagnarok.getThumbnail(), equalTo("http://cdn.myanimelist.net/images/qm_50.gif"));
        assertThat(matanteiLokiRagnarok.getTitle(), equalTo("Matantei Loki Ragnarok"));

        final FilterEntry saiunkokuMonogatari = fetchFilterList.get(1);
        assertThat(saiunkokuMonogatari, not(nullValue()));
        assertThat(saiunkokuMonogatari.getInfoLink(), equalTo("http://myanimelist.net/anime/957"));
        assertThat(saiunkokuMonogatari.getThumbnail(), equalTo("http://cdn.myanimelist.net/images/qm_50.gif"));
        assertThat(saiunkokuMonogatari.getTitle(), equalTo("Saiunkoku Monogatari"));
    }
}

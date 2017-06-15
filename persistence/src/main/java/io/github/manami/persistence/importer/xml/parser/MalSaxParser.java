package io.github.manami.persistence.importer.xml.parser;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.persistence.PersistenceFacade;

/**
 * @author manami-project
 * @since 2.0.0
 */
public class MalSaxParser extends DefaultHandler {

    /** Actual anime object. */
    private Anime actAnime;

    /** This is the builder for the text within the elements. */
    private StringBuilder strBuilder;
    private String statusCurrentAnime;

    private InfoLink infoLink;
    private String title;
    private AnimeType animeType;
    private int episodes;

    private final PersistenceFacade persistence;

    private final List<Anime> animeListEntries;
    private final List<Anime> filterListEntries;
    private final List<Anime> watchListEntries;


    /**
     * Constructor awaiting a list.
     *
     * @since 2.0.0
     * @param persistence
     */
    public MalSaxParser(final PersistenceFacade persistence) {
        this.persistence = persistence;
        animeListEntries = newArrayList();
        filterListEntries = newArrayList();
        watchListEntries = newArrayList();
    }


    @Override
    public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes attributes) throws SAXException {
        strBuilder = new StringBuilder();

        if (qName.equals("anime")) {
            actAnime = null;
            infoLink = null;
            title = null;
            animeType = null;
            episodes = 1;
        }
    }


    @Override
    public void endElement(final String namespaceURI, final String localName, final String qName) {

        switch (qName) {
            case "series_animedb_id":
                infoLink = new InfoLink("https://myanimelist.net/anime/" + strBuilder.toString().trim());
                break;
            case "series_title":
                title = strBuilder.toString().trim();
                break;
            case "series_type":
                animeType = AnimeType.findByName(strBuilder.toString().trim());
                break;
            case "series_episodes":
                final String episodeStr = strBuilder.toString().trim();
                if (NumberUtils.isParsable(episodeStr)) {
                    episodes = Integer.valueOf(episodeStr);
                }
                break;
            case "my_status":
                statusCurrentAnime = strBuilder.toString().trim().toLowerCase();
                break;
            case "anime":
                addAnime();
                break;
        }
    }


    private void addAnime() {
        actAnime = new Anime(title, infoLink).type(animeType).episodes(episodes).location("/");

        switch (statusCurrentAnime) {
            case "completed":
                animeListEntries.add(actAnime);
                break;
            case "watching":
            case "plan to watch":
                watchListEntries.add(actAnime);
                break;
            case "dropped":
                filterListEntries.add(actAnime);
                break;
            default:
                break;
        }

        statusCurrentAnime = null;
    }


    @Override
    public void characters(final char ch[], final int start, final int length) {
        strBuilder.append(new String(ch, start, length));
    }


    @Override
    public void endDocument() throws SAXException {
        persistence.addAnimeList(animeListEntries);
        persistence.addFilterList(filterListEntries);
        persistence.addWatchList(watchListEntries);
    }
}

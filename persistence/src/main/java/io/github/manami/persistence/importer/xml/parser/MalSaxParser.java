package io.github.manami.persistence.importer.xml.parser;

import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.persistence.PersistenceFacade;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author manami-project
 * @since 2.0.0
 */
public class MalSaxParser extends DefaultHandler {

    /** Actual anime object. */
    private Anime actAnime = null;

    /** This is the builder for the text within the elements. */
    private StringBuilder strBuilder;

    private String statusCurrentAnime;

    private final PersistenceFacade persistence;

    private final List<Anime> animeListEntries;
    private final List<Anime> filterListEntries;
    private final List<Anime> watchListEntries;


    /**
     * Constructor awaiting a list.
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
            actAnime = new Anime();
            actAnime.setLocation("/");
        }
    }


    @Override
    public void endElement(final String namespaceURI, final String localName, final String qName) {

        switch (qName) {
            case "series_animedb_id":
                if (!actAnime.getInfoLink().isPresent()) {
                    actAnime.setInfoLink(new InfoLink("http://myanimelist.net/anime/" + strBuilder.toString().trim()));
                }
                break;
            case "series_title":
                if (isBlank(actAnime.getTitle())) {
                    actAnime.setTitle(strBuilder.toString().trim());
                }
                break;
            case "series_type":
                if (actAnime.getType() == null) {
                    actAnime.setType(AnimeType.findByName(strBuilder.toString().trim()));
                }
                break;
            case "series_episodes":
                if (actAnime.getEpisodes() == 0 && !"unknown".equalsIgnoreCase(strBuilder.toString().trim())) {
                    actAnime.setEpisodes(Integer.valueOf(strBuilder.toString().trim()));
                }
                break;
            case "my_status":
                statusCurrentAnime = strBuilder.toString().trim().toLowerCase();
                break;
            case "anime":
                addAnime(actAnime);
                break;
        }
    }


    private void addAnime(final Anime anime) {
        switch (statusCurrentAnime) {
            case "completed":
                animeListEntries.add(anime);
                break;
            case "watching":
            case "plan to watch":
                watchListEntries.add(anime);
                break;
            case "dropped":
                filterListEntries.add(anime);
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

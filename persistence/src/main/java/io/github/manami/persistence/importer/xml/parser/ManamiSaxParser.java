package io.github.manami.persistence.importer.xml.parser;

import com.google.common.collect.Lists;
import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.persistence.PersistenceFacade;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.List;

/**
 * @author manami-project
 * @version 2.0.0
 */
public class ManamiSaxParser extends DefaultHandler {

    /** This is the builder for the text within the elements. */
    private StringBuilder strBuilder;

    private final PersistenceFacade persistence;

    private final List<Anime> animeListEntries;
    private final List<FilterEntry> filterListEntries;
    private final List<WatchListEntry> watchListEntries;


    /**
     * Constructor awaiting a list.
     *
     * @version 2.0.0
     * @param persistence
     */
    public ManamiSaxParser(final PersistenceFacade persistence) {
        this.persistence = persistence;
        animeListEntries = Lists.newArrayList();
        filterListEntries = Lists.newArrayList();
        watchListEntries = Lists.newArrayList();
    }


    @Override
    public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes attributes) {
        strBuilder = new StringBuilder();

        switch (qName) {
            case "anime":
                createAnimeEntry(attributes);
                break;
            case "filterEntry":
                createFilterEntry(attributes);
                break;
            case "watchListEntry":
                createWatchListEntry(attributes);
                break;
        }
    }


    @Override
    public void endDocument() throws SAXException {
        persistence.addAnimeList(animeListEntries);
        persistence.addFilterList(filterListEntries);
        persistence.addWatchList(watchListEntries);
    }


    /**
     * @since 2.7.0
     * @param attributes
     */
    private void createAnimeEntry(final Attributes attributes) {
        final Anime actAnime = new Anime();
        actAnime.setTitle(attributes.getValue("title").trim());
        actAnime.setType(AnimeType.findByName(attributes.getValue("type").trim()));
        actAnime.setEpisodes(Integer.valueOf(attributes.getValue("episodes").trim()));
        actAnime.setLocation(attributes.getValue("location").trim());

        final String infoLink = attributes.getValue("infoLink").trim();
        if (StringUtils.isNotBlank(infoLink)) {
            actAnime.setInfoLink(infoLink);
        }
        animeListEntries.add(actAnime);
    }


    /**
     * @since 2.7.0
     * @param attributes
     */
    private void createFilterEntry(final Attributes attributes) {
        final FilterEntry entry = new FilterEntry(attributes.getValue("title").trim(), attributes.getValue("thumbnail").trim(), attributes.getValue("infoLink").trim());
        filterListEntries.add(entry);
    }


    /**
     * @since 2.8.0
     * @param attributes
     */
    private void createWatchListEntry(final Attributes attributes) {
        final WatchListEntry entry = new WatchListEntry(attributes.getValue("title").trim(), attributes.getValue("thumbnail").trim(), attributes.getValue("infoLink").trim());
        watchListEntries.add(entry);
    }


    @Override
    public void characters(final char ch[], final int start, final int length) {
        strBuilder.append(new String(ch, start, length));
    }
}

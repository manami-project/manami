package io.github.manami.persistence.importer.xml.parser;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import io.github.manami.dto.AnimeType;
import io.github.manami.dto.ToolVersion;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterListEntry;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.persistence.PersistenceFacade;
import io.github.manami.persistence.importer.xml.postprocessor.ImportMigrationPostProcessor;

/**
 * @author manami-project
 * @version 2.0.0
 */
public class ManamiSaxParser extends DefaultHandler {

    /** This is the builder for the text within the elements. */
    private StringBuilder strBuilder;

    private final PersistenceFacade persistence;

    private final List<Anime> animeListEntries;
    private final List<FilterListEntry> filterListEntries;
    private final List<WatchListEntry> watchListEntries;

    private ImportMigrationPostProcessor importMigrationPostProcessor;


    /**
     * Constructor awaiting a list.
     *
     * @version 2.0.0
     * @param persistence
     */
    public ManamiSaxParser(final PersistenceFacade persistence) {
        this.persistence = persistence;
        animeListEntries = newArrayList();
        filterListEntries = newArrayList();
        watchListEntries = newArrayList();

    }


    @Override
    public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes attributes) {
        strBuilder = new StringBuilder();

        switch (qName) {
            case "manami":
                importMigrationPostProcessor = new ImportMigrationPostProcessor(ToolVersion.getToolVersion(), attributes.getValue("version"), animeListEntries, filterListEntries, watchListEntries);
                break;
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
        importMigrationPostProcessor.execute();
    }


    private void createAnimeEntry(final Attributes attributes) {
        final String title = attributes.getValue("title").trim();
        final InfoLink infoLink = new InfoLink(attributes.getValue("infoLink").trim());

        final Anime actAnime = new Anime(title, infoLink);
        actAnime.setType(AnimeType.findByName(attributes.getValue("type").trim()));
        actAnime.setEpisodes(Integer.valueOf(attributes.getValue("episodes").trim()));
        actAnime.setLocation(attributes.getValue("location").trim());

        animeListEntries.add(actAnime);
    }


    private void createFilterEntry(final Attributes attributes) {
        final FilterListEntry entry = new FilterListEntry(attributes.getValue("title").trim(), attributes.getValue("thumbnail").trim(), new InfoLink(attributes.getValue("infoLink").trim()));
        filterListEntries.add(entry);
    }


    private void createWatchListEntry(final Attributes attributes) {
        final WatchListEntry entry = new WatchListEntry(attributes.getValue("title").trim(), attributes.getValue("thumbnail").trim(), new InfoLink(attributes.getValue("infoLink").trim()));
        watchListEntries.add(entry);
    }


    @Override
    public void characters(final char ch[], final int start, final int length) {
        strBuilder.append(new String(ch, start, length));
    }
}

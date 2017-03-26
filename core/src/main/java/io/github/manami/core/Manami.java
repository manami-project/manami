package io.github.manami.core;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.google.common.eventbus.EventBus;

import io.github.manami.cache.Cache;
import io.github.manami.core.commands.CommandService;
import io.github.manami.core.config.Config;
import io.github.manami.core.services.CacheInitializationService;
import io.github.manami.core.services.SearchService;
import io.github.manami.core.services.ServiceRepository;
import io.github.manami.core.services.ThumbnailBackloadService;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.persistence.ApplicationPersistence;
import io.github.manami.persistence.PersistenceFacade;
import io.github.manami.persistence.exporter.csv.CsvExporter;
import io.github.manami.persistence.exporter.json.JsonExporter;
import io.github.manami.persistence.exporter.xml.XmlExporter;
import io.github.manami.persistence.importer.csv.CsvImporter;
import io.github.manami.persistence.importer.json.JsonImporter;
import io.github.manami.persistence.importer.xml.XmlImporter;
import io.github.manami.persistence.importer.xml.XmlImporter.XmlStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * Main access to the features of the application. This class has got delegation
 * as well as operational functionality.
 *
 * @author manami-project
 * @since 1.0.0
 */
@Named
@Slf4j
public class Manami implements ApplicationPersistence {

    private static final String XMFILE_SUFFIX_XML = ".xml";
    private static final String FILE_SUFFIX_CSV = ".csv";
    private static final String FILE_SUFFIX_JSON = ".json";

    /** Instance of the command service. */
    private final CommandService cmdService;

    /** Instance of the application configuration. */
    private final Config config;

    /** Instance of the persistence Layer. */
    private final PersistenceFacade persistence;

    /** Instance of the service repository. */
    private final ServiceRepository serviceRepo;

    /** Instance of the cache. */
    private final Cache cache;

    /** Instance of the event bus. */
    final EventBus eventBus;


    /**
     * Constructor
     *
     * @since 2.3.0
     * @param cmdService
     *            Service for handling all commands.
     * @param config
     *            Config including the configurations paths and the currently
     *            opened file.
     * @param persistence
     *            Facade for DB operations.
     * @param serviceRepo
     *            service repository.
     */
    @Inject
    public Manami(final Cache cache, final CommandService cmdService, final Config config, final PersistenceFacade persistence, final ServiceRepository serviceRepo, final EventBus eventBus) {
        this.cache = cache;
        this.cmdService = cmdService;
        this.config = config;
        this.persistence = persistence;
        this.serviceRepo = serviceRepo;
        this.eventBus = eventBus;
    }


    /**
     * Creates a new empty List.
     *
     * @since 2.0.0
     */
    public void newList() {
        resetCommandHistory();
        config.setFile(null);
        persistence.clearAll();
    }


    /**
     * Clears the command stacks, the anime list and unsets the file.
     *
     * @since 2.0.0
     */
    private void resetCommandHistory() {
        cmdService.clearAll();
        cmdService.setUnsaved(false);
    }


    /**
     * Opens a file.
     *
     * @since 2.0.0
     * @param file
     *            File to open.
     */
    public void open(final Path file) throws SAXException, ParserConfigurationException, IOException {
        persistence.clearAll();
        new XmlImporter(XmlStrategy.MANAMI, persistence).importFile(file);
        config.setFile(file);
        serviceRepo.startService(new ThumbnailBackloadService(cache, persistence));
        serviceRepo.startService(new CacheInitializationService(cache, persistence.fetchAnimeList()));
    }


    /**
     * Exports the file.
     *
     * @since 2.0.0
     * @param file
     *            File to export to.
     */
    public void export(final Path file) {
        if (file.toString().endsWith(FILE_SUFFIX_CSV)) {
            new CsvExporter(persistence).exportAll(file);
        } else if (file.toString().endsWith(FILE_SUFFIX_JSON)) {
            new JsonExporter(persistence).exportAll(file);
        }
    }


    /**
     * Imports a file either XML (MAL List), JSON or CSV.
     *
     * @since 2.0.0
     * @param file
     *            File to import.
     */
    public void importFile(final Path file) {
        try {
            if (file.toString().endsWith(XMFILE_SUFFIX_XML)) {
                new XmlImporter(XmlStrategy.MAL, persistence).importFile(file);
            } else if (file.toString().endsWith(FILE_SUFFIX_CSV)) {
                new CsvImporter(persistence).importFile(file);
            } else if (file.toString().endsWith(FILE_SUFFIX_JSON)) {
                new JsonImporter(persistence).importFile(file);
            }
        } catch (SAXException | ParserConfigurationException | IOException e) {
            log.error("An error occurred trying to import {}:", file, e);
        }
    }


    /**
     * Saves the opened file.
     *
     * @since 2.0.0
     */
    public void save() {
        final XmlExporter xmlExporter = new XmlExporter(persistence);
        if (xmlExporter.exportAll(config.getFile())) {
            cmdService.setUnsaved(false);
            cmdService.resetDirtyFlag();
        }
    }


    @Override
    public boolean filterAnime(final MinimalEntry anime) {
        return persistence.filterAnime(anime);
    }


    @Override
    public List<FilterEntry> fetchFilterList() {
        return persistence.fetchFilterList();
    }


    @Override
    public boolean filterEntryExists(final InfoLink infoLink) {
        return persistence.filterEntryExists(infoLink);
    }


    @Override
    public boolean removeFromFilterList(final InfoLink infoLink) {
        return persistence.removeFromFilterList(infoLink);
    }


    @Override
    public boolean addAnime(final Anime anime) {
        return persistence.addAnime(anime);
    }


    /**
     * Does everything needed before exiting.
     *
     * @since 2.0.0
     */
    public void exit() {
        System.exit(0);
    }


    @Override
    public List<Anime> fetchAnimeList() {
        return persistence.fetchAnimeList();
    }


    @Override
    public boolean animeEntryExists(final InfoLink infoLink) {
        return persistence.animeEntryExists(infoLink);
    }


    @Override
    public List<WatchListEntry> fetchWatchList() {
        return persistence.fetchWatchList();
    }


    @Override
    public boolean watchListEntryExists(final InfoLink infoLink) {
        return persistence.watchListEntryExists(infoLink);
    }


    @Override
    public boolean watchAnime(final MinimalEntry anime) {
        return persistence.watchAnime(anime);
    }


    @Override
    public boolean removeFromWatchList(final InfoLink infoLink) {
        return persistence.removeFromWatchList(infoLink);
    }


    @Override
    public boolean removeAnime(final UUID id) {
        return persistence.removeAnime(id);
    }


    @Override
    public void updateOrCreate(final MinimalEntry entry) {
        persistence.updateOrCreate(entry);
    }


    /**
     * Searches for a specific string and fires an event with the search
     * results.
     *
     * @since 2.9.0
     * @param searchString
     */
    public void search(final String searchString) {
        if (isNotBlank(searchString)) {
            log.info("Initiated seach for [{}]", searchString);
            serviceRepo.startService(new SearchService(searchString, persistence, eventBus));
        }
    }


    /**
     * @since 2.10.0
     */
    public void exportList(final List<Anime> list, final Path file) {
        if (list == null || file == null) {
            throw new IllegalArgumentException("Either list or file to export to is null");
        }

        if (file.toString().endsWith(FILE_SUFFIX_JSON)) {
            new JsonExporter(persistence).exportList(list, file);
        }
    }
}

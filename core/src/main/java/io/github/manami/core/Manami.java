package io.github.manami.core;

import io.github.manami.cache.Cache;
import io.github.manami.core.commands.CommandService;
import io.github.manami.core.config.Config;
import io.github.manami.core.services.CacheInitializationService;
import io.github.manami.core.services.ServiceRepository;
import io.github.manami.core.services.ThumbnailBackloadService;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Main access to the features of the application. This class has got delegation
 * as well as operational functionality.
 *
 * @author manami-project
 * @since 1.0.0
 */
@Named
public class Manami implements ApplicationPersistence {

    /** Logger. */
    private final static Logger LOG = LoggerFactory.getLogger(Manami.class);

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
    public Manami(final Cache cache, final CommandService cmdService, final Config config, final PersistenceFacade persistence, final ServiceRepository serviceRepo) {
        this.cache = cache;
        this.cmdService = cmdService;
        this.config = config;
        this.persistence = persistence;
        this.serviceRepo = serviceRepo;
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
        if (file.toString().endsWith(".csv")) {
            new CsvExporter(persistence).exportList(file);
        } else if (file.toString().endsWith(".json")) {
            new JsonExporter(persistence).exportList(file);
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
            if (file.toString().endsWith(".xml")) {
                new XmlImporter(XmlStrategy.MAL, persistence).importFile(file);
            } else if (file.toString().endsWith(".csv")) {
                new CsvImporter(persistence).importFile(file);
            } else if (file.toString().endsWith(".json")) {
                new JsonImporter(persistence).importFile(file);
            }
        } catch (SAXException | ParserConfigurationException | IOException e) {
            LOG.error("An error occurred trying to import {}:", file, e);
        }
    }


    /**
     * Saves the opened file.
     *
     * @since 2.0.0
     */
    public void save() {
        final XmlExporter xmlExporter = new XmlExporter(config.getConfigFilesPath(), persistence);
        if (xmlExporter.exportList(config.getFile())) {
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
    public boolean filterEntryExists(final String url) {
        return persistence.filterEntryExists(url);
    }


    @Override
    public boolean removeFromFilterList(final String url) {
        return persistence.removeFromFilterList(url);
    }


    @Override
    public boolean addAnime(final Anime anime) {
        if (anime != null) {
            persistence.removeFromFilterList(anime.getInfoLink());
            persistence.removeFromWatchList(anime.getInfoLink());
            return persistence.addAnime(anime);
        }

        return false;
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
    public boolean animeEntryExists(final String url) {
        return persistence.animeEntryExists(url);
    }


    @Override
    public List<WatchListEntry> fetchWatchList() {
        return persistence.fetchWatchList();
    }


    @Override
    public boolean watchListEntryExists(final String url) {
        return persistence.watchListEntryExists(url);
    }


    @Override
    public boolean watchAnime(final MinimalEntry anime) {
        return persistence.watchAnime(anime);
    }


    @Override
    public boolean removeFromWatchList(final String url) {
        return persistence.removeFromWatchList(url);
    }


    @Override
    public boolean removeAnime(final UUID id) {
        return persistence.removeAnime(id);
    }


    @Override
    public void updateOrCreate(final Anime anime) {
        persistence.updateOrCreate(anime);
    }


    @Override
    public void updateOrCreate(final WatchListEntry entry) {
        persistence.updateOrCreate(entry);
    }


    @Override
    public void updateOrCreate(final FilterEntry entry) {
        persistence.updateOrCreate(entry);
    }


    @Override
    public void updateOrCreate(final MinimalEntry entry) {
        persistence.updateOrCreate(entry);
    }
}

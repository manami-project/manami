package io.github.manami.persistence.exporter.csv;

import com.google.common.collect.Lists;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.persistence.ApplicationPersistence;
import io.github.manami.persistence.exporter.Exporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Exports a list to a csv file.
 *
 * @author manami project
 * @since 2.0.0
 */
public class CsvExporter implements Exporter {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(CsvExporter.class);

    /** Contains configurations for reading and writing csv files. */
    private final CsvConfig config;

    private final ApplicationPersistence persistence;

    private ICsvListWriter listWriter;


    /**
     * Constructor
     *
     * @since 2.0.0
     */
    public CsvExporter(final ApplicationPersistence persistence) {
        this.persistence = persistence;
        config = new CsvConfig();
    }


    @Override
    public boolean exportList(final Path file) {

        try {
            listWriter = new CsvListWriter(new FileWriter(file.toFile()), CsvPreference.STANDARD_PREFERENCE);

            // write the header
            listWriter.writeHeader(config.getHeaders());

            writeAnimeList();
            writeWatchList();
            writeFilterList();

            listWriter.close();
        } catch (final IOException e) {
            LOG.error("An error occurred while trying to export the list to CSV: ", e);
            return false;
        }

        return true;
    }


    /**
     * @since 2.7.0
     * @throws IOException
     */
    private void writeAnimeList() throws IOException {
        final List<List<String>> mappedEntryList = Lists.newArrayList();
        List<String> curEntry;

        // Map Anime Objects to a list
        for (final Anime entry : persistence.fetchAnimeList()) {
            curEntry = Lists.newArrayList();
            curEntry.add(CsvConfig.ANIMELIST);
            curEntry.add(entry.getTitle());
            curEntry.add(entry.getTypeAsString());
            curEntry.add(String.valueOf(entry.getEpisodes()));
            curEntry.add(entry.getInfoLink());
            curEntry.add(entry.getLocation());
            mappedEntryList.add(curEntry);
        }

        // write the animeList
        for (final List<String> entry : mappedEntryList) {
            listWriter.write(entry, config.getProcessors());
        }
    }


    /**
     * @since 2.7.0
     */
    private void writeWatchList() throws IOException {
        final List<List<String>> mappedEntryList = Lists.newArrayList();
        List<String> curEntry;

        // Map Anime Objects to a list
        for (final WatchListEntry entry : persistence.fetchWatchList()) {
            curEntry = Lists.newArrayList();
            curEntry.add(CsvConfig.WATCHLIST);
            curEntry.add(entry.getTitle());
            curEntry.add("");
            curEntry.add("");
            curEntry.add(entry.getInfoLink());
            curEntry.add("");
            mappedEntryList.add(curEntry);
        }

        // write the watchList
        for (final List<String> entry : mappedEntryList) {
            listWriter.write(entry, config.getProcessors());
        }
    }


    /**
     * @since 2.7.0
     */
    private void writeFilterList() throws IOException {
        final List<List<String>> mappedEntryList = Lists.newArrayList();
        List<String> curEntry;

        // Map Anime Objects to a list
        for (final FilterEntry entry : persistence.fetchFilterList()) {
            curEntry = Lists.newArrayList();
            curEntry.add(CsvConfig.FILTERLIST);
            curEntry.add(entry.getTitle());
            curEntry.add("");
            curEntry.add("");
            curEntry.add(entry.getInfoLink());
            curEntry.add("");
            mappedEntryList.add(curEntry);
        }

        // write the filterList
        for (final List<String> entry : mappedEntryList) {
            listWriter.write(entry, config.getProcessors());
        }
    }
}
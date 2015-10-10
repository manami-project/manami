package io.github.manami.persistence.importer.csv;

import com.google.common.collect.Lists;
import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.persistence.PersistenceFacade;
import io.github.manami.persistence.exporter.csv.CsvConfig;
import io.github.manami.persistence.importer.Importer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * @author manami-project
 * @since 2.0.0
 */
public class CsvImporter implements Importer {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(CsvImporter.class);

    /** Configuration for CSV files. */
    private final CsvConfig csvConfig;

    private final PersistenceFacade persistence;
    private final List<Anime> animeListEntries;
    private final List<FilterEntry> filterListEntries;
    private final List<WatchListEntry> watchListEntries;


    /**
     * Constructor
     *
     * @since 2.0.0
     */
    public CsvImporter(final PersistenceFacade persistence) {
        this.persistence = persistence;
        csvConfig = new CsvConfig();
        animeListEntries = Lists.newArrayList();
        filterListEntries = Lists.newArrayList();
        watchListEntries = Lists.newArrayList();
    }


    @Override
    public void importFile(final Path file) {
        final CellProcessor[] processors = csvConfig.getProcessors();
        List<Object> objectList;

        try (ICsvListReader listReader = new CsvListReader(new FileReader(file.toFile()), CsvPreference.STANDARD_PREFERENCE)) {
            listReader.getHeader(true);
            while ((objectList = listReader.read(processors)) != null) {
                // get all columns
                String title = (String) objectList.get(1);
                final String typeStr = (String) objectList.get(2);
                final String episodesStr = (String) objectList.get(3);
                String infoLink = (String) objectList.get(4);
                String location = (String) objectList.get(5);

                // prepare attributes
                title = StringUtils.isNotBlank(title) ? title.trim() : null;
                final AnimeType type = StringUtils.isNotBlank(typeStr) ? AnimeType.findByName(typeStr.trim()) : null;
                final int episodes = StringUtils.isNotBlank(episodesStr) && StringUtils.isNumeric(episodesStr) ? Integer.parseInt(episodesStr) : 0;
                infoLink = StringUtils.isNotBlank(infoLink) ? infoLink.trim() : null;
                location = StringUtils.isNotBlank(location) ? location.trim() : null;

                // create object by list type
                switch ((String) objectList.get(0)) {
                    case CsvConfig.ANIMELIST:
                        animeListEntries.add(new Anime(title, type, episodes, infoLink, location));
                        break;
                    case CsvConfig.WATCHLIST:
                        watchListEntries.add(new WatchListEntry(title, infoLink));
                        break;
                    case CsvConfig.FILTERLIST:
                        filterListEntries.add(new FilterEntry(title, infoLink));
                        break;

                    default:
                        break;
                }

            }

            persistence.addAnimeList(animeListEntries);
            persistence.addFilterList(filterListEntries);
            persistence.addWatchList(watchListEntries);
        } catch (final IOException e) {
            LOG.error("An error occurred trying to import the CSV file: ", e);
        }
    }
}

package io.github.manami.persistence.importer.csv;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterListEntry;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.persistence.PersistenceFacade;
import io.github.manami.persistence.exporter.csv.CsvConfig;
import io.github.manami.persistence.exporter.csv.CsvConfig.CsvConfigType;
import io.github.manami.persistence.importer.Importer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CsvImporter implements Importer {

    /** Configuration for CSV files. */
    private final CsvConfig csvConfig;

    private final PersistenceFacade persistence;
    private final List<Anime> animeListEntries;
    private final List<FilterListEntry> filterListEntries;
    private final List<WatchListEntry> watchListEntries;


    public CsvImporter(final PersistenceFacade persistence) {
        this.persistence = persistence;
        csvConfig = new CsvConfig();
        animeListEntries = newArrayList();
        filterListEntries = newArrayList();
        watchListEntries = newArrayList();
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
                String infoLinkStr = (String) objectList.get(4);
                String location = (String) objectList.get(5);

                // prepare attributes
                title = isNotBlank(title) ? title.trim() : null;
                final AnimeType type = isNotBlank(typeStr) ? AnimeType.findByName(typeStr.trim()) : null;
                final int episodes = isNotBlank(episodesStr) && isNumeric(episodesStr) ? Integer.parseInt(episodesStr) : 0;
                infoLinkStr = infoLinkStr != null ? infoLinkStr.trim() : infoLinkStr;
                location = isNotBlank(location) ? location.trim() : null;

                final InfoLink infoLink = new InfoLink(infoLinkStr);

                // create object by list type
                final CsvConfigType csvConfigType = CsvConfigType.findByName((String) objectList.get(0));
                switch (csvConfigType) {
                    case ANIMELIST:
                        animeListEntries.add(new Anime(title, infoLink).type(type).episodes(episodes).location(location));
                        break;
                    case WATCHLIST:
                        watchListEntries.add(new WatchListEntry(title, infoLink));
                        break;
                    case FILTERLIST:
                        filterListEntries.add(new FilterListEntry(title, infoLink));
                        break;

                    default:
                        break;
                }

            }

            persistence.addAnimeList(animeListEntries);
            persistence.addFilterList(filterListEntries);
            persistence.addWatchList(watchListEntries);
        } catch (final IOException e) {
            log.error("An error occurred trying to import the CSV file: ", e);
        }
    }
}

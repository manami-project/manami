package io.github.manami.persistence.exporter.csv;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Configuration for the CSV import and export.
 *
 * @author manami-project
 * @since 2.0.0
 */
public class CsvConfig {

    @AllArgsConstructor
    public enum CsvConfigType {
        ANIMELIST("animeList"), WATCHLIST("watchList"), FILTERLIST("filterList");

        @Getter
        private final String value;


        public static CsvConfigType findByName(final String name) {
            for (final CsvConfigType type : values()) {
                if (type.getValue().equalsIgnoreCase((name))) {
                    return type;
                }
            }
            return null;
        }
    }


    /**
     * Returns the names of the columns.
     *
     * @since 2.0.0
     * @return A String with the names of the columns for the csv file.
     */
    public String[] getHeaders() {
        return new String[] { "list", "title", "type", "episodes", "infolink", "location" };
    }


    /**
     * Type of Processors.
     *
     * @since 2.0.0
     * @return An array with indication of the column's type.
     */
    public CellProcessor[] getProcessors() {
        return new CellProcessor[] { new NotNull(), // List (e.g. animeList,
                                                    // filterList, watchList)
                new NotNull(), // Title
                new Optional(), // Type
                new Optional(), // Episodes
                new Optional(), // InfoLink
                new Optional(), // Location
        };
    }
}

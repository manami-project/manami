package io.github.manami.persistence.exporter;

import java.nio.file.Path;

/**
 * Interface for an exporter.
 */
public interface Exporter {

    /**
     * Exports a list to a specified file.
     *
     * @param file File
     * @return true if the export was successful and false if an error occurred.
     */
    boolean exportAll(final Path file);
}

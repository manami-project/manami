package io.github.manami.persistence.exporter;

import java.nio.file.Path;

/**
 * Interface for an exporter.
 *
 * @author manami-project
 * @version 2.0.0
 */
public interface Exporter {

    /**
     * Exports a list to a specified file.
     *
     * @since 2.7.0
     * @param file
     *            File
     * @return true if the export was successful and false if an error occurred.
     */
    boolean exportList(final Path file);
}

package io.github.manami.persistence.importer;

import java.io.IOException;
import java.nio.file.Path;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * Interface for an importer.
 *
 * @author manami project
 * @since 2.0.0
 */
public interface Importer {

    /**
     * Imports a list from a file and enriches the given list.
     *
     * @since 2.0.0
     * @param file
     *            File
     */
    void importFile(Path file) throws SAXException, ParserConfigurationException, IOException;
}

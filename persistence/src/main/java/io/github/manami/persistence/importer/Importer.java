package io.github.manami.persistence.importer;

import java.io.IOException;
import java.nio.file.Path;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * Interface for an importer.
 */
public interface Importer {

    /**
     * Imports a list from a file and enriches the given list.
     */
    void importFile(Path file) throws SAXException, ParserConfigurationException, IOException;
}

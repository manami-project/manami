package io.github.manami.persistence.importer.xml;

import java.io.IOException;
import java.nio.file.Path;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import io.github.manami.persistence.PersistenceFacade;
import io.github.manami.persistence.importer.Importer;
import io.github.manami.persistence.importer.xml.parser.MalSaxParser;
import io.github.manami.persistence.importer.xml.parser.ManamiSaxParser;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Importer for opening xml files which are specific for this application.
 *
 * @author manami-project
 * @since 2.0.0
 */
public class XmlImporter implements Importer {

    /** Strategy being used, */
    private final XmlStrategy strategy;

    private final PersistenceFacade persistence;

    /**
     * Strategy for XML import.
     *
     * @author manami-project
     * @since 2.0.0
     */
    public enum XmlStrategy {
        /**
         * Strategy for opening manami files.
         */
        MANAMI,
        /**
         * Strategy for myanimelist.net files.
         */
        MAL
    }


    /**
     * @since 2.0.0
     * @param strategy
     *            Strategy
     */
    public XmlImporter(final XmlStrategy strategy, final PersistenceFacade persistence) {
        this.strategy = strategy;
        this.persistence = persistence;
    }


    @Override
    public void importFile(final Path file) throws SAXException, ParserConfigurationException, IOException {
        if (strategy != null) {
            final SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setValidating(true);
            parserFactory.setNamespaceAware(true);

            final XMLReader xmlReader = parserFactory.newSAXParser().getXMLReader();
            xmlReader.setContentHandler(createParserFromStrategy());
            xmlReader.parse(new InputSource(file.toAbsolutePath().toString()));
        }
    }


    /**
     * Creates a parser from the given strategy.
     *
     * @since 2.0.0
     * @return Parser
     */
    private ContentHandler createParserFromStrategy() {
        ContentHandler ret = null;

        switch (strategy) {
            case MANAMI:
                ret = new ManamiSaxParser(persistence);
                break;
            case MAL:
                ret = new MalSaxParser(persistence);
                break;
        }

        return ret;
    }
}

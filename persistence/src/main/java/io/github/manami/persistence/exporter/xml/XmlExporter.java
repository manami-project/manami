package io.github.manami.persistence.exporter.xml;

import io.github.manami.dto.ToolVersion;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.persistence.ApplicationPersistence;
import io.github.manami.persistence.exporter.Exporter;
import io.github.manami.persistence.utility.PathResolver;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author manami-project
 * @since 2.0.0
 */
public class XmlExporter implements Exporter {

    private static final String RELATIVE_PATH_SEPARATOR = "/";

    /** Logger */
    private final static Logger LOG = LoggerFactory.getLogger(XmlExporter.class);

    /** Document. */
    private Document doc = null;

    /** File to save to. */
    private Path file;

    private final ApplicationPersistence persistence;


    /**
     * Constructor.
     *
     * @since 2.0.0
     * @param configFilesPath
     *            Config for the application.
     */
    public XmlExporter(final ApplicationPersistence persistence) {
        this.persistence = persistence;
    }


    @Override
    public boolean exportAll(final Path file) {
        this.file = file;
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        DocumentBuilder builder;

        try {
            builder = factory.newDocumentBuilder();

            if (Files.notExists(file)) {
                try {
                    Files.createFile(file);
                } catch (final IOException e) {
                    LOG.error("Could not create XML file {}", file.getFileName(), e);
                }
            }
            doc = builder.newDocument();

            // add doctype
            final DOMImplementation domImpl = doc.getImplementation();
            final DocumentType doctype = domImpl.createDocumentType("animeList", "SYSTEM", createDtdPath());
            doc.appendChild(doctype);
        } catch (final ParserConfigurationException e) {
            LOG.error("An error occurred while trying to initialize the dom tree: ", e);
            return false;
        }

        createDomTree();
        prettyPrintXML2File();
        return true;
    }


    /**
     * @since 2.10.0
     * @return
     */
    private String createConfigFilePath() {
        String appDir = XmlExporter.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        int endOfString = appDir.lastIndexOf(RELATIVE_PATH_SEPARATOR);

        if (endOfString == -1) {
            endOfString = appDir.lastIndexOf("\\");
        }

        appDir = appDir.substring(0, endOfString);

        if (appDir.startsWith(RELATIVE_PATH_SEPARATOR)) {
            appDir = appDir.substring(1);
        }

        return PathResolver.buildRelativizedPath(appDir, file.getParent());
    }


    /**
     * @since 2.10.0
     * @return
     */
    private String createDtdPath() {
        String relativeConfigPath = createConfigFilePath();

        if (!relativeConfigPath.endsWith(RELATIVE_PATH_SEPARATOR)) {
            relativeConfigPath = relativeConfigPath.concat(RELATIVE_PATH_SEPARATOR);
        }

        return relativeConfigPath.concat("config/animelist.dtd");
    }


    /**
     * @since 2.10.0
     * @return
     */
    private String createXsltPath() {
        String relativeConfigPath = createConfigFilePath();

        if (!relativeConfigPath.endsWith(RELATIVE_PATH_SEPARATOR)) {
            relativeConfigPath = relativeConfigPath.concat(RELATIVE_PATH_SEPARATOR);
        }

        return relativeConfigPath.concat("config/theme/animelist_transform.xsl");
    }


    /**
     * Method to create the dom tree.
     *
     * @since 2.0.0
     */
    private void createDomTree() {
        final Element root = createRootElement();
        createAnimeList(root);
        createWatchList(root);
        createFilterList(root);
    }


    /**
     * @since 2.7.0
     */
    private Element createRootElement() {
        final Element root = doc.createElement("manami");
        root.setAttribute("version", ToolVersion.getVersion());
        doc.appendChild(root);

        // create transformation and css information
        final Node xslt = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xml\" href=\"" + createXsltPath() + "\"");
        doc.insertBefore(xslt, root);

        return root;
    }


    /**
     * @since 2.7.0
     */
    private void createAnimeList(final Element parent) {
        final Element elementAnimeList = doc.createElement("animeList");
        Element actAnime;

        for (final Anime anime : persistence.fetchAnimeList()) {
            // element "anime"
            actAnime = doc.createElement("anime");

            // attribute "title"
            actAnime.setAttribute("title", anime.getTitle());

            // attribute "type"
            actAnime.setAttribute("type", anime.getTypeAsString());

            // attribute "episodes"
            actAnime.setAttribute("episodes", String.valueOf(anime.getEpisodes()));

            // attribute "infolink"
            actAnime.setAttribute("infoLink", anime.getInfoLink());

            // attribute "location"
            actAnime.setAttribute("location", anime.getLocation());

            // append to animeList
            elementAnimeList.appendChild(actAnime);
        }

        parent.appendChild(elementAnimeList);
    }


    /**
     * @since 2.7.0
     */
    private void createWatchList(final Element parent) {
        final Element elementWatchList = doc.createElement("watchList");
        Element actEntry;

        for (final WatchListEntry anime : persistence.fetchWatchList()) {
            // element "anime"
            actEntry = doc.createElement("watchListEntry");

            // attribute "title"
            actEntry.setAttribute("title", anime.getTitle());

            actEntry.setAttribute("thumbnail", anime.getThumbnail());

            // attribute "infolink"
            actEntry.setAttribute("infoLink", anime.getInfoLink());

            // append to animeList
            elementWatchList.appendChild(actEntry);
        }

        parent.appendChild(elementWatchList);
    }


    /**
     * @since 2.7.0
     */
    private void createFilterList(final Element parent) {
        final Element elementFilterList = doc.createElement("filterList");
        Element actEntry;

        for (final FilterEntry anime : persistence.fetchFilterList()) {
            // element "anime"
            actEntry = doc.createElement("filterEntry");

            // attribute "title"
            actEntry.setAttribute("title", anime.getTitle());

            actEntry.setAttribute("thumbnail", anime.getThumbnail());

            // attribute "infolink"
            actEntry.setAttribute("infoLink", anime.getInfoLink());

            // append to animeList
            elementFilterList.appendChild(actEntry);
        }

        parent.appendChild(elementFilterList);
    }


    /**
     * Write the tree into the file and save it.
     *
     * @since 2.0.0
     */
    private void prettyPrintXML2File() {
        final TransformerFactory tfactory = TransformerFactory.newInstance();
        Transformer transformer;
        PrintWriter output;

        try {
            transformer = tfactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            if (doc.getDoctype() != null) {
                transformer.setOutputProperty("doctype-system", doc.getDoctype().getSystemId());
            }

            output = new PrintWriter(file.toFile(), "UTF-8");
            transformer.transform(new DOMSource(doc), new StreamResult(output));
            output.close();

        } catch (TransformerException | IOException e) {
            LOG.error("An error occurred while trying to export the list to a xml file: ", e);
        }
    }
}

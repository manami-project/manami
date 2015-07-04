package io.github.manami.persistence.exporter.json;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;

import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.persistence.ApplicationPersistence;
import io.github.manami.persistence.exporter.Exporter;

import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exports a list to valid json.
 *
 * @author manami project
 * @since 2.0.0
 */
public class JsonExporter implements Exporter {

    /** Logger. */
    private final static Logger LOG = LoggerFactory.getLogger(JsonExporter.class);
    private final ApplicationPersistence persistence;


    public JsonExporter(final ApplicationPersistence persistence) {
        this.persistence = persistence;
    }


    @Override
    public boolean exportList(final Path file) {
        try (final PrintWriter printWriter = new PrintWriter(file.toFile())) {
            final JSONWriter writer = new JSONWriter(printWriter);
            writer.array();
            writer.array();

            for (final Anime element : persistence.fetchAnimeList()) {
                writer.object().key("title").value(element.getTitle()).key("type").value(element.getTypeAsString()).key("episodes").value(element.getEpisodes()).key("infoLink")
                        .value(element.getInfoLink()).key("location").value(element.getLocation()).endObject();
            }
            writer.endArray();
            writer.array();

            for (final WatchListEntry element : persistence.fetchWatchList()) {
                writer.object().key("thumbnail").value(element.getThumbnail()).key("title").value(element.getTitle()).key("infoLink").value(element.getInfoLink()).endObject();
            }

            writer.endArray();
            writer.array();

            for (final FilterEntry element : persistence.fetchFilterList()) {
                writer.object().key("thumbnail").value(element.getThumbnail()).key("title").value(element.getTitle()).key("infoLink").value(element.getInfoLink()).endObject();
            }

            writer.endArray();
            writer.endArray();
            printWriter.flush();
        } catch (final FileNotFoundException e) {
            LOG.error("An error occurred while trying to export the list to JSON: ", e);
            return false;
        }

        return true;
    }
}

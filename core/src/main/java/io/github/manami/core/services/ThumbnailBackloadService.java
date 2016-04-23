package io.github.manami.core.services;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.util.Assert.notNull;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.manami.cache.Cache;
import io.github.manami.dto.entities.AbstractMinimalEntry;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.persistence.PersistenceFacade;

/**
 * @author manami-project
 * @since 2.8.2
 */
public class ThumbnailBackloadService extends AbstractService<Void> {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ThumbnailBackloadService.class);
    private static final int OK = 200;
    private static final int NOT_FOUND = 404;
    private final PersistenceFacade persistence;
    private final Cache cache;
    private CloseableHttpClient httpclient;


    /**
     * Constructor awaiting a cache.
     *
     * @since 2.8.2
     * @param cache
     *            Cache
     */
    public ThumbnailBackloadService(final Cache cache, final PersistenceFacade persistence) {
        this.cache = cache;
        this.persistence = persistence;
    }


    /**
     * @since 2.8.2
     */
    @Override
    protected Void execute() {
        notNull(cache, "Cache cannot be null");
        notNull(persistence, "List of animes cannot be null");

        httpclient = HttpClients.createDefault();

        persistence.fetchFilterList().forEach(this::loadThumbnailIfNotExists);
        persistence.fetchWatchList().forEach(this::loadThumbnailIfNotExists);

        persistence.fetchFilterList().forEach(this::checkPictures);
        persistence.fetchWatchList().forEach(this::checkPictures);

        try {
            httpclient.close();
        } catch (final IOException e) {
            LOG.error("An error occured while trying to close http client: ", e);
        }

        return null;
    }


    @Override
    public void reset() {
        cancel();
        super.reset();
    }


    /**
     * @since 2.9.0
     * @param entry
     */
    private void loadThumbnailIfNotExists(final MinimalEntry entry) {
        if (isInterrupt()) {
            return;
        }

        if (entry != null && (isBlank(entry.getThumbnail()) || AbstractMinimalEntry.NO_IMG_THUMB.equals(entry.getThumbnail()))) {
            final Anime cachedAnime = cache.fetchAnime(entry.getInfoLink());
            LOG.debug("Loading thumbnail for entry {}", entry.getInfoLink());
            if (cachedAnime != null) {
                updateThumbnail(entry, cachedAnime);
            }
        }
    }


    /**
     * @since 2.9.0
     * @param entry
     * @param cachedAnime
     */
    private void updateThumbnail(final MinimalEntry entry, final Anime cachedAnime) {
        MinimalEntry updatedEntry = null;

        if (entry instanceof FilterEntry) {
            updatedEntry = new FilterEntry(entry.getTitle(), cachedAnime.getThumbnail(), entry.getInfoLink());
        } else if (entry instanceof WatchListEntry) {
            updatedEntry = new WatchListEntry(entry.getTitle(), cachedAnime.getThumbnail(), entry.getInfoLink());
        }

        persistence.updateOrCreate(updatedEntry);
    }


    /**
     * @since 2.9.0
     * @param entry
     */
    private void checkPictures(final MinimalEntry entry) {
        if (isInterrupt()) {
            return;
        }

        if (entry != null && isNotBlank(entry.getThumbnail())) {
            LOG.debug("Checking thumbnail for {}", entry.getInfoLink());

            int responseCodeThumbnail = NOT_FOUND;

            try {
                final CloseableHttpResponse response = httpclient.execute(RequestBuilder.head(entry.getThumbnail()).build());
                responseCodeThumbnail = response.getStatusLine().getStatusCode();
            } catch (final IOException e) {
                LOG.error("Could not retrieve picture link status: ", e);
            }

            if (responseCodeThumbnail != OK) {
                final Anime updatedAnime = cache.fetchAnime(entry.getInfoLink());
                if (updatedAnime != null) {
                    LOG.debug("Updating thumbnail for [{}]", entry.getInfoLink());
                    updateThumbnail(entry, updatedAnime);
                }
            }
        }
    }
}

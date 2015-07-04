package io.github.manami.core.services;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import io.github.manami.cache.Cache;
import io.github.manami.dto.entities.AbstractMinimalEntry;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.persistence.PersistenceFacade;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * @author manami project
 * @since 2.8.2
 */
public class BackloadService extends AbstractService<Void> {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(BackloadService.class);
    private final PersistenceFacade persistence;
    private final Cache cache;


    /**
     * Constructor awaiting a cache.
     *
     * @since 2.8.2
     * @param cache
     *            Cache
     */
    public BackloadService(final Cache cache, final PersistenceFacade persistence) {
        this.cache = cache;
        this.persistence = persistence;
    }


    @Override
    public void start() {
        Assert.notNull(persistence, "List of animes cannot be null");

        reset();

        service = new Service<Void>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        backloadThumbnails();
                        return null;
                    }
                };
            }
        };

        service.setOnCancelled(getFailureEvent());
        service.setOnFailed(getFailureEvent());
        service.setOnSucceeded(getSuccessEvent());
        service.start();
    }


    /**
     * @since 2.8.2
     */
    private void backloadThumbnails() {
        // watch list
        for (final WatchListEntry entry : persistence.fetchWatchList()) {
            if (StringUtils.isBlank(entry.getThumbnail()) || AbstractMinimalEntry.NO_IMG_THUMB.equals(entry.getThumbnail())) {
                final Anime cachedAnime = cache.fetchAnime(entry.getInfoLink());
                LOG.debug("Loading thumbnail for watch list entry {}", entry.getInfoLink());
                if (cachedAnime != null) {
                    persistence.removeFromWatchList(entry.getInfoLink());
                    persistence.watchAnime(cachedAnime);
                }
            }
        }

        // filter list
        for (final FilterEntry entry : persistence.fetchFilterList()) {
            if (StringUtils.isBlank(entry.getThumbnail()) || AbstractMinimalEntry.NO_IMG_THUMB.equals(entry.getThumbnail())) {
                final Anime cachedAnime = cache.fetchAnime(entry.getInfoLink());
                LOG.debug("Loading thumbnail for filter list entry {}", entry.getInfoLink());
                if (cachedAnime != null) {
                    persistence.removeFromFilterList(entry.getInfoLink());
                    persistence.filterAnime(cachedAnime);
                }
            }
        }
    }


    @Override
    public void reset() {
        cancel();
        super.reset();
    }
}

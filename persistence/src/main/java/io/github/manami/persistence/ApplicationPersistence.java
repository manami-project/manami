package io.github.manami.persistence;

import io.github.manami.dto.entities.MinimalEntry;

/**
 * @author manami-project
 * @since 2.2.0
 */
public interface ApplicationPersistence extends AnimeListHandler, WatchListHandler, FilterListHandler {

    void updateOrCreate(final MinimalEntry entry);
}

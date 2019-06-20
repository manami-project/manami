package io.github.manami.persistence;

import io.github.manami.dto.entities.MinimalEntry;

public interface ApplicationPersistence extends AnimeListHandler, WatchListHandler, FilterListHandler {

    void updateOrCreate(final MinimalEntry entry);
}

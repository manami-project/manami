package io.github.manami.persistence;

import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.dto.entities.WatchListEntry;

import java.util.List;

/**
 * @author manami project
 * @since 2.7.0
 */
public interface WatchListHandler {

    List<WatchListEntry> fetchWatchList();


    boolean watchListEntryExists(String url);


    boolean watchAnime(MinimalEntry anime);


    boolean removeFromWatchList(String url);


    /**
     * @since 2.9.0
     * @param entry
     *            Entry which is supposed to be updated or created.
     */
    void updateOrCreate(WatchListEntry entry);
}

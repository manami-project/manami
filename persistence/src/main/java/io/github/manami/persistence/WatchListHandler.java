package io.github.manami.persistence;

import java.util.List;

import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.dto.entities.WatchListEntry;

/**
 * @author manami project
 * @since 2.7.0
 */
public interface WatchListHandler {

    List<WatchListEntry> fetchWatchList();


    boolean watchListEntryExists(String url);


    boolean watchAnime(MinimalEntry anime);


    boolean removeFromWatchList(String url);
}

package io.github.manami.persistence;

import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.dto.entities.WatchListEntry;

import java.util.List;

public interface WatchListHandler {

    List<WatchListEntry> fetchWatchList();


    boolean watchListEntryExists(InfoLink infoLink);


    boolean watchAnime(MinimalEntry anime);


    boolean removeFromWatchList(InfoLink infoLink);
}

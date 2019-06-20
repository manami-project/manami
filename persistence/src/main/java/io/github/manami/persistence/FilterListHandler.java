package io.github.manami.persistence;

import java.util.List;

import io.github.manami.dto.entities.FilterListEntry;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.MinimalEntry;

public interface FilterListHandler {

    /**
     * Adds a URL to the filter list.
     */
    boolean filterAnime(MinimalEntry anime);


    /**
     * Retrieves the persisted filter list.
     * 
     * @return List of anime which have been filtered.
     */
    List<FilterListEntry> fetchFilterList();


    /**
     * Checks whether an anime is already in your filter list.
     * 
     * @param infoLink URL
     * @return true if the URL is in the filter list.
     */
    boolean filterEntryExists(InfoLink infoLink);


    /**
     * Removes an entry from the filter list.
     * 
     * @param infoLink URL
     */
    boolean removeFromFilterList(InfoLink infoLink);
}

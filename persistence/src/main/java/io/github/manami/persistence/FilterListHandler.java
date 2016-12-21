package io.github.manami.persistence;

import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.MinimalEntry;

import java.util.List;

/**
 * @author manami-project
 * @since 2.7.0
 */
public interface FilterListHandler {

    /**
     * Adds a URL to the filter list.
     * @since 2.1.0
     * @param anime
     * Anime
     */
    boolean filterAnime(MinimalEntry anime);


    /**
     * Retrieves the persisted filter list.
     * @since 2.1.0
     * @return List of animes which have been filtered.
     */
    List<FilterEntry> fetchFilterList();


    /**
     * Checks whether an anime is already in your filter list.
     * @since 2.1.0
     * @param infoLink
     * URL
     * @return true if the URL is in the filter list.
     */
    boolean filterEntryExists(InfoLink infoLink);


    /**
     * Removes an entry from the filter list.
     * @since 2.1.0
     * @param infoLink
     * URL
     */
    boolean removeFromFilterList(InfoLink infoLink);
}

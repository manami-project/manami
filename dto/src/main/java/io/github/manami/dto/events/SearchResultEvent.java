package io.github.manami.dto.events;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import io.github.manami.dto.entities.MinimalEntry;

/**
 * Contains a {@link List} of {@link MinimalEntry} for each list type.
 *
 * @author manami-project
 * @since 2.9.0
 */
public class SearchResultEvent {

    private final List<MinimalEntry> animeListSearchResultList;
    private final List<MinimalEntry> filterListSearchResultList;
    private final List<MinimalEntry> watchListSearchResultList;


    /**
     * @since 2.9.0
     */
    public SearchResultEvent() {
        animeListSearchResultList = newArrayList();
        filterListSearchResultList = newArrayList();
        watchListSearchResultList = newArrayList();
    }


    /**
     * @since 2.9.0
     * @return The list containing search results from anime list.
     */
    public List<MinimalEntry> getAnimeListSearchResultList() {
        return animeListSearchResultList;
    }


    /**
     * @since 2.9.0
     * @return The list containing search results from filter list.
     */
    public List<MinimalEntry> getFilterListSearchResultList() {
        return filterListSearchResultList;
    }


    /**
     * @since 2.9.0
     * @return The list containing search results from watch list.
     */
    public List<MinimalEntry> getWatchListSearchResultList() {
        return watchListSearchResultList;
    }
}

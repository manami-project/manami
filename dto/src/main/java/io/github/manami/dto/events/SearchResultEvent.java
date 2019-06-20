package io.github.manami.dto.events;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.google.common.collect.ImmutableList;

import io.github.manami.dto.entities.MinimalEntry;

/**
 * Contains a {@link List} of {@link MinimalEntry} for each list type.
 */
public class SearchResultEvent {

    private final String searchString;
    private final List<MinimalEntry> animeListSearchResultList;
    private final List<MinimalEntry> filterListSearchResultList;
    private final List<MinimalEntry> watchListSearchResultList;


    public SearchResultEvent(final String searchString) {
        this.searchString = searchString;
        animeListSearchResultList = newArrayList();
        filterListSearchResultList = newArrayList();
        watchListSearchResultList = newArrayList();
    }


    /**
     * @return The list containing search results from anime list.
     */
    public List<MinimalEntry> getAnimeListSearchResultList() {
        return ImmutableList.copyOf(animeListSearchResultList);
    }


    /**
     * @return The list containing search results from filter list.
     */
    public List<MinimalEntry> getFilterListSearchResultList() {
        return ImmutableList.copyOf(filterListSearchResultList);
    }


    /**
     * @return The list containing search results from watch list.
     */
    public List<MinimalEntry> getWatchListSearchResultList() {
        return ImmutableList.copyOf(watchListSearchResultList);
    }


    /**
     * @return the searchString
     */
    public String getSearchString() {
        return searchString;
    }


    public void addAnimeListSearchResult(final MinimalEntry entry) {
        if (entry != null) {
            animeListSearchResultList.add(entry);
        }
    }


    public void addFilterListSearchResult(final MinimalEntry entry) {
        if (entry != null) {
            filterListSearchResultList.add(entry);
        }
    }


    public void addWatchListSearchResult(final MinimalEntry entry) {
        if (entry != null) {
            watchListSearchResultList.add(entry);
        }
    }
}

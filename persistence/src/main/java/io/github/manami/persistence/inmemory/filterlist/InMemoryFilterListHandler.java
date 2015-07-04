package io.github.manami.persistence.inmemory.filterlist;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.manami.dto.comparator.MinimalEntryComByTitleAsc;
import io.github.manami.dto.entities.*;
import io.github.manami.persistence.FilterListHandler;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author manami project
 * @since 2.7.0
 */
@Named
public class InMemoryFilterListHandler implements FilterListHandler {

    private final Map<String, FilterEntry> filterList;


    public InMemoryFilterListHandler() {
        filterList = Maps.newConcurrentMap();
    }


    @Override
    public boolean filterAnime(final MinimalEntry anime) {
        if (anime == null || StringUtils.isBlank(anime.getInfoLink()) || filterList.containsKey(anime.getInfoLink())) {
            return false;
        }

        FilterEntry entry = null;

        if (anime instanceof Anime || anime instanceof WatchListEntry) {
            entry = FilterEntry.valueOf(anime);
        } else if (anime instanceof FilterEntry) {
            entry = (FilterEntry) anime;
        }

        if (entry != null && StringUtils.isBlank(entry.getThumbnail())) {
            entry.setThumbnail(AbstractMinimalEntry.NO_IMG_THUMB);
        }

        if (entry != null) {
            filterList.put(entry.getInfoLink(), entry);
            return true;
        }

        return false;
    }


    @Override
    public List<FilterEntry> fetchFilterList() {
        final List<FilterEntry> sortList = Lists.newArrayList(filterList.values());
        Collections.sort(sortList, new MinimalEntryComByTitleAsc());
        return ImmutableList.copyOf(sortList);
    }


    @Override
    public boolean filterEntryExists(final String url) {
        return filterList.containsKey(url);
    }


    @Override
    public boolean removeFromFilterList(final String url) {
        return filterList.remove(url) != null;
    }


    /**
     * @since 2.7.0
     */
    public void clear() {
        filterList.clear();
    }
}

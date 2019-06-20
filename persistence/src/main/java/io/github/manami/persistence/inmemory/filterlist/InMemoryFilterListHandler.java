package io.github.manami.persistence.inmemory.filterlist;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newConcurrentMap;
import static io.github.manami.dto.entities.MinimalEntry.isValidMinimalEntry;

import com.google.common.collect.ImmutableList;
import io.github.manami.dto.comparator.MinimalEntryComByTitleAsc;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterListEntry;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.persistence.FilterListHandler;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Named;

@Named
public class InMemoryFilterListHandler implements FilterListHandler {

  private final Map<InfoLink, FilterListEntry> filterList;


  public InMemoryFilterListHandler() {
    filterList = newConcurrentMap();
  }


  @Override
  public boolean filterAnime(final MinimalEntry anime) {
    if (!isValidMinimalEntry(anime) || filterList.containsKey(anime.getInfoLink())) {
      return false;
    }

    Optional<FilterListEntry> entry = Optional.empty();

    if (anime instanceof Anime || anime instanceof WatchListEntry) {
      entry = FilterListEntry.valueOf(anime);
    } else if (anime instanceof FilterListEntry) {
      entry = Optional.ofNullable((FilterListEntry) anime);
    }

    if (!entry.isPresent()) {
      return false;
    }

    filterList.put(entry.get().getInfoLink(), entry.get());
    return true;
  }


  @Override
  public List<FilterListEntry> fetchFilterList() {
    final List<FilterListEntry> sortList = newArrayList(filterList.values());
    Collections.sort(sortList, new MinimalEntryComByTitleAsc());
    return ImmutableList.copyOf(sortList);
  }


  @Override
  public boolean filterEntryExists(final InfoLink infoLink) {
    return filterList.containsKey(infoLink);
  }


  @Override
  public boolean removeFromFilterList(final InfoLink infoLink) {
    if (infoLink != null && infoLink.isValid()) {
      return filterList.remove(infoLink) != null;
    }

    return false;
  }


  public void clear() {
    filterList.clear();
  }


  public void updateOrCreate(final FilterListEntry entry) {
    if (isValidMinimalEntry(entry)) {
      filterList.put(entry.getInfoLink(), entry);
    }
  }
}

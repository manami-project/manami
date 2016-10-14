package io.github.manami.core.services;

import com.google.common.collect.ImmutableList;
import io.github.manami.cache.Cache;
import io.github.manami.dto.comparator.MinimalEntryComByTitleAsc;
import io.github.manami.dto.entities.Anime;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.springframework.util.Assert.notNull;

/**
 * This service is called whenever a new list is opened. It creates cache
 * entries if necessary.
 *
 * Always start {@link BackgroundService}s using the {@link ServiceRepository}!
 *
 * @author manami-project
 * @since 2.2.0
 */
@Slf4j
public class CacheInitializationService extends AbstractService<Void> {

    /** Instance of the cache. */
    private final Cache cache;

    /** The user's anime list. */
    private final List<Anime> list;


    /**
     * Constructor awaiting a cache.
     *
     * @since 2.2.0
     * @param cache
     *            Cache
     */
    public CacheInitializationService(final Cache cache, final List<Anime> list) {
        this.cache = cache;
        this.list = list;
    }


    @Override
    public Void execute() {
        notNull(list, "List of animes cannot be null");

        if (list.size() > 0) {

            // Create copy of anime list and reverse it
            final List<Anime> reversedList = newArrayList(ImmutableList.copyOf(list));
            Collections.sort(reversedList, new MinimalEntryComByTitleAsc());
            Collections.reverse(reversedList);

            // create cache entries for the reversed list
            for (int index = 0; index < reversedList.size() && !isInterrupt(); index++) {
                final Anime anime = reversedList.get(index);
                if (anime.getInfoLink().isValid()) {
                    log.debug("Creating cache entry for {} if necessary.", anime.getInfoLink());
                    cache.fetchAnime(anime.getInfoLink());
                }
            }
        }

        return null;
    }


    @Override
    public void reset() {
        cancel();
        super.reset();
    }
}

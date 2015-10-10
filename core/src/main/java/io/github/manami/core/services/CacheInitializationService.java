package io.github.manami.core.services;

import io.github.manami.cache.Cache;
import io.github.manami.dto.comparator.MinimalEntryComByTitleAsc;
import io.github.manami.dto.entities.Anime;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * This service is called whenever a new list is opened. It creates cache
 * entries if necessary.
 *
 * Always start {@link BackgroundService}s using the {@link ServiceRepository}!
 *
 * @author manami-project
 * @since 2.2.0
 */
public class CacheInitializationService extends AbstractService<Void> {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(CacheInitializationService.class);

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
        Assert.notNull(list, "List of animes cannot be null");

        if (list.size() > 0) {

            // Create copy of anime list and reverse it
            final List<Anime> reversedList = Lists.newArrayList(ImmutableList.copyOf(list));
            Collections.sort(reversedList, new MinimalEntryComByTitleAsc());
            Collections.reverse(reversedList);

            // create cache entries for the reversed list
            for (int index = 0; index < reversedList.size() && !isInterrupt(); index++) {
                final Anime anime = reversedList.get(index);
                if (StringUtils.isNotBlank(anime.getInfoLink())) {
                    LOG.debug("Creating cache entry for {} if necessary.", anime.getInfoLink());
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

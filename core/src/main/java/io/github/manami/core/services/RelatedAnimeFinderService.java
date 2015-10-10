package io.github.manami.core.services;

import io.github.manami.cache.Cache;
import io.github.manami.core.Manami;
import io.github.manami.core.services.events.ProgressState;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.MinimalEntry;

import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Stack;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.javafx.collections.ObservableListWrapper;

/**
 * Finds related animes in info site links.
 *
 * Always start {@link BackgroundService}s using the {@link ServiceRepository}!
 *
 * @author manami-project
 * @since 2.3.0
 */
public class RelatedAnimeFinderService extends AbstractService<Map<String, Anime>> {

    private static final Logger LOG = LoggerFactory.getLogger(RelatedAnimeFinderService.class);

    /** Contains all animes which are already in the anime list. */
    private final List<String> myAnimes;

    /** Stack of animes which need to be checked. */
    private Stack<String> animesToCheck;

    /** List of all related animes. This output is being shown to the user. */
    private final Map<String, Anime> relatedAnime;

    /** Animes which have already been checked. */
    private final ObservableList<String> checkedAnimes;

    /** Instance of the cache. */
    private final Cache cache;

    /** Core application. */
    private final Manami app;

    private final List<? extends MinimalEntry> list;


    /**
     *
     * @param cache
     *            Instance of the cache.
     * @param app
     *            Instance of the manami app.
     * @param list
     *            List which being checked for related animes.
     * @param observer
     *            Observer which is being notified about the progress. It also
     *            gets the result as a list through notification.
     */
    public RelatedAnimeFinderService(final Cache cache, final Manami app, final List<? extends MinimalEntry> list, final Observer observer) {
        this.app = app;
        this.cache = cache;
        this.list = list;
        addObserver(observer);
        myAnimes = Lists.newArrayList();
        relatedAnime = Maps.newHashMap();
        animesToCheck = new Stack<>();
        checkedAnimes = new ObservableListWrapper<>(Lists.newArrayList());
        checkedAnimes.addListener((ListChangeListener<String>) event -> {
            setChanged();
            notifyObservers(new ProgressState(checkedAnimes.size(), animesToCheck.size()));
        });

    }


    @Override
    public Map<String, Anime> execute() {
        list.forEach(entry -> {
            final String url = entry.getInfoLink();
            if (StringUtils.isNotBlank(url)) {
                myAnimes.add(url);
                animesToCheck.push(url);
            }
        });

        // Sort stack
        final Stack<String> sortedStack = new Stack<>();
        while (!animesToCheck.isEmpty()) {
            sortedStack.push(animesToCheck.pop());
        }
        animesToCheck = sortedStack;

        while (!animesToCheck.empty() && !isInterrupt()) {
            final String entry = animesToCheck.pop();

            if (!checkedAnimes.contains(entry)) {
                LOG.debug("Checking {} for related animes.", entry);
                checkAnime(entry);
            }
        }

        return relatedAnime;
    }


    @Override
    public void reset() {
        cancel();
        myAnimes.clear();
        animesToCheck.clear();
        relatedAnime.clear();
        checkedAnimes.clear();
        super.reset();
    }


    private void checkAnime(final String url) {
        final List<Anime> showAnimeList = Lists.newArrayList();
        final Anime cachedAnime = cache.fetchAnime(url);

        if (cachedAnime == null) {
            return;
        }

        final List<String> relatedAnimeList = cachedAnime.getRelatedAnimes();

        for (int index = 0; index < relatedAnimeList.size() && !isInterrupt(); index++) {
            final String element = relatedAnimeList.get(index);

            if (StringUtils.isNotBlank(element)) {

                if (!animesToCheck.contains(element) && !checkedAnimes.contains(element) && !app.filterEntryExists(element)) {
                    animesToCheck.push(element);
                }

                if (!relatedAnime.containsKey(element) && !app.animeEntryExists(element) && !app.watchListEntryExists(element) && !app.filterEntryExists(element)) {
                    final Anime curAnime = cache.fetchAnime(element);
                    relatedAnime.put(element, curAnime);
                    showAnimeList.add(curAnime);
                }
            }
        }

        setChanged();

        if (!showAnimeList.isEmpty()) {
            LOG.trace("\n\n---------------- Extracted animes for [{}] ----------------", url);
            showAnimeList.forEach(e -> LOG.trace("{} : {}", e.getTitle(), e.getInfoLink()));
            LOG.trace("-----------------------------------------------------------\n\n");
        }

        notifyObservers(showAnimeList);
        checkedAnimes.add(url);
    }
}

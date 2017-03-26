package io.github.manami.core.services;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;
import com.sun.javafx.collections.ObservableSetWrapper;

import io.github.manami.cache.Cache;
import io.github.manami.core.Manami;
import io.github.manami.core.services.events.ProgressState;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.MinimalEntry;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import lombok.extern.slf4j.Slf4j;

/**
 * Finds related animes in info site links.
 * Always start {@link BackgroundService}s using the {@link ServiceRepository}!
 *
 * @author manami-project
 * @since 2.3.0
 */
@Slf4j
public class RelatedAnimeFinderService extends AbstractService<Map<InfoLink, Anime>> {

    /** Stack of animes which need to be checked. */
    private Stack<InfoLink> animesToCheck;

    /** List of all related animes. This output is being shown to the user. */
    private final Map<InfoLink, Anime> relatedAnime;

    /** Animes which have already been checked. */
    private final ObservableSet<InfoLink> checkedAnimes;

    /** Instance of the cache. */
    private final Cache cache;

    /** Core application. */
    private final Manami app;

    private final List<? extends MinimalEntry> list;

    private final AtomicInteger progress = new AtomicInteger(0);

    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);


    /**
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
        relatedAnime = newHashMap();
        animesToCheck = new Stack<>();
        checkedAnimes = new ObservableSetWrapper<>(newHashSet());
        checkedAnimes.addListener((SetChangeListener<InfoLink>) event -> {
            setChanged();
            notifyObservers(new ProgressState(progress.incrementAndGet(), list.size() + animesToCheck.size()));
        });
    }


    @Override
    public Map<InfoLink, Anime> execute() {
        final List<Callable<Void>> initialTaskList = newArrayList();

        list.forEach(entry -> {
            initialTaskList.add(() -> {
                if (!isInterrupt() && entry.getInfoLink().isValid()) {
                    log.debug("Checking [{}] (initial entry) for related anime.", entry);
                    checkAnime(entry.getInfoLink());
                    checkedAnimes.add(entry.getInfoLink());
                }
                return null;
            });
        });

        try {
            executorService.invokeAll(initialTaskList);
        } catch (final InterruptedException e) {
            log.error("Error on invoking getting related anime: ", e);
            cancel();
        }

        // Sort stack
        final Stack<InfoLink> sortedStack = new Stack<>();

        while (!animesToCheck.isEmpty()) {
            sortedStack.push(animesToCheck.pop());
        }

        animesToCheck = sortedStack;

        while (!animesToCheck.empty() && !isInterrupt()) {
            final InfoLink entry = animesToCheck.pop();

            if (!checkedAnimes.contains(entry)) {
                log.debug("Checking {} for related anime.", entry);
                checkAnime(entry);
            }
        }

        return relatedAnime;
    }


    @Override
    public void reset() {
        cancel();
        animesToCheck.clear();
        relatedAnime.clear();
        checkedAnimes.clear();
        super.reset();
    }


    private void checkAnime(final InfoLink infoLink) {
        final List<Anime> showAnimeList = newArrayList();
        final Optional<Anime> optCachedAnime = cache.fetchAnime(infoLink);

        if (!optCachedAnime.isPresent()) {
            return;
        }

        final Anime cachedAnime = optCachedAnime.get();

        final List<InfoLink> relatedAnimeList = Lists.newArrayList();
        cache.fetchRelatedAnimes(cachedAnime.getInfoLink()).forEach(relatedAnimeList::add);

        for (int index = 0; index < relatedAnimeList.size() && !isInterrupt(); index++) {
            final InfoLink element = relatedAnimeList.get(index);

            if (element.isValid()) {
                if (!animesToCheck.contains(element) && !checkedAnimes.contains(element) && !app.filterEntryExists(element)) {
                    animesToCheck.push(element);
                }

                if (!relatedAnime.containsKey(element) && !app.animeEntryExists(element) && !app.watchListEntryExists(element) && !app.filterEntryExists(element)) {
                    final Anime curAnime = cache.fetchAnime(element).get();
                    relatedAnime.put(element, curAnime);
                    showAnimeList.add(curAnime);
                }
            }
        }

        setChanged();

        if (!showAnimeList.isEmpty() && log.isTraceEnabled()) {
            log.trace("\n\n---------------- Extracted animes for [{}] ----------------", infoLink);
            showAnimeList.forEach(e -> log.trace("{} : {}", e.getTitle(), e.getInfoLink()));
            log.trace("-----------------------------------------------------------\n\n");
        }

        notifyObservers(showAnimeList);
        checkedAnimes.add(infoLink);
    }
}

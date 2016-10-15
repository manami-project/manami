package io.github.manami.core.services;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newCopyOnWriteArrayList;
import static com.google.common.collect.Maps.newConcurrentMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Observer;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.manami.cache.Cache;
import io.github.manami.cache.strategies.headlessbrowser.extractor.AnimeExtractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.anime.mal.MyAnimeListNetAnimeExtractor;
import io.github.manami.core.Manami;
import io.github.manami.core.services.events.AdvancedProgressState;
import io.github.manami.core.services.events.ProgressState;
import io.github.manami.dto.entities.Anime;
import lombok.extern.slf4j.Slf4j;

/**
 * Extracts and counts recommendations for a list of animes.
 * Always start {@link BackgroundService}s using the {@link ServiceRepository}!
 *
 * @author manami-project
 * @since 2.4.0
 */
@Slf4j
public class RecommendationsRetrievalService extends AbstractService<List<Anime>> {

    /** List to be searched for recommendations. */
    private final List<String> urlList;

    /** List which is being given to the GUI. */
    private List<Anime> resultList;

    /** All possible recommendations */
    private Map<String, Integer> recommendationsAll;

    /**
     * All recommendations that make 80% of all written user recommendations.
     */
    private List<String> userRecomList;
    private final AnimeExtractor extractor;
    private final Manami app;
    private final Cache cache;
    private final AtomicInteger progress = new AtomicInteger(0);
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);


    /**
     * @since 2.5.0
     * @param app
     * @param cache
     * @param observer
     */
    public RecommendationsRetrievalService(final Manami app, final Cache cache, final Observer observer) {
        extractor = new MyAnimeListNetAnimeExtractor();
        urlList = newCopyOnWriteArrayList();
        recommendationsAll = newConcurrentMap();
        this.app = app;
        this.cache = cache;
        addObserver(observer);
    }


    @Override
    public List<Anime> execute() {
        app.fetchAnimeList().forEach(entry -> {
            if (isNotBlank(entry.getInfoLink())) {
                urlList.add(entry.getInfoLink());
            }
        });

        Collections.shuffle(urlList);

        final List<Callable<Void>> taskList = newArrayList();

        urlList.forEach(entry -> {
            taskList.add(() -> {
                if (!isInterrupt()) {
                    log.debug("Getting recommendations for {}", entry);
                    getRecommendations(entry);
                    setChanged();
                    notifyObservers(new ProgressState(progress.incrementAndGet(), urlList.size()));
                }
                return null;
            });
        });

        try {
            executorService.invokeAll(taskList);
        } catch (final InterruptedException e) {
            log.error("Error on invoking getting recommendations: ", e);
            cancel();
        }

        if (!isInterrupt()) {
            finalizeRecommendations();
        }

        for (int i = 0; i < userRecomList.size() && !isInterrupt(); i++) {
            setChanged();

            /*
             * +1 on i because i starts with 0 and +1 because the value
             * indicates the next entry to be loaded
             */
            final int nextEntryIndex = i + 2;

            notifyObservers(new AdvancedProgressState(nextEntryIndex, userRecomList.size(), cache.fetchAnime(userRecomList.get(i)).get()));
        }

        return resultList;
    }


    private void getRecommendations(final String url) {
        final Optional<Anime> animeToFindRecommendationsFor = cache.fetchAnime(url);

        if (!animeToFindRecommendationsFor.isPresent()) {
            return;
        }

        cache.fetchRecommendations(animeToFindRecommendationsFor.get()).forEach((key, value) -> addRecom(key, value));
    }


    private void addRecom(final String url, final int amount) {
        if (isInterrupt()) {
            return;
        }

        final String normalizedUrl = extractor.normalizeInfoLink(url);

        if (!urlList.contains(normalizedUrl) && !app.filterEntryExists(normalizedUrl) && !app.watchListEntryExists(normalizedUrl)) {
            if (recommendationsAll.containsKey(normalizedUrl)) {
                recommendationsAll.put(normalizedUrl, recommendationsAll.get(normalizedUrl) + amount);
            } else {
                recommendationsAll.put(normalizedUrl, amount);
            }
        }
    }


    private void finalizeRecommendations() {
        int sumAll = 0;
        recommendationsAll = sortMapByValue(recommendationsAll);

        for (final Entry<String, Integer> entry : recommendationsAll.entrySet()) {
            sumAll += entry.getValue();
        }

        userRecomList = newArrayList();
        int percentage = 0;
        int curSum = 0;

        for (final Entry<String, Integer> entry : recommendationsAll.entrySet()) {
            if (percentage < 80 && userRecomList.size() < 100) {
                userRecomList.add(entry.getKey());
                curSum += entry.getValue();
                percentage = (curSum * 100) / sumAll;
            } else {
                return;
            }
        }
    }


    private static Map<String, Integer> sortMapByValue(final Map<String, Integer> unsortMap) {
        // Convert Map to List
        final List<Map.Entry<String, Integer>> list = newArrayList(unsortMap.entrySet());

        // Sort list with comparator, to compare the Map values
        Collections.sort(list, (o1, o2) -> (o1.getValue() > o2.getValue()) ? -1 : ((Objects.equals(o1.getValue(), o2.getValue())) ? 0 : 1));

        // Convert sorted map back to a Map
        final Map<String, Integer> sortedMap = new LinkedHashMap<>();
        for (final Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }


    @Override
    public void reset() {
        cancel();
        urlList.clear();
        recommendationsAll.clear();
        super.reset();
    }
}

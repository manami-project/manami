package io.github.manami.core.services;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newCopyOnWriteArrayList;
import static com.google.common.collect.Maps.newConcurrentMap;

import java.security.SecureRandom;
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
import io.github.manami.dto.entities.InfoLink;
import lombok.extern.slf4j.Slf4j;

/**
 * Extracts and counts recommendations for a list of anime.
 * Always start {@link BackgroundService}s using the {@link ServiceRepository}!
 */
@Slf4j
public class RecommendationsRetrievalService extends AbstractService<List<Anime>> {

    /**
     * Max percentage rate which the shown recommendations can make out of all
     * entries.
     */
    private static final int MAX_PERCENTAGE = 80;

    /** Max number of entries of which a recommendations list can consist. */
    private static final int MAX_NUMBER_OF_ENTRIES = 100;

    /** List to be searched for recommendations. */
    private final List<InfoLink> urlList;

    /** List which is being given to the GUI. */
    private List<Anime> resultList;

    /** All possible recommendations */
    private Map<InfoLink, Integer> recommendationsAll;

    /**
     * All recommendations that make 80% of all written user recommendations.
     */
    private List<InfoLink> userRecomList;
    private final AnimeExtractor extractor;
    private final Manami app;
    private final Cache cache;
    private final AtomicInteger progress = new AtomicInteger(0);
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);


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
            if (entry.getInfoLink().isValid()) {
                urlList.add(entry.getInfoLink());
            }
        });

        Collections.shuffle(urlList, new SecureRandom());
        Collections.shuffle(urlList, new SecureRandom());
        Collections.shuffle(urlList, new SecureRandom());
        Collections.shuffle(urlList, new SecureRandom());

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


    private void getRecommendations(final InfoLink infoLink) {
        final Optional<Anime> animeToFindRecommendationsFor = cache.fetchAnime(infoLink);

        if (!animeToFindRecommendationsFor.isPresent()) {
            return;
        }

        cache.fetchRecommendations(animeToFindRecommendationsFor.get().getInfoLink()).asList().forEach((entry) -> addRecom(entry.getInfoLink(), entry.getAmount()));
    }


    private void addRecom(final InfoLink infoLink, final int amount) {
        if (isInterrupt()) {
            return;
        }

        final InfoLink normalizedInfoLink = extractor.normalizeInfoLink(infoLink);

        if (!urlList.contains(normalizedInfoLink) && !app.filterEntryExists(normalizedInfoLink) && !app.watchListEntryExists(normalizedInfoLink)) {
            if (recommendationsAll.containsKey(normalizedInfoLink)) {
                recommendationsAll.put(normalizedInfoLink, recommendationsAll.get(normalizedInfoLink) + amount);
            } else {
                recommendationsAll.put(normalizedInfoLink, amount);
            }
        }
    }


    private void finalizeRecommendations() {
        int sumAll = 0;
        recommendationsAll = sortMapByValue(recommendationsAll);

        for (final Entry<InfoLink, Integer> entry : recommendationsAll.entrySet()) {
            sumAll += entry.getValue();
        }

        userRecomList = newArrayList();
        int percentage = 0;
        int curSum = 0;

        for (final Entry<InfoLink, Integer> entry : recommendationsAll.entrySet()) {
            if (percentage < MAX_PERCENTAGE && userRecomList.size() < MAX_NUMBER_OF_ENTRIES) {
                userRecomList.add(entry.getKey());
                curSum += entry.getValue();
                percentage = (curSum * 100) / sumAll;
            } else {
                return;
            }
        }
    }


    private static Map<InfoLink, Integer> sortMapByValue(final Map<InfoLink, Integer> unsortMap) {
        // Convert Map to List
        final List<Map.Entry<InfoLink, Integer>> list = newArrayList(unsortMap.entrySet());

        // Sort list with comparator, to compare the Map values
        Collections.sort(list, (o1, o2) -> (o1.getValue() > o2.getValue()) ? -1 : ((Objects.equals(o1.getValue(), o2.getValue())) ? 0 : 1));

        // Convert sorted map back to a Map
        final Map<InfoLink, Integer> sortedMap = new LinkedHashMap<>();
        for (final Entry<InfoLink, Integer> entry : list) {
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

package io.github.manami.core.services;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import io.github.manami.cache.Cache;
import io.github.manami.cache.extractor.HeadlessBrowser;
import io.github.manami.cache.extractor.anime.AnimeExtractor;
import io.github.manami.cache.extractor.plugins.mal.MyAnimeListNetPlugin;
import io.github.manami.core.Manami;
import io.github.manami.core.services.events.AdvancedProgressState;
import io.github.manami.core.services.events.ProgressState;
import io.github.manami.dto.entities.Anime;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts and counts recommendations for a list of animes.
 *
 * Always start {@link BackgroundService}s using the {@link ServiceRepository}!
 *
 * @author manami project
 * @since 2.4.0
 */
public class RecommendationsRetrievalService extends AbstractService<List<Anime>> {

    private static final Logger LOG = LoggerFactory.getLogger(RecommendationsRetrievalService.class);

    private final HeadlessBrowser browser;

    /** List to be searched for recommendations. */
    private final List<String> urlList;

    /** List which is being given to the GUI. */
    private List<Anime> resultList;

    /** All possible recommendations */
    private Map<String, Integer> recommendationsAll;

    /** All recommendations that make 80% of all written user recommendations. */
    private List<String> userRecomList;
    private final AnimeExtractor extractor;
    private final Manami app;
    private final Cache cache;


    /**
     * @since 2.5.0
     * @param app
     * @param cache
     * @param observer
     */
    public RecommendationsRetrievalService(final Manami app, final Cache cache, final Observer observer) {
        browser = new HeadlessBrowser();
        extractor = new MyAnimeListNetPlugin();
        urlList = Lists.newArrayList();
        recommendationsAll = Maps.newHashMap();
        this.app = app;
        this.cache = cache;
        addObserver(observer);
    }


    @Override
    public void start() {

        reset();
        app.fetchAnimeList().forEach(entry -> {
            if (StringUtils.isNotBlank(entry.getInfoLink())) {
                urlList.add(entry.getInfoLink());
            }
        });
        createAndStartService();
    }


    private void getRecommendations(final String url) {
        final String recomUrl = String.format("%s/Death_Note/userrecs", url);
        String recomSite = (recomUrl.startsWith("http")) ? browser.pageAsString(recomUrl) : null;
        final String delimiter = "Make a recommendation for";
        final String animeUrlDelimiter = "/anime/";
        final String recomFlag = "Recommended by";
        recomSite = StringUtils.normalizeSpace(recomSite);
        recomSite = StringUtils.substringBetween(recomSite, delimiter, "</html>");

        if (StringUtils.isNotBlank(recomSite)) {
            String curAnime = null;

            while (recomSite.length() > 0) {
                if (curAnime == null && StringUtils.startsWithIgnoreCase(recomSite, animeUrlDelimiter)) {
                    final Pattern entryPattern = Pattern.compile("/anime/([0-9]*?)/");
                    final Matcher entryMatcher = entryPattern.matcher(recomSite);
                    curAnime = (entryMatcher.find()) ? entryMatcher.group() : null;
                    recomSite = StringUtils.substring(recomSite, animeUrlDelimiter.length() - 1, recomSite.length());
                } else if (curAnime != null && !StringUtils.startsWithIgnoreCase(recomSite, "/anime/")) {
                    final int nextAnime = StringUtils.indexOfIgnoreCase(recomSite, animeUrlDelimiter);
                    final String sub = StringUtils.substring(recomSite, 0, nextAnime);

                    if (StringUtils.containsIgnoreCase(sub, recomFlag)) {
                        int numberOfRecoms = StringUtils.countMatches(sub, recomFlag);
                        addRecom(curAnime, numberOfRecoms);
                        recomSite = StringUtils.substring(recomSite, nextAnime - 1);
                    } else {
                        recomSite = StringUtils.substring(recomSite, nextAnime);
                    }

                    curAnime = null;
                } else {
                    recomSite = StringUtils.substring(recomSite, 1, recomSite.length());
                }
            }
        }
    }


    private void addRecom(final String url, final int amount) {
        final String normalizedUrl = extractor.normalizeInfoLink(url);

        if (!urlList.contains(normalizedUrl) && !app.filterEntryExists(normalizedUrl) && !app.watchListEntryExists(normalizedUrl)) {
            if (recommendationsAll.containsKey(normalizedUrl)) {
                recommendationsAll.put(normalizedUrl, recommendationsAll.get(normalizedUrl) + amount);
            } else {
                recommendationsAll.put(normalizedUrl, amount);
            }
        }
    }


    private void createAndStartService() {
        service = new Service<List<Anime>>() {

            @Override
            protected Task<List<Anime>> createTask() {
                return new Task<List<Anime>>() {

                    @Override
                    protected List<Anime> call() throws Exception {
                        for (int i = 0; i < urlList.size() && !isInterrupt(); i++) {
                            LOG.debug("Getting recommendations for {}", urlList.get(i));
                            getRecommendations(urlList.get(i));
                            setChanged();
                            notifyObservers(new ProgressState(i + 1, urlList.size()));
                        }

                        if (!isInterrupt()) {
                            finalizeRecommendations();
                        }

                        for (int i = 0; i < userRecomList.size() && !isInterrupt(); i++) {
                            setChanged();
                            notifyObservers(new AdvancedProgressState(i + 1, userRecomList.size(), cache.fetchAnime(userRecomList.get(i))));
                        }

                        return resultList;
                    }
                };
            }
        };

        service.setOnCancelled(getFailureEvent());
        service.setOnFailed(getFailureEvent());
        service.setOnSucceeded(getSuccessEvent());
        service.start();
    }


    private void finalizeRecommendations() {
        int sumAll = 0;
        recommendationsAll = sortMapByValue(recommendationsAll);

        for (final Entry<String, Integer> entry : recommendationsAll.entrySet()) {
            sumAll += entry.getValue();
        }

        userRecomList = Lists.newArrayList();
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
        final List<Map.Entry<String, Integer>> list = Lists.newArrayList(unsortMap.entrySet());

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

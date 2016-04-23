package io.github.manami.core.services;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.apache.commons.lang3.StringUtils.indexOfIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.normalizeSpace;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.substring;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.manami.cache.Cache;
import io.github.manami.cache.extractor.HeadlessBrowser;
import io.github.manami.cache.extractor.anime.AnimeExtractor;
import io.github.manami.cache.extractor.plugins.mal.MyAnimeListNetPlugin;
import io.github.manami.core.Manami;
import io.github.manami.core.services.events.AdvancedProgressState;
import io.github.manami.core.services.events.ProgressState;
import io.github.manami.dto.entities.Anime;

/**
 * Extracts and counts recommendations for a list of animes.
 *
 * Always start {@link BackgroundService}s using the {@link ServiceRepository}!
 *
 * @author manami-project
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

    /**
     * All recommendations that make 80% of all written user recommendations.
     */
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
        urlList = newArrayList();
        recommendationsAll = newHashMap();
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


    private void getRecommendations(final String url) {
        final String recomUrl = String.format("%s/Death_Note/userrecs", url);
        String recomSite = (recomUrl.startsWith("http")) ? browser.pageAsString(recomUrl) : null;
        final String animeUrlDelimiter = "/anime/";
        final String recomFlag = "Recommended by";
        recomSite = normalizeSpace(recomSite);

        if (isNotBlank(recomSite)) {
            String curAnime = null;

            while (recomSite.length() > 0) {
                if (curAnime == null && startsWithIgnoreCase(recomSite, animeUrlDelimiter)) {
                    final Pattern entryPattern = Pattern.compile("/anime/([0-9]*?)/");
                    final Matcher entryMatcher = entryPattern.matcher(recomSite);
                    curAnime = (entryMatcher.find()) ? entryMatcher.group() : null;
                    recomSite = substring(recomSite, animeUrlDelimiter.length() - 1, recomSite.length());
                } else if (curAnime != null && !startsWithIgnoreCase(recomSite, "/anime/")) {
                    final int nextAnime = indexOfIgnoreCase(recomSite, animeUrlDelimiter);
                    final String sub = substring(recomSite, 0, nextAnime);

                    if (containsIgnoreCase(sub, recomFlag)) {
                        final int numberOfRecoms = countMatches(sub, recomFlag);
                        addRecom(curAnime, numberOfRecoms);
                        recomSite = substring(recomSite, nextAnime - 1);
                    } else {
                        recomSite = substring(recomSite, nextAnime);
                    }

                    curAnime = null;
                } else {
                    recomSite = substring(recomSite, 1, recomSite.length());
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

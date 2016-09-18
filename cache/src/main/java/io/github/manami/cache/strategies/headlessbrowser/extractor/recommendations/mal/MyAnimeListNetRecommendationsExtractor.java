package io.github.manami.cache.strategies.headlessbrowser.extractor.recommendations.mal;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.apache.commons.lang3.StringUtils.indexOfIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.normalizeSpace;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.substring;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

import io.github.manami.cache.strategies.headlessbrowser.extractor.Extractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.recommendations.RecommendationsExtractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.util.mal.MyAnimeListNetUtil;

@Named
@Extractor
public class MyAnimeListNetRecommendationsExtractor implements RecommendationsExtractor {

    private static final String DELIMITER_NEXT_RECOMMENDATION = "<div class=\"picSurround\">";


    @Override
    public boolean isResponsible(final String url) {
        return MyAnimeListNetUtil.isResponsible(url);
    }


    @Override
    public String normalizeInfoLink(final String url) {
        String normalizedUrl = MyAnimeListNetUtil.normalizeInfoLink(url);

        final String patternString = String.format("http://%s/anime/[0-9]+", MyAnimeListNetUtil.DOMAIN);

        // no tailings
        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(normalizedUrl);

        if (matcher.find()) {
            normalizedUrl = String.format("%s/Death_Note/userrecs", normalizedUrl);
        }

        return normalizedUrl;
    }


    @Override
    public Map<String, Integer> extractRecommendations(final String siteContent) {
        final Map<String, Integer> ret = Maps.newHashMap();

        final String animeUrlDelimiter = "/anime/";
        final String recomFlag = "Recommended by";
        String recomSite = normalizeSpace(siteContent);

        if (isNotBlank(recomSite)) {
            String curAnime = null;

            while (recomSite.length() > 0) {
                if (curAnime == null && startsWithIgnoreCase(recomSite, animeUrlDelimiter)) {
                    final Pattern entryPattern = Pattern.compile("/anime/([0-9]*?)/");
                    final Matcher entryMatcher = entryPattern.matcher(recomSite);
                    curAnime = (entryMatcher.find()) ? entryMatcher.group() : null;
                    recomSite = substring(recomSite, animeUrlDelimiter.length() - 1, recomSite.length());
                } else if (curAnime != null && !startsWithIgnoreCase(recomSite, "/anime/")) {
                    final int nextAnime = indexOfIgnoreCase(recomSite, DELIMITER_NEXT_RECOMMENDATION);
                    final String sub = substring(recomSite, 0, nextAnime);

                    if (containsIgnoreCase(sub, recomFlag)) {
                        final int numberOfRecoms = countMatches(sub, recomFlag);
                        final String fullUrl = createCleanFullUrl(curAnime);

                        ret.put(fullUrl, numberOfRecoms);
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

        return ret;
    }


    /**
     * @param curAnime
     * @return
     */
    private String createCleanFullUrl(final String curAnime) {
        return StringUtils.removeEnd(String.format("http://%s%s", MyAnimeListNetUtil.DOMAIN, curAnime), "/");
    }
}

package io.github.manami.cache.strategies.headlessbrowser.extractor.recommendations.mal;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.apache.commons.lang3.StringUtils.indexOfIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.normalizeSpace;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.substring;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import io.github.manami.cache.strategies.headlessbrowser.extractor.Extractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.recommendations.RecommendationsExtractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.util.mal.MyAnimeListNetUtil;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.Recommendation;
import io.github.manami.dto.entities.RecommendationList;

@Named
@Extractor
public class MyAnimeListNetRecommendationsExtractor implements RecommendationsExtractor {

    private static final String DELIMITER_NEXT_RECOMMENDATION = "<div class=\"picSurround\">";


    @Override
    public boolean isResponsible(final InfoLink infoLink) {
        return MyAnimeListNetUtil.isResponsible(infoLink);
    }


    @Override
    public InfoLink normalizeInfoLink(final InfoLink infoLink) {
        InfoLink normalizedInfoLink = MyAnimeListNetUtil.normalizeInfoLink(infoLink);

        final String patternString = String.format("https://%s/anime/[0-9]+", MyAnimeListNetUtil.DOMAIN);

        // no tailings
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(normalizedInfoLink.getUrl());

        if (matcher.find()) {
            String malId = EMPTY;
            pattern = Pattern.compile("[0-9]+");
            matcher = pattern.matcher(normalizedInfoLink.getUrl());

            if (matcher.find()) {
                malId = matcher.group();
                normalizedInfoLink = new InfoLink(String.format("https://%s/anime.php?id=%s&display=userrecs", MyAnimeListNetUtil.DOMAIN, malId));
            }
        }

        return normalizedInfoLink;
    }


    @Override
    public RecommendationList extractRecommendations(final String siteContent) {
        final RecommendationList ret = new RecommendationList();

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

                        ret.addRecommendation(new Recommendation(new InfoLink(fullUrl), numberOfRecoms));
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

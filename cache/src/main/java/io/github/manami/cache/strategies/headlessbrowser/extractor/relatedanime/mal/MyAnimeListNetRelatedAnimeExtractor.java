package io.github.manami.cache.strategies.headlessbrowser.extractor.relatedanime.mal;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

import io.github.manami.cache.strategies.headlessbrowser.extractor.Extractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.relatedanime.RelatedAnimeExtractor;
import io.github.manami.cache.strategies.headlessbrowser.extractor.util.mal.MyAnimeListNetUtil;

@Named
@Extractor
public class MyAnimeListNetRelatedAnimeExtractor implements RelatedAnimeExtractor {

    @Override
    public Set<String> extractRelatedAnimes(final String siteContent) {
        final List<String> raw = newArrayList();
        String subStr = siteContent.trim();

        // get rid of all whitespaces
        subStr = subStr.replaceAll("\\s", "");

        Pattern pattern = Pattern.compile("</div>RelatedAnime</h2>(.*?)<h2>");
        Matcher matcher = pattern.matcher(subStr);

        if (matcher.find()) {
            subStr = matcher.group();
        }

        if (isNotBlank(subStr) && isNotBlank(siteContent) && !siteContent.startsWith(subStr.substring(0, 4))) {
            pattern = Pattern.compile("/anime/[0-9]+");
            matcher = pattern.matcher(subStr);

            while (matcher.find()) {
                raw.add(matcher.group());
            }
        }

        final Set<String> ret = newHashSet();
        if (raw.size() > 0) {
            raw.forEach(element -> {
                final String relatedAnimeUrl = normalizeInfoLink(element);
                if (!ret.contains(relatedAnimeUrl)) {
                    ret.add(relatedAnimeUrl);
                }
            });
        }

        return ret;
    }


    @Override
    public boolean isResponsible(final String url) {
        return MyAnimeListNetUtil.isResponsible(url);
    }


    @Override
    public String normalizeInfoLink(final String url) {
        return MyAnimeListNetUtil.normalizeInfoLink(url);
    }
}

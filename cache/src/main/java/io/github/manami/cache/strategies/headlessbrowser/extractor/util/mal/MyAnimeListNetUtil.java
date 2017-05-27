package io.github.manami.cache.strategies.headlessbrowser.extractor.util.mal;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.manami.dto.entities.InfoLink;

public final class MyAnimeListNetUtil {

    /** Domain name. */
    public final static String DOMAIN = "myanimelist.net";


    private MyAnimeListNetUtil() {
    }


    public static boolean isResponsible(final InfoLink infoLink) {
        if (!infoLink.isValid()) {
            return false;
        }

        final String url = infoLink.getUrl();

        return url.startsWith("http://" + DOMAIN) || url.startsWith("http://www." + DOMAIN) || url.startsWith("https://" + DOMAIN) || url.startsWith("https://www." + DOMAIN);
    }


    public static InfoLink normalizeInfoLink(final InfoLink infoLink) {
        final String prefix = String.format("https://%s/anime", DOMAIN);

        String ret = infoLink.getUrl();

        // no tailings
        Pattern pattern = Pattern.compile(".*?/[0-9]+");
        Matcher matcher = pattern.matcher(ret);

        if (matcher.find()) {
            ret = matcher.group();
        }

        // correct prefix
        if (isNotBlank(ret) && !ret.startsWith(prefix)) {
            pattern = Pattern.compile("[0-9]+");
            matcher = pattern.matcher(ret);

            if (matcher.find()) {
                ret = String.format("%s/%s", prefix, matcher.group());
            }
        }

        return new InfoLink(ret);
    }
}

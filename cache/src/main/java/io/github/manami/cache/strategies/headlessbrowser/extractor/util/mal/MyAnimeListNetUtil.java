package io.github.manami.cache.strategies.headlessbrowser.extractor.util.mal;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MyAnimeListNetUtil {

    /** Domain name. */
    public final static String DOMAIN = "myanimelist.net";


    private MyAnimeListNetUtil() {
    }


    public static boolean isResponsible(final String url) {
        if (isBlank(url)) {
            return false;
        }

        return url.startsWith("http://" + DOMAIN) || url.startsWith("http://www." + DOMAIN) || url.startsWith("https://" + DOMAIN) || url.startsWith("https://www." + DOMAIN);
    }


    public static String normalizeInfoLink(final String url) {
        final String prefix = String.format("http://%s/anime", DOMAIN);

        // no tailings
        Pattern pattern = Pattern.compile(".*?/[0-9]+");
        Matcher matcher = pattern.matcher(url);

        String ret = null;

        if (matcher.find()) {
            ret = matcher.group();
        }

        // correct prefix
        if (isNotBlank(ret) && !ret.startsWith(prefix)) {
            pattern = Pattern.compile("/[0-9]+");
            matcher = pattern.matcher(url);

            if (matcher.find()) {
                ret = prefix + matcher.group();
            }
        }

        return ret;
    }
}

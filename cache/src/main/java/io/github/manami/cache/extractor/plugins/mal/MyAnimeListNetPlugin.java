package io.github.manami.cache.extractor.plugins.mal;

import com.google.common.collect.Lists;
import io.github.manami.cache.extractor.anime.AbstractAnimeSitePlugin;
import io.github.manami.cache.extractor.anime.Extractor;
import io.github.manami.dto.AnimeType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to gather information from myanimelist.net automatically.
 *
 * @author manami project
 * @since 2.0.0
 */
@Named
@Extractor
public class MyAnimeListNetPlugin extends AbstractAnimeSitePlugin {

    private static final Logger LOG = LoggerFactory.getLogger(MyAnimeListNetPlugin.class);

    /** Regex Pattern. */
    private Pattern pattern;

    /** Regex matcher. */
    private Matcher matcher;

    /** String which is shown on the site in case an id does not exists. */
    private final static String INVALID_REQUEST = "Invalid Request";

    /** Request unsuccessful by incapsula. */
    private static final String REQUEST_UNSUCCESSFUL = "Request unsuccessful. Incapsula incident ID";

    /** Domain name. */
    private final static String DOMAIN = "myanimelist.net";


    @Override
    protected boolean isValidInfoLink() {
        return !siteContent.contains(INVALID_REQUEST) && !siteContent.contains(REQUEST_UNSUCCESSFUL);
    }


    @Override
    protected String extractTitle() {
        pattern = Pattern.compile("<h1>.+</h1>");
        matcher = pattern.matcher(siteContent);
        String title = null;

        if (matcher.find()) {
            title = matcher.group();

            pattern = Pattern.compile("</div>(.*?)</h1>");
            matcher = pattern.matcher(title);

            if (matcher.find()) {
                title = matcher.group();
                title = title.replace("</div>", "").replace("</h1>", "");
                title = title.trim();
            }
        }

        return title;
    }


    @Override
    protected AnimeType extractType() {
        pattern = Pattern.compile("<span class=\"dark_text\">(.??)Type:(.??)</span>(.+?)</div>");
        matcher = pattern.matcher(siteContent);
        AnimeType type = null;

        if (matcher.find()) {
            String subStr = matcher.group();

            pattern = Pattern.compile("</span>(.*?)</div>");
            matcher = pattern.matcher(subStr);

            if (matcher.find()) {
                subStr = matcher.group();
                subStr = subStr.replace("</span>", "").replace("</div>", "");
                subStr = subStr.trim();
                type = AnimeType.findByName(subStr);
            }
        }

        if (type == null) {
            LOG.debug("Could not find any type in siteContent. Fallback: Setting type to TV");
            type = AnimeType.TV;
        }

        return type;
    }


    @Override
    protected String extractEpisodes() {
        pattern = Pattern.compile("<span class=\"dark_text\">(.??)Episodes:(.??)</span>(.+?)</div>");
        matcher = pattern.matcher(siteContent);
        String episodes = "1";

        if (matcher.find()) {
            episodes = matcher.group();

            pattern = Pattern.compile("</span>(.*?)</div>");
            matcher = pattern.matcher(episodes);

            if (matcher.find()) {
                episodes = matcher.group();
                episodes = episodes.replace("</span>", "").replace("</div>", "");
                episodes = episodes.trim();
            }
        }

        return episodes;
    }


    @Override
    protected String getDomain() {
        return DOMAIN;
    }


    @Override
    protected String extractPictureLink() {
        pattern = Pattern.compile("http://cdn\\.myanimelist\\.net/images/anime/[0-9]+/[0-9]+\\.jpg");
        matcher = pattern.matcher(siteContent);
        String picture = null;

        if (matcher.find()) {
            picture = matcher.group();
        }

        return picture;
    }


    @Override
    public String normalizeInfoLink(final String url) {
        final String prefix = String.format("http://%s/anime", DOMAIN);

        // no tailings
        pattern = Pattern.compile(".*?/[0-9]+");
        matcher = pattern.matcher(url);

        String ret = null;

        if (matcher.find()) {
            ret = matcher.group();
        }

        // correct prefix
        if (StringUtils.isNotBlank(ret) && !ret.startsWith(prefix)) {
            pattern = Pattern.compile("/[0-9]+");
            matcher = pattern.matcher(url);

            if (matcher.find()) {
                ret = prefix + matcher.group();
            }
        }

        return ret;
    }


    @Override
    protected String extractThumbnail() {
        String picture = extractPictureLink();

        if (StringUtils.isNotBlank(picture)) {
            final StringBuilder strBuilder = new StringBuilder(picture);
            picture = strBuilder.insert(picture.length() - 4, "t").toString();
        }

        return picture;
    }


    @Override
    public List<String> extractRelatedAnimes() {
        final List<String> raw = Lists.newArrayList();
        String subStr = super.siteContent.trim();

        // get rid of all whitespaces
        subStr = subStr.replaceAll("\\s", "");

        pattern = Pattern.compile("</div>RelatedAnime</h2>(.*?)<h2>");
        matcher = pattern.matcher(subStr);

        if (matcher.find()) {
            subStr = matcher.group();
        }

        if (StringUtils.isNotBlank(subStr) && StringUtils.isNotBlank(super.siteContent) && !super.siteContent.startsWith(subStr.substring(0, 4))) {
            pattern = Pattern.compile("/anime/[0-9]+");
            matcher = pattern.matcher(subStr);

            while (matcher.find()) {
                raw.add(matcher.group());
            }
        }

        final List<String> ret = Lists.newArrayList();
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
}

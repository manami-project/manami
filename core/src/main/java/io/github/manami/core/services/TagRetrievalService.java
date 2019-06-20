package io.github.manami.core.services;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.symmetricDifference;

import java.util.Observer;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;

import io.github.manami.cache.Cache;
import io.github.manami.cache.strategies.headlessbrowser.JavaUrlConnection;
import io.github.manami.core.Manami;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;
import lombok.extern.slf4j.Slf4j;

/**
 * Finds related anime in info site links.
 * Always start {@link BackgroundService}s using the {@link ServiceRepository}!
 */
@Slf4j
public class TagRetrievalService extends AbstractService<Void> {

    private static final String LOG_MSG_LAST_PAGE = "Last page. No more entries for this genre.";

    /** Instance of the cache. */
    private final Cache cache;

    /** Core application. */
    private final Manami app;

    private final String tagUrl;

    private final JavaUrlConnection javaUrlConnection;

    private final Set<InfoLink> foundAll;
    private Set<InfoLink> foundPerPage;
    private Set<InfoLink> foundPreviousPage;
    private final Pattern pattern;


    /**
     * @param cache
     *            Instance of the cache.
     * @param app
     *            Instance of the manami app.
     * @param list
     *            List which being checked for related anime.
     * @param observer
     *            Observer which is being notified about the progress. It also
     *            gets the result as a list through notification.
     */
    public TagRetrievalService(final Cache cache, final Manami app, final String tagUrl, final Observer observer) {
        this.app = app;
        this.cache = cache;
        this.tagUrl = tagUrl;
        addObserver(observer);
        javaUrlConnection = new JavaUrlConnection();
        foundAll = newHashSet();
        foundPerPage = newHashSet();
        foundPreviousPage = newHashSet();
        pattern = Pattern.compile("https://myanimelist\\.net/anime/[0-9]+");
    }


    @Override
    public Void execute() {
        log.info("############################################ START ############################################");
        final InfoLink genre = new InfoLink(tagUrl);
        int pageCounter = 1;

        while (pageCounter > 0) {
            log.info("-------------------------------------------- Page {} --------------------------------------------", pageCounter);
            final InfoLink genrePage = new InfoLink(String.format("%s?page=%s", genre.getUrl(), pageCounter));
            String pageAsString = javaUrlConnection.pageAsString(genrePage);
            pageAsString = pageAsString.substring(0, pageAsString.indexOf("<div class=\"footer-ranking\">"));

            if (!pageAsString.contains("404 Not Found - MyAnimeList.net")) {
                extractAnimes(pageAsString);

                if (pageCounter == 1 || symmetricDifference(foundPerPage, foundPreviousPage).size() > 0) {
                    pageCounter++;
                } else {
                    pageCounter = -1;
                    log.info(LOG_MSG_LAST_PAGE);
                }
            } else {
                pageCounter = -1;
                log.info(LOG_MSG_LAST_PAGE);
            }
        }

        log.info("############################################ STOP ############################################");

        setChanged();
        notifyObservers(Boolean.TRUE);

        return null;
    }


    private void extractAnimes(final String pageAsString) {
        foundPreviousPage = ImmutableSet.copyOf(foundPerPage);
        foundPerPage = newHashSet();
        final Matcher matcher = pattern.matcher(pageAsString);

        while (matcher.find()) {
            foundPerPage.add(new InfoLink(matcher.group()));
        }

        foundPerPage.forEach(e -> log.info("{}", e));

        foundPerPage.stream().filter(e -> e.isValid()).filter(e -> !foundAll.contains(e)).filter(e -> !app.animeEntryExists(e)).filter(e -> !app.watchListEntryExists(e)).filter(e -> !app.filterEntryExists(e)).forEach(e -> {
            final Optional<Anime> anime = cache.fetchAnime(e);
            if (anime.isPresent()) {
                setChanged();
                notifyObservers(anime.get());
            }
        });

        foundAll.addAll(foundPerPage);
    }


    @Override
    public void reset() {
        cancel();
        super.reset();
    }
}

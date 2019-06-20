package io.github.manami.persistence.importer.json;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONTokener;

import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterListEntry;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.persistence.PersistenceFacade;
import io.github.manami.persistence.importer.Importer;
import lombok.extern.slf4j.Slf4j;

/**
 * Imports a list from a valid JSON file.
 */
@Slf4j
public class JsonImporter implements Importer {

    private final PersistenceFacade persistence;
    private final List<Anime> animeListEntries;
    private final List<FilterListEntry> filterListEntries;
    private final List<WatchListEntry> watchListEntries;


    public JsonImporter(final PersistenceFacade persistence) {
        this.persistence = persistence;
        animeListEntries = newArrayList();
        filterListEntries = newArrayList();
        watchListEntries = newArrayList();
    }


    @Override
    public void importFile(final Path file) {
        try {
            final FileReader fileReader = new FileReader(file.toFile());
            final BufferedReader br = new BufferedReader(fileReader);
            final StringBuilder strBuilder = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                strBuilder.append(line).append('\n');
            }

            final JSONTokener tokener = new JSONTokener(strBuilder.toString());
            final JSONArray jsonArr = new JSONArray(tokener);
            extractAnimeList(jsonArr);
            extractWatchList(jsonArr);
            extractFilterList(jsonArr);

            br.close();
        } catch (final IOException e) {
            log.error("An error occurred while trying to import JSON file: ", e);
        }
    }


    private void extractAnimeList(final JSONArray jsonArr) {
        final JSONArray animeListArr = jsonArr.getJSONArray(0);

        for (int i = 0; i < animeListArr.length(); i++) {
            final String title = animeListArr.getJSONObject(i).getString("title").trim();
            final AnimeType type = AnimeType.findByName(animeListArr.getJSONObject(i).getString("type").trim());
            final Integer episodes = animeListArr.getJSONObject(i).getInt("episodes");
            final String location = animeListArr.getJSONObject(i).getString("location").trim();

            String infoLink = animeListArr.getJSONObject(i).getString("infoLink");
            infoLink = (infoLink != null) ? infoLink.trim() : infoLink;

            if (isNotBlank(title) && type != null && episodes != null && isNotBlank(infoLink) && isNotBlank(location)) {
                final Anime curAnime = new Anime(title, new InfoLink(infoLink)).type(type).episodes(episodes).location(location);
                animeListEntries.add(curAnime);
            } else {
                log.debug("Could not import '{}', because the type is unknown.", title);
            }
        }
        persistence.addAnimeList(animeListEntries);
    }


    private void extractWatchList(final JSONArray jsonArr) {
        final JSONArray animeListArr = jsonArr.getJSONArray(1);

        for (int i = 0; i < animeListArr.length(); i++) {
            final String thumbnail = animeListArr.getJSONObject(i).getString("thumbnail").trim();
            final String title = animeListArr.getJSONObject(i).getString("title").trim();
            String infoLink = animeListArr.getJSONObject(i).getString("infoLink");
            infoLink = (infoLink != null) ? infoLink.trim() : infoLink;

            if (isNotBlank(title) && isNotBlank(infoLink)) {
                watchListEntries.add(new WatchListEntry(title, thumbnail, new InfoLink(infoLink)));
            } else {
                log.debug("Could not import '{}', because the type is unknown.", title);
            }
        }
        persistence.addWatchList(watchListEntries);
    }


    private void extractFilterList(final JSONArray jsonArr) {
        final JSONArray animeListArr = jsonArr.getJSONArray(2);

        for (int i = 0; i < animeListArr.length(); i++) {
            final String thumbnail = animeListArr.getJSONObject(i).getString("thumbnail").trim();
            final String title = animeListArr.getJSONObject(i).getString("title").trim();
            String infoLink = animeListArr.getJSONObject(i).getString("infoLink");
            infoLink = (infoLink != null) ? infoLink.trim() : infoLink;

            if (isNotBlank(title) && isNotBlank(infoLink)) {
                filterListEntries.add(new FilterListEntry(title, thumbnail, new InfoLink(infoLink)));
            } else {
                log.debug("Could not import '{}', because the type is unknown.", title);
            }
        }
        persistence.addFilterList(filterListEntries);
    }
}

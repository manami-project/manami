package io.github.manami.persistence.importer.xml.postprocessor;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.List;

import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterListEntry;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.persistence.utility.Version;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImportMigrationPostProcessor {

    private Version documentVersion;
    private final List<Anime> animeListEntries;
    private final List<FilterListEntry> filterListEntries;
    private final List<WatchListEntry> watchListEntries;
    private final String currentToolVersion;
    private final String originalDocumentVersionString;


    public ImportMigrationPostProcessor(final String currentToolVersion, final String documentVersion, final List<Anime> animeListEntries, final List<FilterListEntry> filterListEntries, final List<WatchListEntry> watchListEntries) {
        this.currentToolVersion = currentToolVersion;
        originalDocumentVersionString = documentVersion;
        this.animeListEntries = animeListEntries;
        this.filterListEntries = filterListEntries;
        this.watchListEntries = watchListEntries;
    }


    public void execute() {
        if (!Version.isValid(currentToolVersion)) {
            log.warn("SKIPPING import migration post processor. Current tool version could not be identified.");
            return;
        }

        if (!Version.isValid(originalDocumentVersionString)) {
            log.warn("SKIPPING import migration post processor. Document version could not be identified.");
            return;
        }

        documentVersion = new Version(originalDocumentVersionString);

        log.info("Starting post processing.");

        migrateTo_2_10_3();
        migrateTo_2_14_2();
    }


    /**
     * Converts info links from http to https (Finally MAL! Took you long
     * enough.)
     */
    private void migrateTo_2_14_2() {
        if (documentVersion.isNewerThan("2.14.2")) {
            log.info("SKIPPING migration to 2.14.2.");
            return;
        }

        log.info("Migrating list to version 2.14.2.");
        animeListEntries.forEach(this::migrateMalInfoLinkToHttps);
        filterListEntries.forEach(this::migrateMalInfoLinkToHttps);
        watchListEntries.forEach(this::migrateMalInfoLinkToHttps);
    }


    private void migrateMalInfoLinkToHttps(final MinimalEntry anime) {
        if (anime == null || anime.getInfoLink() == null || !anime.getInfoLink().isValid()) {
            return;
        }

        if (anime.getInfoLink().getUrl().startsWith("http://myanimelist.net") || anime.getInfoLink().getUrl().startsWith("http://www.myanimelist.net")) {
            anime.setInfoLink(new InfoLink(anime.getInfoLink().getUrl().replaceAll("http", "https")));
        }
    }


    /**
     * Converts the picture links of MAL to the new CDN URL.
     */
    private void migrateTo_2_10_3() {
        if (documentVersion.isNewerThan("2.10.3")) {
            log.info("SKIPPING migration to 2.10.3.");
            return;
        }

        log.info("Migrating list to version 2.10.3.");
        filterListEntries.forEach(this::migrateCdnUrl);
        watchListEntries.forEach(this::migrateCdnUrl);
    }


    private void migrateCdnUrl(final MinimalEntry anime) {
        if (anime == null) {
            return;
        }

        final String oldCdnUrl = "http://cdn.myanimelist.net";
        final String newCdnUrl = "https://myanimelist.cdn-dena.com";
        final String thumbnail = anime.getThumbnail();

        if (isNotBlank(thumbnail) && thumbnail.startsWith(oldCdnUrl)) {
            anime.setThumbnail(thumbnail.replaceAll(oldCdnUrl, newCdnUrl));
        }
    }
}

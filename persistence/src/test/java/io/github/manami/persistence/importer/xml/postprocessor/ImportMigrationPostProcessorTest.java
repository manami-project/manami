package io.github.manami.persistence.importer.xml.postprocessor;

import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.Test;
import org.testng.collections.Lists;

import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.WatchListEntry;

public class ImportMigrationPostProcessorTest {

    @Test(groups = "unitTest", description = "Test execution stops, because the tool version is not valid. Result: no changes to the entries.")
    public void testMigrationStopsInvalidToolVersion() {
        // given
        final String entry1Thumb = "http://cdn.myanimelist.net/images/anime/9/9453t.jpg";
        final String entry2Thumb = "http://img7.anidb.net/pics/anime/thumbs/50x65/129527.jpg-thumb.jpg";
        final FilterEntry entry1 = new FilterEntry("Death Note", entry1Thumb, "http://myanimelist.net/anime/1535");
        final FilterEntry entry2 = new FilterEntry("Fullmetal Panic!", entry2Thumb, "http://anidb.net/perl-bin/animedb.pl?show=anime&aid=17");
        final FilterEntry entry3 = new FilterEntry("Code Geass: Hangyaku no Lelouch R2", "https://myanimelist.net/anime/2904/Code_Geass__Hangyaku_no_Lelouch_R2");

        final List<FilterEntry> filterListEntries = Lists.newArrayList(entry1, entry2, entry3);

        final ImportMigrationPostProcessor processor = new ImportMigrationPostProcessor("unknown", "2.10.2", Lists.newArrayList(), filterListEntries, Lists.newArrayList());

        // when
        processor.execute();

        // then
        assertEquals(entry1.getThumbnail(), entry1Thumb);
        assertEquals(entry2.getThumbnail(), entry2Thumb);
        assertEquals(entry3.getThumbnail(), "https://myanimelist.cdn-dena.com/images/qm_50.gif");
    }


    @Test(groups = "unitTest", description = "Test execution stops, because the document version is not valid. Result: no changes to the entries.")
    public void testMigrationStopsInvalidDocumentVersion() {
        // given
        final String entry1Thumb = "http://cdn.myanimelist.net/images/anime/9/9453t.jpg";
        final String entry2Thumb = "http://img7.anidb.net/pics/anime/thumbs/50x65/129527.jpg-thumb.jpg";
        final FilterEntry entry1 = new FilterEntry("Death Note", entry1Thumb, "http://myanimelist.net/anime/1535");
        final FilterEntry entry2 = new FilterEntry("Fullmetal Panic!", entry2Thumb, "http://anidb.net/perl-bin/animedb.pl?show=anime&aid=17");
        final FilterEntry entry3 = new FilterEntry("Code Geass: Hangyaku no Lelouch R2", "https://myanimelist.net/anime/2904/Code_Geass__Hangyaku_no_Lelouch_R2");

        final List<FilterEntry> filterListEntries = Lists.newArrayList(entry1, entry2, entry3);

        final ImportMigrationPostProcessor processor = new ImportMigrationPostProcessor("2.10.3", "2.?.2", Lists.newArrayList(), filterListEntries, Lists.newArrayList());

        // when
        processor.execute();

        // then
        assertEquals(entry1.getThumbnail(), entry1Thumb);
        assertEquals(entry2.getThumbnail(), entry2Thumb);
        assertEquals(entry3.getThumbnail(), "https://myanimelist.cdn-dena.com/images/qm_50.gif");
    }


    @Test(groups = "unitTest", description = "Test migration to version 2.10.3 is skipped, because the document version is not valid. Result: no changes to the entries.")
    public void testMigration2103IsSkippedBecauseTheCurrentDocumentVersionIsMoreRecent() {
        // given
        final String entry1Thumb = "http://cdn.myanimelist.net/images/anime/9/9453t.jpg";
        final String entry2Thumb = "http://img7.anidb.net/pics/anime/thumbs/50x65/129527.jpg-thumb.jpg";
        final FilterEntry entry1 = new FilterEntry("Death Note", entry1Thumb, "http://myanimelist.net/anime/1535");
        final FilterEntry entry2 = new FilterEntry("Fullmetal Panic!", entry2Thumb, "http://anidb.net/perl-bin/animedb.pl?show=anime&aid=17");
        final FilterEntry entry3 = new FilterEntry("Code Geass: Hangyaku no Lelouch R2", "https://myanimelist.net/anime/2904/Code_Geass__Hangyaku_no_Lelouch_R2");

        final List<FilterEntry> filterListEntries = Lists.newArrayList(entry1, entry2, entry3);

        final ImportMigrationPostProcessor processor = new ImportMigrationPostProcessor("2.10.3", "2.?.2", Lists.newArrayList(), filterListEntries, Lists.newArrayList());

        // when
        processor.execute();

        // then
        assertEquals(entry1.getThumbnail(), entry1Thumb);
        assertEquals(entry2.getThumbnail(), entry2Thumb);
        assertEquals(entry3.getThumbnail(), "https://myanimelist.cdn-dena.com/images/qm_50.gif");
    }


    @Test(groups = "unitTest", description = "Test migration to version 2.10.3 for filter list")
    public void testMigration2103WorksForFilterList() {
        // given
        final String fmpThumb = "http://img7.anidb.net/pics/anime/thumbs/50x65/129527.jpg-thumb.jpg";
        final FilterEntry entry1 = new FilterEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");
        final FilterEntry entry2 = new FilterEntry("Fullmetal Panic!", fmpThumb, "http://anidb.net/perl-bin/animedb.pl?show=anime&aid=17");
        final FilterEntry entry3 = new FilterEntry("Code Geass: Hangyaku no Lelouch R2", "https://myanimelist.net/anime/2904/Code_Geass__Hangyaku_no_Lelouch_R2");

        final List<FilterEntry> filterListEntries = Lists.newArrayList(entry1, entry2, entry3);

        final ImportMigrationPostProcessor processor = new ImportMigrationPostProcessor("2.10.3", "2.10.2", Lists.newArrayList(), filterListEntries, Lists.newArrayList());

        // when
        processor.execute();

        // then
        assertEquals(entry1.getThumbnail(), "https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg");
        assertEquals(entry2.getThumbnail(), fmpThumb);
        assertEquals(entry3.getThumbnail(), "https://myanimelist.cdn-dena.com/images/qm_50.gif");
    }


    @Test(groups = "unitTest", description = "Test migration to version 2.10.3 for watch list")
    public void testMigration2103WorksForWatchList() {
        // given
        final String fmpThumb = "http://img7.anidb.net/pics/anime/thumbs/50x65/129527.jpg-thumb.jpg";
        final WatchListEntry entry1 = new WatchListEntry("Death Note", "http://cdn.myanimelist.net/images/anime/9/9453t.jpg", "http://myanimelist.net/anime/1535");
        final WatchListEntry entry2 = new WatchListEntry("Fullmetal Panic!", fmpThumb, "http://anidb.net/perl-bin/animedb.pl?show=anime&aid=17");
        final WatchListEntry entry3 = new WatchListEntry("Code Geass: Hangyaku no Lelouch R2", "https://myanimelist.net/anime/2904/Code_Geass__Hangyaku_no_Lelouch_R2");

        final List<WatchListEntry> watchListEntries = Lists.newArrayList(entry1, entry2, entry3);

        final ImportMigrationPostProcessor processor = new ImportMigrationPostProcessor("2.10.3", "2.10.2", Lists.newArrayList(), Lists.newArrayList(), watchListEntries);

        // when
        processor.execute();

        // then
        assertEquals(entry1.getThumbnail(), "https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg");
        assertEquals(entry2.getThumbnail(), fmpThumb);
        assertEquals(entry3.getThumbnail(), "https://myanimelist.cdn-dena.com/images/qm_50.gif");
    }
}

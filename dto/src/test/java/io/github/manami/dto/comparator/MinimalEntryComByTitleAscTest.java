package io.github.manami.dto.comparator;

import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.WatchListEntry;

public class MinimalEntryComByTitleAscTest {

    @Test(groups = UNIT_TEST_GROUP)
    public void testFirstOneGreater() {
        // given
        final MinimalEntryComByTitleAsc comparator = new MinimalEntryComByTitleAsc();

        final WatchListEntry steinsGate = new WatchListEntry("Steins;Gate", "https://myanimelist.cdn-dena.com/images/anime/5/73199t.jpg", new InfoLink("http://myanimelist.net/anime/9253"));
        final WatchListEntry gintama = new WatchListEntry("Gintama", "https://myanimelist.cdn-dena.com/images/anime/3/72078t.jpg", new InfoLink("http://myanimelist.net/anime/28977"));

        // when
        final int result = comparator.compare(steinsGate, gintama);

        // then
        assertEquals(result > 0, true);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFirstOneLesser() {
        // given
        final MinimalEntryComByTitleAsc comparator = new MinimalEntryComByTitleAsc();

        final WatchListEntry steinsGate = new WatchListEntry("Steins;Gate", "https://myanimelist.cdn-dena.com/images/anime/5/73199t.jpg", new InfoLink("http://myanimelist.net/anime/9253"));
        final WatchListEntry gintama = new WatchListEntry("Gintama", "https://myanimelist.cdn-dena.com/images/anime/3/72078t.jpg", new InfoLink("http://myanimelist.net/anime/28977"));

        // when
        final int result = comparator.compare(gintama, steinsGate);

        // then
        assertEquals(result < 0, true);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testBothEqual() {
        // given
        final MinimalEntryComByTitleAsc comparator = new MinimalEntryComByTitleAsc();

        final WatchListEntry steinsGate = new WatchListEntry("Steins;Gate", "https://myanimelist.cdn-dena.com/images/anime/5/73199t.jpg", new InfoLink("http://myanimelist.net/anime/9253"));

        // when
        final int result = comparator.compare(steinsGate, steinsGate);

        // then
        assertEquals(result, 0);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFirstParameterNull() {
        // given
        final MinimalEntryComByTitleAsc comparator = new MinimalEntryComByTitleAsc();

        final WatchListEntry steinsGate = new WatchListEntry("Steins;Gate", "https://myanimelist.cdn-dena.com/images/anime/5/73199t.jpg", new InfoLink("http://myanimelist.net/anime/9253"));

        // when
        final int result = comparator.compare(null, steinsGate);

        // then
        assertEquals(result, 0);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testSecondParameterNull() {
        // given
        final MinimalEntryComByTitleAsc comparator = new MinimalEntryComByTitleAsc();

        final WatchListEntry steinsGate = new WatchListEntry("Steins;Gate", "https://myanimelist.cdn-dena.com/images/anime/5/73199t.jpg", new InfoLink("http://myanimelist.net/anime/9253"));

        // when
        final int result = comparator.compare(steinsGate, null);

        // then
        assertEquals(result, 0);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testFirstParameterTitleNullOrEmpty() {
        // given
        final MinimalEntryComByTitleAsc comparator = new MinimalEntryComByTitleAsc();

        final WatchListEntry steinsGate = new WatchListEntry("Steins;Gate", "https://myanimelist.cdn-dena.com/images/anime/5/73199t.jpg", new InfoLink("http://myanimelist.net/anime/9253"));
        final WatchListEntry gintama = new WatchListEntry("Gintama", "https://myanimelist.cdn-dena.com/images/anime/3/72078t.jpg", new InfoLink("http://myanimelist.net/anime/28977"));
        gintama.setTitle(EMPTY);

        // when
        final int result = comparator.compare(gintama, steinsGate);

        // then
        assertEquals(result, 0);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testSecondParameterTitleNullOrEmpty() {
        // given
        final MinimalEntryComByTitleAsc comparator = new MinimalEntryComByTitleAsc();

        final WatchListEntry steinsGate = new WatchListEntry("Steins;Gate", "https://myanimelist.cdn-dena.com/images/anime/5/73199t.jpg", new InfoLink("http://myanimelist.net/anime/9253"));
        final WatchListEntry gintama = new WatchListEntry("Gintama", "https://myanimelist.cdn-dena.com/images/anime/3/72078t.jpg", new InfoLink("http://myanimelist.net/anime/28977"));
        steinsGate.setTitle(EMPTY);

        // when
        final int result = comparator.compare(gintama, steinsGate);

        // then
        assertEquals(result, 0);
    }
}

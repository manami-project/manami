package io.github.manami.dto.comparator;

import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.WatchListEntry;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class MinimalEntryComByTitleAscTest {

    @Test(groups = "unitTest")
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


    @Test(groups = "unitTest")
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


    @Test(groups = "unitTest")
    public void testBothEqual() {
        // given
        final MinimalEntryComByTitleAsc comparator = new MinimalEntryComByTitleAsc();

        final WatchListEntry steinsGate = new WatchListEntry("Steins;Gate", "https://myanimelist.cdn-dena.com/images/anime/5/73199t.jpg", new InfoLink("http://myanimelist.net/anime/9253"));

        // when
        final int result = comparator.compare(steinsGate, steinsGate);

        // then
        assertEquals(result, 0);
    }


    @Test(groups = "unitTest")
    public void testFirstParameterNull() {
        // given
        final MinimalEntryComByTitleAsc comparator = new MinimalEntryComByTitleAsc();

        final WatchListEntry steinsGate = new WatchListEntry("Steins;Gate", "https://myanimelist.cdn-dena.com/images/anime/5/73199t.jpg", new InfoLink("http://myanimelist.net/anime/9253"));

        // when
        final int result = comparator.compare(null, steinsGate);

        // then
        assertEquals(result, 0);
    }


    @Test(groups = "unitTest")
    public void testSecondParameterNull() {
        // given
        final MinimalEntryComByTitleAsc comparator = new MinimalEntryComByTitleAsc();

        final WatchListEntry steinsGate = new WatchListEntry("Steins;Gate", "https://myanimelist.cdn-dena.com/images/anime/5/73199t.jpg", new InfoLink("http://myanimelist.net/anime/9253"));

        // when
        final int result = comparator.compare(steinsGate, null);

        // then
        assertEquals(result, 0);
    }


    @Test(groups = "unitTest")
    public void testFirstParameterTitleNullOrEmpty() {
        // given
        final MinimalEntryComByTitleAsc comparator = new MinimalEntryComByTitleAsc();

        final WatchListEntry steinsGate = new WatchListEntry("Steins;Gate", "https://myanimelist.cdn-dena.com/images/anime/5/73199t.jpg", new InfoLink("http://myanimelist.net/anime/9253"));
        final WatchListEntry gintama = new WatchListEntry("Gintama", "https://myanimelist.cdn-dena.com/images/anime/3/72078t.jpg", new InfoLink("http://myanimelist.net/anime/28977"));
        gintama.setTitle("");

        // when
        final int result = comparator.compare(gintama, steinsGate);

        // then
        assertEquals(result, 0);
    }


    @Test(groups = "unitTest")
    public void testSecondParameterTitleNullOrEmpty() {
        // given
        final MinimalEntryComByTitleAsc comparator = new MinimalEntryComByTitleAsc();

        final WatchListEntry steinsGate = new WatchListEntry("Steins;Gate", "https://myanimelist.cdn-dena.com/images/anime/5/73199t.jpg", new InfoLink("http://myanimelist.net/anime/9253"));
        final WatchListEntry gintama = new WatchListEntry("Gintama", "https://myanimelist.cdn-dena.com/images/anime/3/72078t.jpg", new InfoLink("http://myanimelist.net/anime/28977"));
        steinsGate.setTitle("");

        // when
        final int result = comparator.compare(gintama, steinsGate);

        // then
        assertEquals(result, 0);
    }
}

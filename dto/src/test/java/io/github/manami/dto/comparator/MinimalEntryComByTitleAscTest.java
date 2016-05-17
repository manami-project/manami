package io.github.manami.dto.comparator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

import io.github.manami.dto.entities.WatchListEntry;

public class MinimalEntryComByTitleAscTest {

    @Test
    public void testFirstOneGreater() {
        // given
        final MinimalEntryComByTitleAsc comparator = new MinimalEntryComByTitleAsc();

        final WatchListEntry steinsGate = new WatchListEntry("Steins;Gate", "http://cdn.myanimelist.net/images/anime/5/73199t.jpg", "http://myanimelist.net/anime/9253");
        final WatchListEntry gintama = new WatchListEntry("Gintama", "http://cdn.myanimelist.net/images/anime/3/72078t.jpg", "http://myanimelist.net/anime/28977");

        // when
        final int result = comparator.compare(steinsGate, gintama);

        // then
        assertThat(result > 0, equalTo(true));
    }


    @Test
    public void testFirstOneLesser() {
        // given
        final MinimalEntryComByTitleAsc comparator = new MinimalEntryComByTitleAsc();

        final WatchListEntry steinsGate = new WatchListEntry("Steins;Gate", "http://cdn.myanimelist.net/images/anime/5/73199t.jpg", "http://myanimelist.net/anime/9253");
        final WatchListEntry gintama = new WatchListEntry("Gintama", "http://cdn.myanimelist.net/images/anime/3/72078t.jpg", "http://myanimelist.net/anime/28977");

        // when
        final int result = comparator.compare(gintama, steinsGate);

        // then
        assertThat(result < 0, equalTo(true));
    }


    @Test
    public void testBothEqual() {
        // given
        final MinimalEntryComByTitleAsc comparator = new MinimalEntryComByTitleAsc();

        final WatchListEntry steinsGate = new WatchListEntry("Steins;Gate", "http://cdn.myanimelist.net/images/anime/5/73199t.jpg", "http://myanimelist.net/anime/9253");

        // when
        final int result = comparator.compare(steinsGate, steinsGate);

        // then
        assertThat(result, equalTo(0));
    }


    @Test
    public void testFirstParameterNull() {
        // given
        final MinimalEntryComByTitleAsc comparator = new MinimalEntryComByTitleAsc();

        final WatchListEntry steinsGate = new WatchListEntry("Steins;Gate", "http://cdn.myanimelist.net/images/anime/5/73199t.jpg", "http://myanimelist.net/anime/9253");

        // when
        final int result = comparator.compare(null, steinsGate);

        // then
        assertThat(result, equalTo(0));
    }


    @Test
    public void testSecondParameterNull() {
        // given
        final MinimalEntryComByTitleAsc comparator = new MinimalEntryComByTitleAsc();

        final WatchListEntry steinsGate = new WatchListEntry("Steins;Gate", "http://cdn.myanimelist.net/images/anime/5/73199t.jpg", "http://myanimelist.net/anime/9253");

        // when
        final int result = comparator.compare(steinsGate, null);

        // then
        assertThat(result, equalTo(0));
    }


    @Test
    public void testFirstParameterTitleNullOrEmpty() {
        // given
        final MinimalEntryComByTitleAsc comparator = new MinimalEntryComByTitleAsc();

        final WatchListEntry steinsGate = new WatchListEntry("Steins;Gate", "http://cdn.myanimelist.net/images/anime/5/73199t.jpg", "http://myanimelist.net/anime/9253");
        final WatchListEntry gintama = new WatchListEntry("Gintama", "http://cdn.myanimelist.net/images/anime/3/72078t.jpg", "http://myanimelist.net/anime/28977");
        gintama.setTitle("");

        // when
        final int result = comparator.compare(gintama, steinsGate);

        // then
        assertThat(result, equalTo(0));
    }


    @Test
    public void testSecondParameterTitleNullOrEmpty() {
        // given
        final MinimalEntryComByTitleAsc comparator = new MinimalEntryComByTitleAsc();

        final WatchListEntry steinsGate = new WatchListEntry("Steins;Gate", "http://cdn.myanimelist.net/images/anime/5/73199t.jpg", "http://myanimelist.net/anime/9253");
        final WatchListEntry gintama = new WatchListEntry("Gintama", "http://cdn.myanimelist.net/images/anime/3/72078t.jpg", "http://myanimelist.net/anime/28977");
        steinsGate.setTitle("");

        // when
        final int result = comparator.compare(gintama, steinsGate);

        // then
        assertThat(result, equalTo(0));
    }
}

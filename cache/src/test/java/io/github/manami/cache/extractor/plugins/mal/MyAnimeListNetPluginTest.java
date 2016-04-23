package io.github.manami.cache.extractor.plugins.mal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.Anime;

public class MyAnimeListNetPluginTest {

    private static final String DEATH_NOTE_MAL_XML_FILE = "death_note_mal.xml";
    private MyAnimeListNetPlugin sut;
    private String deathNoteRawXml;


    @Before
    public void setUp() throws IOException {
        sut = new MyAnimeListNetPlugin();
        final ClassPathResource resource = new ClassPathResource(DEATH_NOTE_MAL_XML_FILE);
        final StringBuilder strBuilder = new StringBuilder();
        Files.readAllLines(resource.getFile().toPath()).forEach(strBuilder::append);
        deathNoteRawXml = strBuilder.toString();
    }


    @Test
    public void testGetDomain() {
        // given
        final String expectedValue = "myanimelist.net";

        // when
        final String result = sut.getDomain();

        // then
        assertThat(result, not(nullValue()));
        assertThat(result, equalTo(expectedValue));
    }


    @Test
    public void testIsValidInfoLink() throws IOException {
        // given
        final Anime anime = sut.extractAnimeEntry("http://myanimelist.net/anime/1535", deathNoteRawXml);

        // when
        final boolean result = sut.isValidInfoLink();

        // then
        assertThat(result, equalTo(true));
    }


    @Test
    public void testExtractTitle() throws IOException {
        // given
        final String expectedValue = "Death Note";

        // when
        final Anime result = sut.extractAnimeEntry("http://myanimelist.net/anime/1535", deathNoteRawXml);

        // then
        assertThat(result, not(nullValue()));
        assertThat(result.getTitle(), equalTo(expectedValue));
    }


    @Test
    public void testExtractType() throws IOException {
        // given
        final AnimeType expectedValue = AnimeType.TV;

        // when
        final Anime result = sut.extractAnimeEntry("http://myanimelist.net/anime/1535", deathNoteRawXml);

        // then
        assertThat(result, not(nullValue()));
        assertThat(result.getType(), equalTo(expectedValue));
    }


    @Test
    public void testExtractEpisodes() throws IOException {
        // given
        final int expectedValue = 37;

        // when
        final Anime result = sut.extractAnimeEntry("http://myanimelist.net/anime/1535", deathNoteRawXml);

        // then
        assertThat(result, not(nullValue()));
        assertThat(result.getEpisodes(), equalTo(expectedValue));
    }


    @Test
    public void testExtractPictureLink() throws IOException {
        // given
        final String expectedValue = "http://cdn.myanimelist.net/images/anime/9/9453.jpg";

        // when
        final Anime result = sut.extractAnimeEntry("http://myanimelist.net/anime/1535", deathNoteRawXml);

        // then
        assertThat(result, not(nullValue()));
        assertThat(result.getPicture(), equalTo(expectedValue));
    }


    @Test
    public void testExtractThumbnail() throws IOException {
        // given
        final String expectedValue = "http://cdn.myanimelist.net/images/anime/9/9453t.jpg";

        // when
        final Anime result = sut.extractAnimeEntry("http://myanimelist.net/anime/1535", deathNoteRawXml);

        // then
        assertThat(result, not(nullValue()));
        assertThat(result.getThumbnail(), equalTo(expectedValue));
    }


    @Test
    public void testNormalizeInfoLink() throws IOException {
        // given
        final String rawLink = "http://myanimelist.net/anime/1535/Death_Note";
        final String expectedValue = "http://myanimelist.net/anime/1535";

        // when
        final String result = sut.normalizeInfoLink(rawLink);

        // then
        assertThat(result, not(nullValue()));
        assertThat(result, equalTo(expectedValue));
    }


    @Test
    public void testNormalizeInfoLinkFromSearchString() throws IOException {
        // given
        final String rawLink = "http://myanimelist.net/anime/1535/Death_Note?q=death%20note";
        final String expectedValue = "http://myanimelist.net/anime/1535";

        // when
        final String result = sut.normalizeInfoLink(rawLink);

        // then
        assertThat(result, not(nullValue()));
        assertThat(result, equalTo(expectedValue));
    }


    @Test
    public void testExtractRelatedAnimes() throws IOException {
        // given
        final String expectedValue = "http://myanimelist.net/anime/2994";

        // when
        final Anime result = sut.extractAnimeEntry("http://myanimelist.net/anime/1535", deathNoteRawXml);

        // then
        assertThat(result, not(nullValue()));
        assertThat(result.getRelatedAnimes().isEmpty(), equalTo(false));
        assertThat(result.getRelatedAnimes().size(), equalTo(1));
        assertThat(result.getRelatedAnimes().get(0), equalTo(expectedValue));
    }
}

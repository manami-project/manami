package io.github.manami.cache.strategies.headlessbrowser.extractor.anime.mal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;

import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.github.manami.dto.AnimeType;
import io.github.manami.dto.entities.Anime;

public class MyAnimeListNetPluginTest {

    private static final String DEATH_NOTE_MAL_XML_FILE = "death_note_mal.xml";
    private MyAnimeListNetPlugin sut;
    private String deathNoteRawXml;


    @BeforeMethod
    public void setUp() throws IOException {
        sut = new MyAnimeListNetPlugin();
        final ClassPathResource resource = new ClassPathResource(DEATH_NOTE_MAL_XML_FILE);
        final StringBuilder strBuilder = new StringBuilder();
        Files.readAllLines(resource.getFile().toPath()).forEach(strBuilder::append);
        deathNoteRawXml = strBuilder.toString();
    }


    @Test(groups = "unitTest")
    public void testGetDomain() {
        // given
        final String expectedValue = "myanimelist.net";

        // when
        final String result = sut.getDomain();

        // then
        assertNotNull(result);
        assertEquals(result, expectedValue);
    }


    @Test(groups = "unitTest")
    public void testIsValidInfoLink() throws IOException {
        // given
        final Anime anime = sut.extractAnimeEntry("http://myanimelist.net/anime/1535", deathNoteRawXml);

        // when
        final boolean result = sut.isValidInfoLink();

        // then
        assertTrue(result);
    }


    @Test(groups = "unitTest")
    public void testExtractTitle() throws IOException {
        // given
        final String expectedValue = "Death Note";

        // when
        final Anime result = sut.extractAnimeEntry("http://myanimelist.net/anime/1535", deathNoteRawXml);

        // then
        assertNotNull(result);
        assertEquals(result.getTitle(), expectedValue);
    }


    @Test(groups = "unitTest")
    public void testExtractType() throws IOException {
        // given
        final AnimeType expectedValue = AnimeType.TV;

        // when
        final Anime result = sut.extractAnimeEntry("http://myanimelist.net/anime/1535", deathNoteRawXml);

        // then
        assertNotNull(result);
        assertEquals(result.getType(), expectedValue);
    }


    @Test(groups = "unitTest")
    public void testExtractEpisodes() throws IOException {
        // given
        final int expectedValue = 37;

        // when
        final Anime result = sut.extractAnimeEntry("http://myanimelist.net/anime/1535", deathNoteRawXml);

        // then
        assertNotNull(result);
        assertEquals(result.getEpisodes(), expectedValue);
    }


    @Test(groups = "unitTest")
    public void testExtractPictureLink() throws IOException {
        // given
        final String expectedValue = "https://myanimelist.cdn-dena.com/images/anime/9/9453.jpg";

        // when
        final Anime result = sut.extractAnimeEntry("http://myanimelist.net/anime/1535", deathNoteRawXml);

        // then
        assertNotNull(result);
        assertEquals(result.getPicture(), expectedValue);
    }


    @Test(groups = "unitTest")
    public void testExtractThumbnail() throws IOException {
        // given
        final String expectedValue = "https://myanimelist.cdn-dena.com/images/anime/9/9453t.jpg";

        // when
        final Anime result = sut.extractAnimeEntry("http://myanimelist.net/anime/1535", deathNoteRawXml);

        // then
        assertNotNull(result);
        assertEquals(result.getThumbnail(), expectedValue);
    }


    @Test(groups = "unitTest")
    public void testNormalizeInfoLink() throws IOException {
        // given
        final String rawLink = "http://myanimelist.net/anime/1535/Death_Note";
        final String expectedValue = "http://myanimelist.net/anime/1535";

        // when
        final String result = sut.normalizeInfoLink(rawLink);

        // then
        assertNotNull(result);
        assertEquals(result, expectedValue);
    }


    @Test(groups = "unitTest")
    public void testNormalizeInfoLinkFromSearchString() throws IOException {
        // given
        final String rawLink = "http://myanimelist.net/anime/1535/Death_Note?q=death%20note";
        final String expectedValue = "http://myanimelist.net/anime/1535";

        // when
        final String result = sut.normalizeInfoLink(rawLink);

        // then
        assertNotNull(result);
        assertEquals(result, expectedValue);
    }


    @Test(groups = "unitTest")
    public void testExtractRelatedAnimes() throws IOException {
        // given
        final String expectedValue = "http://myanimelist.net/anime/2994";

        // when
        final Anime result = sut.extractAnimeEntry("http://myanimelist.net/anime/1535", deathNoteRawXml);

        // then
        assertNotNull(result); // TODO need to be fixed
        // assertFalse(result.getRelatedAnimes().isEmpty());
        // assertEquals(result.getRelatedAnimes().size(), 1);
        // assertEquals(result.getRelatedAnimes().get(0), expectedValue);
    }
}

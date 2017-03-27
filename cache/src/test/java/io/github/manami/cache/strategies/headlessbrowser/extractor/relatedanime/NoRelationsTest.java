package io.github.manami.cache.strategies.headlessbrowser.extractor.relatedanime;

import static io.github.manami.cache.strategies.headlessbrowser.extractor.TestConst.UNIT_TEST_GROUP;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

import io.github.manami.cache.strategies.headlessbrowser.extractor.relatedanime.mal.MyAnimeListNetRelatedAnimeExtractor;
import io.github.manami.dto.entities.InfoLink;

public class NoRelationsTest {

    @Test(groups = UNIT_TEST_GROUP, description = "Site without related anime.")
    public void extractRelatedAnimesNoRelations() throws IOException {
        // given
        final MyAnimeListNetRelatedAnimeExtractor sut = new MyAnimeListNetRelatedAnimeExtractor();
        final ClassPathResource resource = new ClassPathResource("related_anime_test_no_relations.html");
        final StringBuilder strBuilder = new StringBuilder();
        Files.readAllLines(resource.getFile().toPath()).forEach(strBuilder::append);
        final String rawHtml = strBuilder.toString();

        // when
        final Set<InfoLink> result = sut.extractRelatedAnimes(rawHtml);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test(groups = UNIT_TEST_GROUP, description = "Alternative site without related anime.")
    public void extractRelatedAnimesNoRelations2() throws IOException {
        // given
        final MyAnimeListNetRelatedAnimeExtractor sut = new MyAnimeListNetRelatedAnimeExtractor();
        final ClassPathResource resource = new ClassPathResource("related_anime_test_no_relations_2.html");
        final StringBuilder strBuilder = new StringBuilder();
        Files.readAllLines(resource.getFile().toPath()).forEach(strBuilder::append);
        final String rawHtml = strBuilder.toString();

        // when
        final Set<InfoLink> result = sut.extractRelatedAnimes(rawHtml);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

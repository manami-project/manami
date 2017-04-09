package io.github.manami.cache.strategies.headlessbrowser.extractor.recommendations.mal;

import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;

import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

import io.github.manami.dto.entities.RecommendationList;

public class NoRecommendationsTest {

    @Test(groups = UNIT_TEST_GROUP, description = "Site without recommendations.")
    public void extractRelatedAnimeNoRelations() throws IOException {
        // given
        final MyAnimeListNetRecommendationsExtractor sut = new MyAnimeListNetRecommendationsExtractor();
        final ClassPathResource resource = new ClassPathResource("recommendations_test_no_recommendations.html");
        final StringBuilder strBuilder = new StringBuilder();
        Files.readAllLines(resource.getFile().toPath()).forEach(strBuilder::append);
        final String rawHtml = strBuilder.toString();

        // when
        final RecommendationList result = sut.extractRecommendations(rawHtml);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

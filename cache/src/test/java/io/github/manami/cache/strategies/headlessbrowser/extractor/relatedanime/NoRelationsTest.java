package io.github.manami.cache.strategies.headlessbrowser.extractor.relatedanime;

import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.manami.cache.strategies.headlessbrowser.extractor.relatedanime.mal.MyAnimeListNetRelatedAnimeExtractor;
import io.github.manami.dto.entities.InfoLink;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

public class NoRelationsTest {

  @Test(groups = UNIT_TEST_GROUP, description = "Site without related anime.")
  public void extractRelatedAnimeNoRelations() throws IOException {
    // given
    final MyAnimeListNetRelatedAnimeExtractor sut = new MyAnimeListNetRelatedAnimeExtractor();
    final ClassPathResource resource = new ClassPathResource(
        "related_anime_test_no_relations.html");
    final StringBuilder strBuilder = new StringBuilder();
    Files.readAllLines(resource.getFile().toPath()).forEach(strBuilder::append);
    final String rawHtml = strBuilder.toString();

    // when
    final Set<InfoLink> result = sut.extractRelatedAnime(rawHtml);

    // then
    assertThat(result).isNotNull();
    assertThat(result.isEmpty()).isTrue();
  }


  @Test(groups = UNIT_TEST_GROUP, description = "Alternative site without related anime.")
  public void extractRelatedAnimeNoRelations2() throws IOException {
    // given
    final MyAnimeListNetRelatedAnimeExtractor sut = new MyAnimeListNetRelatedAnimeExtractor();
    final ClassPathResource resource = new ClassPathResource(
        "related_anime_test_no_relations_2.html");
    final StringBuilder strBuilder = new StringBuilder();
    Files.readAllLines(resource.getFile().toPath()).forEach(strBuilder::append);
    final String rawHtml = strBuilder.toString();

    // when
    final Set<InfoLink> result = sut.extractRelatedAnime(rawHtml);

    // then
    assertThat(result).isNotNull();
    assertThat(result.isEmpty()).isTrue();
  }
}

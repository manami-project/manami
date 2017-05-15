package io.github.manami.dto.entities;

import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class RecommendationListTest {

  @Test(groups = UNIT_TEST_GROUP)
  public void testConstructor() {
    // given

    // when
    final RecommendationList sut = new RecommendationList();

    // then
    assertThat(sut.isNotEmpty()).isFalse();
    assertThat(sut.isEmpty()).isTrue();
    assertThat(sut.asList()).isNotNull();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testAddRecommendation() {
    // given
    final RecommendationList sut = new RecommendationList();
    final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");
    final Recommendation recom = new Recommendation(infoLink, 103);

    // when
    sut.addRecommendation(recom);

    // then
    assertThat(sut.isNotEmpty()).isTrue();
    assertThat(sut.isEmpty()).isFalse();
    assertThat(sut.asList()).isNotNull();
    assertThat(sut.containsKey(infoLink)).isTrue();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testAddSameRecommendation() {
    // given
    final RecommendationList sut = new RecommendationList();
    final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");

    final Recommendation recom1 = new Recommendation(infoLink, 103);
    sut.addRecommendation(recom1);

    final Recommendation recom2 = new Recommendation(infoLink, 2);

    // when
    sut.addRecommendation(recom2);

    // then
    assertThat(sut.isNotEmpty()).isTrue();
    assertThat(sut.isEmpty()).isFalse();
    assertThat(sut.asList()).isNotNull();
    assertThat(sut.asList().size()).isEqualTo(1);
    final Integer sum = recom1.getAmount() + recom2.getAmount();
    assertThat(sut.get(infoLink).getAmount()).isEqualTo(sum);
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testAsList() {
    // given
    final RecommendationList sut = new RecommendationList();
    final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");
    final Recommendation recom = new Recommendation(infoLink, 103);
    sut.addRecommendation(recom);

    // when
    sut.asList().clear();

    // then
    assertThat(sut.isNotEmpty()).isTrue();
    assertThat(sut.isEmpty()).isFalse();
    assertThat(sut.asList()).isNotNull();
    assertThat(sut.containsKey(infoLink)).isTrue();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testNotContainsKey() {
    // given
    final RecommendationList sut = new RecommendationList();
    sut.addRecommendation(
        new Recommendation(new InfoLink("http://myanimelist.net/anime/1535"), 103));

    // when
    final boolean result = sut.containsKey(new InfoLink("https://myanimelist.net/anime/32281"));

    // then
    assertThat(result).isFalse();
  }


  @Test(groups = UNIT_TEST_GROUP)
  public void testGet() {
    // given
    final RecommendationList sut = new RecommendationList();
    final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");
    final Recommendation recom = new Recommendation(infoLink, 103);
    sut.addRecommendation(recom);

    // when
    final Recommendation result = sut.get(infoLink);

    // then
    assertThat(sut.isNotEmpty()).isTrue();
    assertThat(sut.isEmpty()).isFalse();
    assertThat(sut.asList()).isNotNull();
    assertThat(sut.containsKey(infoLink)).isTrue();
    assertThat(result).isEqualTo(recom);
  }
}
package io.github.manami.dto.entities;

import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class RecommendationTest {

    @Test(groups = UNIT_TEST_GROUP)
    public void testConstructor() {
        // given
        final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");
        final Integer amount = 5;

        // when
        final Recommendation sut = new Recommendation(infoLink, amount);

        // then
        assertEquals(sut.getInfoLink(), infoLink);
        assertEquals(sut.getAmount(), amount);
    }


    @Test(groups = UNIT_TEST_GROUP)
    public void testChangeOfAmount() {
        // given
        final InfoLink infoLink = new InfoLink("http://myanimelist.net/anime/1535");
        final Integer amount = 5;
        final Integer amountInc = amount + 25;
        final Recommendation sut = new Recommendation(infoLink, amount);

        // when
        sut.setAmount(amountInc);

        // then
        assertEquals(sut.getInfoLink(), infoLink);
        assertEquals(sut.getAmount(), amountInc);
    }
}

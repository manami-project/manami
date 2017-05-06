package io.github.manami.dto.events;

import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

import javafx.stage.Stage;

public class ApplicationContextStartedEventTest {

    @Test(groups = UNIT_TEST_GROUP)
    public void testApplicationContextStartedEventTest() {
        // given
        final Stage stage = mock(Stage.class);

        final ApplicationContextStartedEvent sut = new ApplicationContextStartedEvent(stage);

        // when
        final Stage result = sut.getStage();

        // then
        assertNotNull(sut);
        assertEquals(result, stage);
    }


    @Test(groups = UNIT_TEST_GROUP, expectedExceptions = IllegalArgumentException.class)
    public void testApplicationContextStartedEventTestWithNull() {
        // given

        // when
        new ApplicationContextStartedEvent(null);

        // then
    }
}

package io.github.manami.dto.events;

import static io.github.manami.dto.TestConst.UNIT_TEST_GROUP;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

public class OpenedFileChangedEventTest {

    @Test(groups = UNIT_TEST_GROUP)
    public void testOpenedFileChangedEventTestIsInstantiable() {
        // given

        // when

        // then
        assertNotNull(new OpenedFileChangedEvent());
    }
}

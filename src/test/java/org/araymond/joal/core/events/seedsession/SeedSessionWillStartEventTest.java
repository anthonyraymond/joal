package org.araymond.joal.core.events.seedsession;

import org.junit.Test;

import static org.assertj.core.api.Assertions.fail;

/**
 * Created by raymo on 25/05/2017.
 */
public class SeedSessionWillStartEventTest {


    @Test
    public void shouldBuild() {
        try {
            new SeedSessionWillStartEvent();
        } catch (final Throwable t) {
            fail("Failed to build", t);
        }
    }

}

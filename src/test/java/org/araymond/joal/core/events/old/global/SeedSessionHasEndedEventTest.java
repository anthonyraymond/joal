package org.araymond.joal.core.events.old.global;

import org.junit.Test;

import static org.assertj.core.api.Assertions.fail;

/**
 * Created by raymo on 25/05/2017.
 */
public class SeedSessionHasEndedEventTest {

    @Test
    public void shouldBuild() {
        try {
            new SeedSessionHasEndedEvent();
        } catch (final Throwable t) {
            fail("Failed to build", t);
        }
    }

}

package org.araymond.joal.core.events;

import org.junit.Test;

import static org.assertj.core.api.Assertions.fail;

/**
 * Created by raymo on 25/05/2017.
 */
public class NoMoreTorrentsFileAvailableEventTest {

    @Test
    public void shouldBuild() {
        try {
            new NoMoreTorrentsFileAvailableEvent();
        } catch (final Throwable t) {
            fail("Failed to build", t);
        }
    }

}

package org.araymond.joal.core.events.seedsession;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 25/05/2017.
 */
public class SeedSessionHasEndedEventTest {

    @Test
    public void shouldBuild() {
        new SeedSessionHasEndedEvent();
    }

}

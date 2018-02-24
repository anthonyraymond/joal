package org.araymond.joal.core.events.global.state;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobalSeedStoppedEventTest {

    @Test
    public void shouldBuild() {
        new GlobalSeedStoppedEvent();
    }

}

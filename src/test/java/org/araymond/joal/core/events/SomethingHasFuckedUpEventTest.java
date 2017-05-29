package org.araymond.joal.core.events;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 25/05/2017.
 */
public class SomethingHasFuckedUpEventTest {

    @Test
    public void shouldNotBuildWithoutTorrent() {
        assertThatThrownBy(() -> new SomethingHasFuckedUpEvent(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Exception cannot be null");
    }

    @Test
    public void shouldBuild() {
        final IllegalArgumentException ex = new IllegalArgumentException("test");
        final SomethingHasFuckedUpEvent event = new SomethingHasFuckedUpEvent(ex);

        assertThat(event.getException()).isEqualTo(ex);
    }
}

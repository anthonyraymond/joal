package org.araymond.joal.core.events;

import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 25/05/2017.
 */
public class NoMoreLeechersEventTest {

    @Test
    public void shouldNotBuildWithoutTorrent() {
        assertThatThrownBy(() -> new NoMoreLeechersEvent(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("MockedTorrent cannot be null");
    }

    @Test
    public void shouldBuild() {
        final MockedTorrent torrent = Mockito.mock(MockedTorrent.class);
        final NoMoreLeechersEvent event = new NoMoreLeechersEvent(torrent);

        assertThat(event.getTorrent()).isEqualTo(torrent);
    }
}

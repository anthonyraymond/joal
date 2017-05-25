package org.araymond.joal.core.events.seedsession;

import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 25/05/2017.
 */
public class SeedSessionHasStartedEventTest {

    @Test
    public void shouldNotBuildWithoutTorrent() {
        assertThatThrownBy(() -> new SeedSessionHasStartedEvent(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("BitTorrentClient cannot be null");
    }

    @Test
    public void shouldBuild() {
        final BitTorrentClient client = Mockito.mock(BitTorrentClient.class);
        final SeedSessionHasStartedEvent event = new SeedSessionHasStartedEvent(client);

        assertThat(event.getBitTorrentClient()).isEqualTo(client);
    }
}

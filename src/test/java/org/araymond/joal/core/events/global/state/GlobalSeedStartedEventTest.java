package org.araymond.joal.core.events.global.state;

import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class GlobalSeedStartedEventTest {

    @Test
    public void shouldBuild() {
        final BitTorrentClient client = mock(BitTorrentClient.class);
        final GlobalSeedStartedEvent event = new GlobalSeedStartedEvent(client);

        assertThat(event.getBitTorrentClient()).isEqualTo(client);
    }

}

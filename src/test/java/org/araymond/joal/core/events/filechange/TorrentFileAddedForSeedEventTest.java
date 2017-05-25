package org.araymond.joal.core.events.filechange;

import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 25/05/2017.
 */
public class TorrentFileAddedForSeedEventTest {

    @Test
    public void shouldNotBuildWithoutTorrent() {
        assertThatThrownBy(() -> new TorrentFileAddedForSeedEvent(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("MockedTorrent cannot be null");
    }

    @Test
    public void shouldBuild() {
        final MockedTorrent torrent = Mockito.mock(MockedTorrent.class);
        final TorrentFileAddedForSeedEvent event = new TorrentFileAddedForSeedEvent(torrent);

        assertThat(event.getTorrent()).isEqualTo(torrent);
    }
}

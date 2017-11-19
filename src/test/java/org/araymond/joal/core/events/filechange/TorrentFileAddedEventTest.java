package org.araymond.joal.core.events.filechange;

import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 25/05/2017.
 */
public class TorrentFileAddedEventTest {

    @Test
    public void shouldNotBuildWithoutTorrent() {
        assertThatThrownBy(() -> new TorrentFileAddedEvent(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("MockedTorrent cannot be null");
    }

    @Test
    public void shouldBuild() {
        final MockedTorrent torrent = Mockito.mock(MockedTorrent.class);
        final TorrentFileAddedEvent event = new TorrentFileAddedEvent(torrent);

        assertThat(event.getTorrent()).isEqualTo(torrent);
    }
}

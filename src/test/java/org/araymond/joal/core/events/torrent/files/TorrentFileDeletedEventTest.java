package org.araymond.joal.core.events.torrent.files;

import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class TorrentFileDeletedEventTest {

    @Test
    public void shouldBuild() {
        final MockedTorrent torrent = mock(MockedTorrent.class);
        final TorrentFileDeletedEvent event = new TorrentFileDeletedEvent(torrent);

        assertThat(event.getTorrent()).isEqualTo(torrent);
    }

}

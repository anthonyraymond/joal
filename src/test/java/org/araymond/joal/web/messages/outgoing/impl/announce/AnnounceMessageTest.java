package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 26/06/2017.
 */
public class AnnounceMessageTest {

    static TorrentWithStats mockTorrentWithStat() {
        final MockedTorrent mt = Mockito.mock(MockedTorrent.class);
        Mockito.doReturn("hash").when(mt).getHexInfoHash();
        Mockito.doReturn("name").when(mt).getName();
        Mockito.doReturn(19246846L).when(mt).getSize();

        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        Mockito.doReturn(mt).when(torrent).getTorrent();
        Mockito.doReturn(12030L).when(torrent).getCurrentRandomSpeedInBytes();

        return torrent;
    }

    @Test
    public void shouldNotBuildWithoutTorrent() {
        assertThatThrownBy(() -> new DefaultAnnouncePayload(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Torrent must not be null.");
    }

    @Test
    public void shouldBuild() {
        final TorrentWithStats torrent = mockTorrentWithStat();
        final DefaultAnnouncePayload message = new DefaultAnnouncePayload(torrent);

        assertThat(message.getId()).isEqualTo(torrent.getTorrent().getHexInfoHash());
        assertThat(message.getName()).isEqualTo(torrent.getTorrent().getName());
        assertThat(message.getSize()).isEqualTo(torrent.getTorrent().getSize());
        assertThat(message.getCurrentSpeed()).isEqualTo(torrent.getCurrentRandomSpeedInBytes());
    }

    private static class DefaultAnnouncePayload extends AnnouncePayload {

        protected DefaultAnnouncePayload(final TorrentWithStats torrent) {
            super(torrent);
        }
    }

}

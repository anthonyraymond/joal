package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.events.announce.AnnouncerEvent;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 26/06/2017.
 */
public class AnnouncePayloadTest {

    public static TorrentWithStats mockTorrentWithStat() {
        final MockedTorrent mt = Mockito.mock(MockedTorrent.class);
        Mockito.when(mt.getHexInfoHash()).thenReturn("hash");
        Mockito.when(mt.getName()).thenReturn("name");
        Mockito.when(mt.getSize()).thenReturn(19246846L);

        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        Mockito.when(torrent.getTorrent()).thenReturn(mt);
        Mockito.when(torrent.getCurrentRandomSpeedInBytes()).thenReturn(12030L);
        Mockito.when(torrent.getInterval()).thenReturn(1800);
        Mockito.when(torrent.getLeechers()).thenReturn(53);
        Mockito.when(torrent.getSeeders()).thenReturn(645);

        return torrent;
    }

    @Test
    public void shouldNotBuildWithoutAnnouncer() {
        assertThatThrownBy(() -> new DefaultAnnouncePayload(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("AnnouncerEvent must not be null.");
    }

    @Test
    public void shouldBuild() {
        final TorrentWithStats torrent = mockTorrentWithStat();
        final AnnouncerEvent event = Mockito.mock(AnnouncerEvent.class);
        Mockito.when(event.getTorrent()).thenReturn(torrent);
        final DefaultAnnouncePayload message = new DefaultAnnouncePayload(event);

        assertThat(message.getId()).isEqualTo(torrent.getTorrent().getHexInfoHash());
        assertThat(message.getName()).isEqualTo(torrent.getTorrent().getName());
        assertThat(message.getSize()).isEqualTo(torrent.getTorrent().getSize());
        assertThat(message.getCurrentSpeed()).isEqualTo(torrent.getCurrentRandomSpeedInBytes());
    }

    private static class DefaultAnnouncePayload extends AnnouncePayload {

        protected DefaultAnnouncePayload(final AnnouncerEvent event) {
            super(event);
        }
    }

}

package org.araymond.joal.core.events.announce;

import com.turn.ttorrent.common.protocol.TrackerMessage;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 25/06/2017.
 */
public class AnnouncerHasAnnouncedEventTest {


    @Test
    public void shouldNotBuildWithoutTorrent() {
        assertThatThrownBy(() -> new AnnouncerHasAnnouncedEvent(null, 1800, 30, 254))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("TorrentWithStats cannot be null");
    }

    @Test
    public void shouldBuild() {
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final AnnouncerHasAnnouncedEvent message = new AnnouncerHasAnnouncedEvent(torrent, 1800, 30, 254);

        assertThat(message.getTorrent()).isEqualTo(torrent);
        assertThat(message.getInterval()).isEqualTo(1800);
        assertThat(message.getSeeders()).isEqualTo(30);
        assertThat(message.getLeechers()).isEqualTo(254);
    }
}

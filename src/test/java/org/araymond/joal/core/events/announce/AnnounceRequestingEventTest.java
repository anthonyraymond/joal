package org.araymond.joal.core.events.announce;

import com.turn.ttorrent.common.protocol.TrackerMessage;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 25/05/2017.
 */
public class AnnounceRequestingEventTest {

    @Test
    public void shouldNotBuildWithoutRequestEvent() {
        assertThatThrownBy(() -> new AnnounceRequestingEvent(null, Mockito.mock(TorrentWithStats.class)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("RequestEvent cannot be null");
    }

    @Test
    public void shouldNotBuildWithoutTorrent() {
        assertThatThrownBy(() -> new AnnounceRequestingEvent(RequestEvent.COMPLETED, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("TorrentWithStats cannot be null");
    }

    @Test
    public void shouldBuild() {
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final AnnounceRequestingEvent event = new AnnounceRequestingEvent(RequestEvent.COMPLETED, torrent);

        assertThat(event.getEvent()).isEqualTo(RequestEvent.COMPLETED);
        assertThat(event.getTorrent()).isEqualTo(torrent);
    }

}

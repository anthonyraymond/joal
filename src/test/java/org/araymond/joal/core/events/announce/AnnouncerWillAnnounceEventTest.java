package org.araymond.joal.core.events.announce;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.ttorent.client.announce.Announcer;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 25/05/2017.
 */
public class AnnouncerWillAnnounceEventTest {

    @Test
    public void shouldNotBuildWithoutRequestEvent() {
        assertThatThrownBy(() -> new AnnouncerWillAnnounceEvent(Mockito.mock(Announcer.class), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("RequestEvent must not be null");
    }

    @Test
    public void shouldNotBuildWithoutAnnouncer() {
        assertThatThrownBy(() -> new AnnouncerWillAnnounceEvent(null, RequestEvent.COMPLETED))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Announcer must not be null");
    }

    @Test
    public void shouldBuild() {
        final Announcer announcer = Mockito.mock(Announcer.class);
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        Mockito.when(announcer.getSeedingTorrent()).thenReturn(torrent);
        final AnnouncerWillAnnounceEvent event = new AnnouncerWillAnnounceEvent(announcer, RequestEvent.COMPLETED);

        assertThat(event.getEvent()).isEqualTo(RequestEvent.COMPLETED);
        assertThat(event.getTorrent()).isEqualTo(torrent);
    }

}

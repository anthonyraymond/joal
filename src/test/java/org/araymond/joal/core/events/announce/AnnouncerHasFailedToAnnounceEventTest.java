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
public class AnnouncerHasFailedToAnnounceEventTest {

    @Test
    public void shouldNotBuildWithoutRequestEvent() {
        assertThatThrownBy(() -> new AnnouncerHasFailedToAnnounceEvent(null, Mockito.mock(TorrentWithStats.class), "this is an error"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("RequestEvent cannot be null");
    }

    @Test
    public void shouldNotBuildWithoutTorrent() {
        assertThatThrownBy(() -> new AnnouncerHasFailedToAnnounceEvent(TrackerMessage.AnnounceRequestMessage.RequestEvent.COMPLETED, null, "this is an error"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("TorrentWithStats cannot be null");
    }

    @Test
    public void shouldNotBuildWithoutError() {
        assertThatThrownBy(() -> new AnnouncerHasFailedToAnnounceEvent(TrackerMessage.AnnounceRequestMessage.RequestEvent.COMPLETED, Mockito.mock(TorrentWithStats.class), " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Error message cannot be null or empty.");
    }

    @Test
    public void shouldBuild() {
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final AnnouncerHasFailedToAnnounceEvent event = new AnnouncerHasFailedToAnnounceEvent(TrackerMessage.AnnounceRequestMessage.RequestEvent.COMPLETED, torrent, "this is an error");

        assertThat(event.getEvent()).isEqualTo(TrackerMessage.AnnounceRequestMessage.RequestEvent.COMPLETED);
        assertThat(event.getTorrent()).isEqualTo(torrent);
        assertThat(event.getError()).isEqualTo("this is an error");
    }

}

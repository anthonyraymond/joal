package org.araymond.joal.core.events.announce;

import com.turn.ttorrent.common.protocol.TrackerMessage;
import org.araymond.joal.core.ttorent.client.announce.Announcer;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.araymond.joal.web.messages.outgoing.impl.announce.AnnouncePayloadTest;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 25/06/2017.
 */
public class AnnouncerHasFailedToAnnounceEventTest {

    @Test
    public void shouldNotBuildWithoutError() {
        assertThatThrownBy(() -> new AnnouncerHasFailedToAnnounceEvent(Mockito.mock(Announcer.class), " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Error message cannot be null or empty.");
    }

    @Test
    public void shouldBuild() {
        final Announcer announcer = Mockito.mock(Announcer.class);
        final TorrentWithStats torrent = AnnouncePayloadTest.mockTorrentWithStat();
        Mockito.when(announcer.getSeedingTorrent()).thenReturn(torrent);
        Mockito.when(announcer.getAnnounceHistory()).thenReturn(Collections.EMPTY_LIST);
        final AnnouncerHasFailedToAnnounceEvent event = new AnnouncerHasFailedToAnnounceEvent(announcer, "this is an error");

        assertThat(event.getAnnounceHistory()).isEqualTo(Collections.EMPTY_LIST);
        assertThat(event.getTorrent()).isEqualTo(torrent);
        assertThat(event.getError()).isEqualTo("this is an error");
    }

}

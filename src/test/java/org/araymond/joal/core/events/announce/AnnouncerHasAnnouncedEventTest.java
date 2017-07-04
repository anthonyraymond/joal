package org.araymond.joal.core.events.announce;

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
public class AnnouncerHasAnnouncedEventTest {

    @Test
    public void shouldBuild() {
        final TorrentWithStats torrent = AnnouncePayloadTest.mockTorrentWithStat();
        Mockito.when(torrent.getInterval()).thenReturn(1800);
        Mockito.when(torrent.getSeeders()).thenReturn(30);
        Mockito.when(torrent.getLeechers()).thenReturn(254);
        final Announcer announcer = Mockito.mock(Announcer.class);
        Mockito.when(announcer.getSeedingTorrent()).thenReturn(torrent);
        Mockito.when(announcer.getAnnounceHistory()).thenReturn(Collections.EMPTY_LIST);
        final AnnouncerHasAnnouncedEvent message = new AnnouncerHasAnnouncedEvent(announcer);

        assertThat(message.getTorrent()).isEqualTo(torrent);
        assertThat(message.getInterval()).isEqualTo(1800);
        assertThat(message.getSeeders()).isEqualTo(30);
        assertThat(message.getLeechers()).isEqualTo(254);
    }
}

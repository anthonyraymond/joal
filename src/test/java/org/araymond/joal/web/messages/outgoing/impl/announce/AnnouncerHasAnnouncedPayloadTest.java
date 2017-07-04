package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.events.announce.AnnouncerHasAnnouncedEvent;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 25/06/2017.
 */
public class AnnouncerHasAnnouncedPayloadTest {

    @Test
    public void shouldBuild() {
        final AnnouncerHasAnnouncedEvent event = Mockito.mock(AnnouncerHasAnnouncedEvent.class);
        final TorrentWithStats torrent = AnnouncePayloadTest.mockTorrentWithStat();
        Mockito.when(event.getTorrent()).thenReturn(torrent);
        Mockito.when(event.getAnnounceHistory()).thenReturn(Collections.EMPTY_LIST);
        Mockito.when(event.getInterval()).thenReturn(1800);
        Mockito.when(event.getSeeders()).thenReturn(1648);
        Mockito.when(event.getLeechers()).thenReturn(9);

        final AnnouncerHasAnnouncedPayload message = new AnnouncerHasAnnouncedPayload(event);

        assertThat(message.getInterval()).isEqualTo(1800);
        assertThat(message.getSeeders()).isEqualTo(1648);
        assertThat(message.getLeechers()).isEqualTo(9);
    }

}

package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.events.announce.AnnouncerHasFailedToAnnounceEvent;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 25/06/2017.
 */
public class AnnouncerHasFailedToAnnouncePayloadTest {

    @Test
    public void shouldBuild() {
        final AnnouncerHasFailedToAnnounceEvent event = Mockito.mock(AnnouncerHasFailedToAnnounceEvent.class);
        final TorrentWithStats torrent = AnnouncePayloadTest.mockTorrentWithStat();
        Mockito.when(event.getTorrent()).thenReturn(torrent);
        Mockito.when(event.getAnnounceHistory()).thenReturn(Collections.EMPTY_LIST);
        Mockito.when(event.getError()).thenReturn("this is an error");

        final AnnouncerHasFailedToAnnouncePayload message = new AnnouncerHasFailedToAnnouncePayload(event);

        assertThat(message.getError()).isEqualTo("this is an error");
    }


}

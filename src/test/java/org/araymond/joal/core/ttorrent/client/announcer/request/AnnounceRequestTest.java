package org.araymond.joal.core.ttorrent.client.announcer.request;

import com.turn.ttorrent.common.protocol.TrackerMessage;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

public class AnnounceRequestTest {

    @Test
    public void shouldCreateStart() {
        final AnnounceRequest announceRequest = AnnounceRequest.createStart(Mockito.mock(Announcer.class));
        assertThat(announceRequest.getEvent()).isEqualTo(RequestEvent.STARTED);
    }

    @Test
    public void shouldCreateRegular() {
        final AnnounceRequest announceRequest = AnnounceRequest.createRegular(Mockito.mock(Announcer.class));
        assertThat(announceRequest.getEvent()).isEqualTo(RequestEvent.NONE);
    }

    @Test
    public void shouldCreateStop() {
        final AnnounceRequest announceRequest = AnnounceRequest.createStop(Mockito.mock(Announcer.class));
        assertThat(announceRequest.getEvent()).isEqualTo(RequestEvent.STOPPED);
    }

}

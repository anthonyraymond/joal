package org.araymond.joal.core.ttorrent.client.announcer.request;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

public class AnnounceRequestTest {

    @Test
    public void shouldCreateStart() {
        final Announcer announcer = Mockito.mock(Announcer.class);
        final AnnounceRequest announceRequest = AnnounceRequest.createStart(announcer);
        assertThat(announceRequest.getEvent()).isEqualTo(RequestEvent.STARTED);
        assertThat(announceRequest.getInfoHash()).isEqualTo(announcer.getTorrentInfoHash());
    }

    @Test
    public void shouldCreateRegular() {
        final Announcer announcer = Mockito.mock(Announcer.class);
        final AnnounceRequest announceRequest = AnnounceRequest.createRegular(announcer);
        assertThat(announceRequest.getEvent()).isEqualTo(RequestEvent.NONE);
        assertThat(announceRequest.getInfoHash()).isEqualTo(announcer.getTorrentInfoHash());
    }

    @Test
    public void shouldCreateStop() {
        final Announcer announcer = Mockito.mock(Announcer.class);
        final AnnounceRequest announceRequest = AnnounceRequest.createStop(announcer);
        assertThat(announceRequest.getEvent()).isEqualTo(RequestEvent.STOPPED);
        assertThat(announceRequest.getInfoHash()).isEqualTo(announcer.getTorrentInfoHash());
    }

}

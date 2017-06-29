package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.fail;

/**
 * Created by raymo on 25/06/2017.
 */
public class AnnouncerWillAnnounceMessageTest {

    @Test
    public void shouldNotBuildWithoutTorrent() {
        assertThatThrownBy(() -> new AnnouncerWillAnnouncePayload(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Torrent must not be null.");
    }

    @Test
    public void shouldBuild() {
        final TorrentWithStats torrent = AnnounceMessageTest.mockTorrentWithStat();
        try {
            final AnnouncerWillAnnouncePayload message = new AnnouncerWillAnnouncePayload(torrent);
        } catch (final Throwable t) {
            fail();
        }
    }

}

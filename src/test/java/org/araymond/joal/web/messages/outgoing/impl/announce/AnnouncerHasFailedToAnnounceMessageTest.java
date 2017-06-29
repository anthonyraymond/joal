package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 25/06/2017.
 */
public class AnnouncerHasFailedToAnnounceMessageTest {

    @Test
    public void shouldNotBuildWithoutTorrent() {
        assertThatThrownBy(() -> new AnnouncerHasFailedToAnnouncePayload(null, "this is an error"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Torrent must not be null.");
    }

    @Test
    public void shouldNotBuildWithoutMessage() {
        final TorrentWithStats torrent = AnnounceMessageTest.mockTorrentWithStat();
        assertThatThrownBy(() -> new AnnouncerHasFailedToAnnouncePayload(torrent, " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error message must not be null or empty.");
    }

    @Test
    public void shouldBuild() {
        final TorrentWithStats torrent = AnnounceMessageTest.mockTorrentWithStat();
        final AnnouncerHasFailedToAnnouncePayload message = new AnnouncerHasFailedToAnnouncePayload(torrent, "this is an error");

        assertThat(message.getError()).isEqualTo("this is an error");
    }


}

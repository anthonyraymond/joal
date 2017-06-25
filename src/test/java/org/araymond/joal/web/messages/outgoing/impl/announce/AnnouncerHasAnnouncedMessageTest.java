package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.araymond.joal.web.messages.outgoing.OutgoingMessageTypes;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 25/06/2017.
 */
public class AnnouncerHasAnnouncedMessageTest {

    @Test
    public void shouldNotBuildWithoutTorrent() {
        assertThatThrownBy(() -> new AnnouncerHasAnnouncedMessage(null, 1800, 30, 254))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Torrent must not be null.");
    }

    @Test
    public void shouldBuild() {
        final TorrentWithStats torrent = AnnounceMessageTest.mockTorrentWithStat();
        final AnnouncerHasAnnouncedMessage message = new AnnouncerHasAnnouncedMessage(torrent, 1800, 30, 254);

        assertThat(message.getType()).isEqualTo(OutgoingMessageTypes.ANNOUNCER_HAS_ANNOUNCED);
        assertThat(message.getInterval()).isEqualTo(1800);
        assertThat(message.getSeeders()).isEqualTo(30);
        assertThat(message.getLeechers()).isEqualTo(254);
    }

}

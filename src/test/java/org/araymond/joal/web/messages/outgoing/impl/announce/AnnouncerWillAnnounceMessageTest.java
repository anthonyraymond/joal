package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.araymond.joal.web.messages.outgoing.OutgoingMessageTypes;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 25/06/2017.
 */
public class AnnouncerWillAnnounceMessageTest {

    @Test
    public void shouldNotBuildWithoutTorrent() {
        assertThatThrownBy(() -> new AnnouncerWillAnnounceMessage(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Torrent must not be null.");
    }

    @Test
    public void shouldBuild() {
        final MockedTorrent torrent = Mockito.mock(MockedTorrent.class);
        final AnnouncerWillAnnounceMessage message = new AnnouncerWillAnnounceMessage(torrent);

        assertThat(message.getType()).isEqualTo(OutgoingMessageTypes.ANNOUNCER_WILL_ANNOUNCE);
        assertThat(message.getTorrent()).isEqualTo(torrent);
    }

}

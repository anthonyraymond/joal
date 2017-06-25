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
public class AnnouncerHasStoppedMessageTest {

    @Test
    public void shouldNotBuildWithoutTorrent() {
        assertThatThrownBy(() -> new AnnouncerHasStoppedMessage(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Torrent must not be null.");
    }

    @Test
    public void shouldBuild() {
        final MockedTorrent torrent = Mockito.mock(MockedTorrent.class);
        final AnnouncerHasStoppedMessage message = new AnnouncerHasStoppedMessage(torrent);

        assertThat(message.getType()).isEqualTo(OutgoingMessageTypes.ANNOUNCER_HAS_STOPPED);
        assertThat(message.getTorrent()).isEqualTo(torrent);
    }

}

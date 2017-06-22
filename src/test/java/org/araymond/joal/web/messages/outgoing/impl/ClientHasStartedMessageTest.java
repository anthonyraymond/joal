package org.araymond.joal.web.messages.outgoing.impl;

import org.araymond.joal.web.messages.outgoing.OutgoingMessageTypes;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 22/06/2017.
 */
public class ClientHasStartedMessageTest {

    @Test
    public void shouldNotBuildWithNullClient() {
        assertThatThrownBy(() -> new ClientHasStartedMessage(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Client must not be null or empty.");
    }

    @Test
    public void shouldNotBuildWithBlankClient() {
        assertThatThrownBy(() -> new ClientHasStartedMessage(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Client must not be null or empty.");
    }

    @Test
    public void shouldBuild() {
        final ClientHasStartedMessage message = new ClientHasStartedMessage("BitTorrent/799(255043872)(43296)");

        assertThat(message.getClient()).isEqualTo("BitTorrent/799(255043872)(43296)");
        assertThat(message.getType()).isEqualTo(OutgoingMessageTypes.CLIENT_HAS_STARTED);
    }

}

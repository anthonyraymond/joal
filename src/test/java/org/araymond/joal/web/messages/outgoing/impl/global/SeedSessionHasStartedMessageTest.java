package org.araymond.joal.web.messages.outgoing.impl.global;

import org.araymond.joal.web.messages.outgoing.OutgoingMessageTypes;
import org.araymond.joal.web.messages.outgoing.impl.global.SeedSessionHasStartedMessage;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 22/06/2017.
 */
public class SeedSessionHasStartedMessageTest {

    @Test
    public void shouldNotBuildWithNullClient() {
        assertThatThrownBy(() -> new SeedSessionHasStartedMessage(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Client must not be null or empty.");
    }

    @Test
    public void shouldNotBuildWithBlankClient() {
        assertThatThrownBy(() -> new SeedSessionHasStartedMessage(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Client must not be null or empty.");
    }

    @Test
    public void shouldBuild() {
        final SeedSessionHasStartedMessage message = new SeedSessionHasStartedMessage("BitTorrent/799(255043872)(43296)");

        assertThat(message.getClient()).isEqualTo("BitTorrent/799(255043872)(43296)");
        assertThat(message.getType()).isEqualTo(OutgoingMessageTypes.SEED_SESSION_HAS_STARTED);
    }

}

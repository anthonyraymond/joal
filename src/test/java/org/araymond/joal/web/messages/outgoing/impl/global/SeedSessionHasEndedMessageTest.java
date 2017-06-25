package org.araymond.joal.web.messages.outgoing.impl.global;

import org.araymond.joal.web.messages.outgoing.OutgoingMessageTypes;
import org.araymond.joal.web.messages.outgoing.impl.global.SeedSessionHasEndedMessage;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 24/06/2017.
 */
public class SeedSessionHasEndedMessageTest {

    @Test
    public void shouldBuild() {
        final SeedSessionHasEndedMessage message = new SeedSessionHasEndedMessage();

        assertThat(message.getType()).isEqualTo(OutgoingMessageTypes.SEED_SESSION_HAS_ENDED);
    }

}

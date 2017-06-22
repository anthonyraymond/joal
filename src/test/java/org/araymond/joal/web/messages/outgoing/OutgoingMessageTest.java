package org.araymond.joal.web.messages.outgoing;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 22/06/2017.
 */
public class OutgoingMessageTest {


    @Test
    public void shouldNotBuildWithNullType() {
        assertThatThrownBy(() -> new DefaultOutgoingMessage(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Type must not be null or empty.");
    }

    @Test
    public void shouldBuild() {
        final OutgoingMessage message = new DefaultOutgoingMessage(OutgoingMessageTypes.CLIENT_HAS_STARTED);

        assertThat(message.getType()).isEqualTo(OutgoingMessageTypes.CLIENT_HAS_STARTED);
    }

    private static final class DefaultOutgoingMessage extends OutgoingMessage {

        protected DefaultOutgoingMessage(final OutgoingMessageTypes type) {
            super(type);
        }
    }

}

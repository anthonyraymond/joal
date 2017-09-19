package org.araymond.joal.web.messages.outgoing.impl.global;

import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Created by raymo on 24/06/2017.
 */
public class SeedSessionHasEndedMessageTest {

    @Test
    public void shouldBuild() {
        try {
            final SeedSessionHasEndedPayload message = new SeedSessionHasEndedPayload();
        } catch (final Throwable t) {
            fail();
        }
    }

}

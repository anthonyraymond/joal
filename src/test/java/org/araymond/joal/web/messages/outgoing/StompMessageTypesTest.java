package org.araymond.joal.web.messages.outgoing;

import org.araymond.joal.web.messages.outgoing.impl.global.SeedSessionHasEndedPayload;
import org.araymond.joal.web.messages.outgoing.impl.global.SeedSessionHasStartedPayload;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 29/06/2017.
 */
public class StompMessageTypesTest {

    @Test
    public void shouldFailIfTypeIsNotMapped() {
        assertThatThrownBy(() -> StompMessageTypes.typeFor(new NonExistingPayloadType()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(NonExistingPayloadType.class.getSimpleName() + " is not mapped with a StompMessageType.");
    }

    @Test
    public void shouldMapSeedSessionHasStartedPayload() {
        assertThat(StompMessageTypes.typeFor(SeedSessionHasStartedPayload.class))
                .isEqualTo(StompMessageTypes.SEED_SESSION_HAS_STARTED);
    }

    @Test
    public void shouldMapSeedSessionHasEndedPayload() {
        assertThat(StompMessageTypes.typeFor(SeedSessionHasEndedPayload.class))
                .isEqualTo(StompMessageTypes.SEED_SESSION_HAS_ENDED);
    }

    /*@Test
    public void shouldMapAnnouncerHasStartedPayload() {
        assertThat(StompMessageTypes.typeFor(AnnouncerHasStartedPayload.class))
                .isEqualTo(StompMessageTypes.ANNOUNCER_HAS_STARTED);
    }

    @Test
    public void shouldMapAnnouncerHasStoppedPayload() {
        assertThat(StompMessageTypes.typeFor(AnnouncerHasStoppedPayload.class))
                .isEqualTo(StompMessageTypes.ANNOUNCER_HAS_STOPPED);
    }

    @Test
    public void shouldMapAnnouncerWillAnnouncePayload() {
        assertThat(StompMessageTypes.typeFor(AnnouncerWillAnnouncePayload.class))
                .isEqualTo(StompMessageTypes.ANNOUNCER_WILL_ANNOUNCE);
    }

    @Test
    public void shouldMapAnnouncerHasAnnouncedPayload() {
        assertThat(StompMessageTypes.typeFor(AnnouncerHasAnnouncedPayload.class))
                .isEqualTo(StompMessageTypes.ANNOUNCER_HAS_ANNOUNCED);
    }

    @Test
    public void shouldMapAnnouncerHasFailedToAnnouncePayload() {
        assertThat(StompMessageTypes.typeFor(AnnouncerHasFailedToAnnouncePayload.class))
                .isEqualTo(StompMessageTypes.ANNOUNCER_HAS_FAILED_TO_ANNOUNCE);
    }*/

    private static final class NonExistingPayloadType implements MessagePayload {
    }

}

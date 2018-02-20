package org.araymond.joal.core.ttorrent.client.announcer.request;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

public class SuccessAnnounceResponseTest {

    public static SuccessAnnounceResponse createOne() {
        return new SuccessAnnounceResponse(1800, 164, 12);
    }

    @Test
    public void shouldBuild() {
        final SuccessAnnounceResponse successAnnounceResponse = new SuccessAnnounceResponse(1800, 164, 12);

        assertThat(successAnnounceResponse.getInterval()).isEqualTo(1800);
        assertThat(successAnnounceResponse.getSeeders()).isEqualTo(164);
        assertThat(successAnnounceResponse.getLeechers()).isEqualTo(12);
    }

}

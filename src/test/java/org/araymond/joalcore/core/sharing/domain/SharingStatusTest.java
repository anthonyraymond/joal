package org.araymond.joalcore.core.sharing.domain;

import org.araymond.joalcore.core.sharing.domain.exceptions.InvalidChangeOfStateException;
import org.junit.jupiter.api.Test;

import static org.araymond.joalcore.core.sharing.domain.SharingStatus.Paused;
import static org.araymond.joalcore.core.sharing.domain.SharingStatus.Sharing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SharingStatusTest {

    @Test
    public void shouldBeAbleToPauseWhenSharing() {
        assertThat(SharingStatus.Sharing.pause()).isEqualTo(Paused);
    }

    @Test
    public void shouldNotBeAbleToPauseWhenPaused() {
        assertThatThrownBy(Paused::pause)
                .isInstanceOf(InvalidChangeOfStateException.class)
                .hasMessageContaining("already Paused");
    }

    @Test
    public void shouldBeAbleToShareWhenPaused() {
        assertThat(Paused.share()).isEqualTo(Sharing);
    }

    @Test
    public void shouldNotBeAbleToShareWhenSharing() {
        assertThatThrownBy(Sharing::share)
                .isInstanceOf(InvalidChangeOfStateException.class)
                .hasMessageContaining("already Sharing");
    }

}
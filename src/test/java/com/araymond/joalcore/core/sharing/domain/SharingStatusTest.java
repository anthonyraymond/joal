package com.araymond.joalcore.core.sharing.domain;

import com.araymond.joalcore.core.sharing.domain.exceptions.InvalidChangeOfStateException;
import org.junit.jupiter.api.Test;

import static com.araymond.joalcore.core.sharing.domain.SharingStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SharingStatusTest {

    @Test
    public void shouldBeAbleToPauseWhenDownloading() {
        assertThat(SharingStatus.Downloading.pause()).isEqualTo(Paused);
    }

    @Test
    public void shouldBeAbleToPauseWhenSeeding() {
        assertThat(SharingStatus.Seeding.pause()).isEqualTo(Paused);
    }

    @Test
    public void shouldNotBeAbleToPauseWhenPause() {
        assertThatThrownBy(Paused::pause)
                .isInstanceOf(InvalidChangeOfStateException.class)
                .hasMessageContaining("already Paused");
    }


    @Test
    public void shouldBeAbleToDownloadWhenPaused() {
        assertThat(Paused.download()).isEqualTo(Downloading);
    }

    @Test
    public void shouldBeAbleToDownloadWhenSeeding() {
        assertThat(SharingStatus.Seeding.download()).isEqualTo(Downloading);
    }

    @Test
    public void shouldNotBeAbleToDownloadWhenDownload() {
        assertThatThrownBy(Downloading::download)
                .isInstanceOf(InvalidChangeOfStateException.class)
                .hasMessageContaining("already Downloading");
    }


    @Test
    public void shouldBeAbleToSeedWhenPaused() {
        assertThat(Paused.seed()).isEqualTo(Seeding);
    }

    @Test
    public void shouldBeAbleToSeedWhenDownloading() {
        assertThat(Downloading.seed()).isEqualTo(Seeding);
    }

    @Test
    public void shouldNotBeAbleToSeedWhenSeeding() {
        assertThatThrownBy(Seeding::seed)
                .isInstanceOf(InvalidChangeOfStateException.class)
                .hasMessageContaining("already Seeding");
    }
    
}
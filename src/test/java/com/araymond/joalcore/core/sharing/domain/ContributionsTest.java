package com.araymond.joalcore.core.sharing.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContributionsTest {

    @Test
    public void shouldAddDownloadAndDecrementLeft() {
        var contrib = new Contributions(Contribution.ZERO, new Left(5275));

        assertThat(contrib
                .add(new DownloadAmount(75))
                .add(new DownloadAmount(200))
        ).isEqualTo(new Contributions(
                new Contribution(new DownloadAmount(275), new UploadAmount(0)),
                new Contribution(new DownloadAmount(275), new UploadAmount(0)),
                new Left(5000)
        ));
    }

    @Test
    public void shouldShouldNotAddMoreDownloadThanLeft() {
        var contrib = new Contributions(Contribution.ZERO, new Left(500))
                .add(new DownloadAmount(501));

        assertThat(contrib).isEqualTo(new Contributions(
                new Contribution(new DownloadAmount(500), new UploadAmount(0)),
                new Contribution(new DownloadAmount(500), new UploadAmount(0)),
                Left.ZERO
        ));
        assertThat(contrib.isFullyDownloaded()).isTrue();
    }

    @Test
    public void shouldAddUpload() {
        var contrib = new Contributions(Contribution.ZERO, new Left(500))
                .add(new UploadAmount(203))
                .add(new UploadAmount(27));

        assertThat(contrib).isEqualTo(new Contributions(
                new Contribution(new DownloadAmount(0), new UploadAmount(230)),
                new Contribution(new DownloadAmount(0), new UploadAmount(230)),
                new Left(500)
        ));
    }

}
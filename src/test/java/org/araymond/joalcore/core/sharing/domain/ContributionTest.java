package org.araymond.joalcore.core.sharing.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContributionTest {

    @Test
    public void shouldAddDownload() {
        var contrib = Contribution.ZERO
                .add(new DownloadAmount(50))
                .add(new DownloadAmount(200));

        assertThat(contrib.downloaded()).isEqualTo(new DownloadAmount(250));
    }

    @Test
    public void shouldAddUpload() {
        var contrib = Contribution.ZERO
                .add(new UploadAmount(50))
                .add(new UploadAmount(200));

        assertThat(contrib.uploaded()).isEqualTo(new UploadAmount(250));
    }
}
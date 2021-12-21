package org.araymond.joal.core.bandwith;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TorrentSeedStatsTest {

    public static TorrentSeedStats createOne() {
        return createOne(0);
    }

    public static TorrentSeedStats createOne(final long uploaded) {
        final TorrentSeedStats stats = new TorrentSeedStats();
        stats.addUploaded(uploaded);
        return stats;
    }

    @Test
    public void shouldBuild() {
        final TorrentSeedStats stats = new TorrentSeedStats();

        assertThat(stats.getUploaded()).isEqualTo(0);
        assertThat(stats.getDownloaded()).isEqualTo(0);
        assertThat(stats.getLeft()).isEqualTo(0);
    }

    @Test
    public void shouldAddUploaded() {
        final TorrentSeedStats stats = new TorrentSeedStats();
        assertThat(stats.getUploaded()).isEqualTo(0);

        stats.addUploaded(50);
        assertThat(stats.getUploaded()).isEqualTo(50);
    }

}

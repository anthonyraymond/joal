package org.araymond.joal.core.torrent;

import org.araymond.joal.core.config.AppConfiguration;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

/**
 * Created by raymo on 14/05/2017.
 */
public class BandwidthManagerTest {

    @Test
    public void shouldNotBuildWithoutConfigProvider() {
        assertThatThrownBy(() -> new BandwidthManager(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Cannot build without ConfigProvider.");
    }

    @Test
    public void shouldBuild() {
        final JoalConfigProvider configProvider = Mockito.mock(JoalConfigProvider.class);

        try {
            final BandwidthManager bandwidthManager = new BandwidthManager(configProvider);
        } catch (final Throwable ignored) {
            fail("should build");
        }
    }

    @Test
    public void shouldGenerateRandomizedSpeed() {
        final JoalConfigProvider configProvider = Mockito.mock(JoalConfigProvider.class);
        final AppConfiguration conf = Mockito.mock(AppConfiguration.class);
        Mockito.when(configProvider.get()).thenReturn(conf);
        Mockito.when(conf.getMinUploadRate()).thenReturn(180);
        Mockito.when(conf.getMaxUploadRate()).thenReturn(190);

        final BandwidthManager bandwidthManager = new BandwidthManager(configProvider);

        for (int i = 0; i < 20; ++i) {
            assertThat(bandwidthManager.generateRandomizedSpeedInBytes())
                    .isBetween(
                            (long) conf.getMinUploadRate() * 1024,
                            (long) conf.getMaxUploadRate() * 1024
                    );
        }
    }

    @Test
    public void shouldNotExceedMaxValue() {
        final JoalConfigProvider configProvider = Mockito.mock(JoalConfigProvider.class);
        final AppConfiguration conf = Mockito.mock(AppConfiguration.class);
        Mockito.when(configProvider.get()).thenReturn(conf);
        Mockito.when(conf.getMinUploadRate()).thenReturn(0);
        Mockito.when(conf.getMaxUploadRate()).thenReturn(1);

        final BandwidthManager bandwidthManager = new BandwidthManager(configProvider);

        for (int i = 0; i < 500; ++i) {
            assertThat(bandwidthManager.generateRandomizedSpeedInBytes())
                    .isBetween(
                            (long) conf.getMinUploadRate() * 1024,
                            (long) conf.getMaxUploadRate() * 1024
                    );
        }
    }

    @Test
    public void shouldGenerateRandomizedSpeedEvenWithZero() {
        final JoalConfigProvider configProvider = Mockito.mock(JoalConfigProvider.class);
        final AppConfiguration conf = Mockito.mock(AppConfiguration.class);
        Mockito.when(configProvider.get()).thenReturn(conf);
        Mockito.when(conf.getMinUploadRate()).thenReturn(0);
        Mockito.when(conf.getMaxUploadRate()).thenReturn(0);

        final BandwidthManager bandwidthManager = new BandwidthManager(configProvider);

        for (int i = 0; i < 20; ++i) {
            assertThat(bandwidthManager.generateRandomizedSpeedInBytes()).isEqualTo(0);
        }
    }

    @Test
    public void shouldIncrementTorrentStat() throws InterruptedException {
        final JoalConfigProvider configProvider = Mockito.mock(JoalConfigProvider.class);
        final AppConfiguration conf = Mockito.mock(AppConfiguration.class);
        Mockito.when(configProvider.get()).thenReturn(conf);
        Mockito.when(conf.getMinUploadRate()).thenReturn(180);
        Mockito.when(conf.getMaxUploadRate()).thenReturn(190);

        final int updateInterval = 4;
        final BandwidthManager bandwidthManager = new BandwidthManager(configProvider, updateInterval);
        final TorrentWithStats torrent = new TorrentWithStats(Mockito.mock(MockedTorrent.class));
        bandwidthManager.registerTorrent(torrent);

        bandwidthManager.start();
        Thread.sleep(5);
        bandwidthManager.stop();

        assertThat(torrent.getUploaded())
                .isBetween(
                        (long) conf.getMinUploadRate() * 1024 / (1000 / updateInterval),
                        (long) conf.getMaxUploadRate() * 1024 / (1000 / updateInterval)
                );
    }


    @Test
    public void shouldIncrementTorrentStatAndSplitSpeed() throws InterruptedException {
        final JoalConfigProvider configProvider = Mockito.mock(JoalConfigProvider.class);
        final AppConfiguration conf = Mockito.mock(AppConfiguration.class);
        Mockito.when(configProvider.get()).thenReturn(conf);
        Mockito.when(conf.getMinUploadRate()).thenReturn(160);
        Mockito.when(conf.getMaxUploadRate()).thenReturn(180);

        final int updateInterval = 4;
        final BandwidthManager bandwidthManager = new BandwidthManager(configProvider, updateInterval);
        final TorrentWithStats torrent = new TorrentWithStats(Mockito.mock(MockedTorrent.class));
        final TorrentWithStats torrent2 = new TorrentWithStats(Mockito.mock(MockedTorrent.class));
        final TorrentWithStats torrent3 = new TorrentWithStats(Mockito.mock(MockedTorrent.class));
        final TorrentWithStats torrent4 = new TorrentWithStats(Mockito.mock(MockedTorrent.class));
        bandwidthManager.registerTorrent(torrent);
        bandwidthManager.registerTorrent(torrent2);
        bandwidthManager.registerTorrent(torrent3);
        bandwidthManager.registerTorrent(torrent4);

        bandwidthManager.start();
        Thread.sleep(5);
        bandwidthManager.stop();

        assertThat(torrent.getUploaded())
                .isBetween(
                        (long) conf.getMinUploadRate() * 1024 / (1000 / updateInterval) / 4,
                        (long) conf.getMaxUploadRate() * 1024 / (1000 / updateInterval) / 4
                );
        assertThat(torrent2.getUploaded())
                .isBetween(
                        (long) conf.getMinUploadRate() * 1024 / (1000 / updateInterval) / 4,
                        (long) conf.getMaxUploadRate() * 1024 / (1000 / updateInterval) / 4
                );
        assertThat(torrent3.getUploaded())
                .isBetween(
                        (long) conf.getMinUploadRate() * 1024 / (1000 / updateInterval) / 4,
                        (long) conf.getMaxUploadRate() * 1024 / (1000 / updateInterval) / 4
                );
        assertThat(torrent4.getUploaded())
                .isBetween(
                        (long) conf.getMinUploadRate() * 1024 / (1000 / updateInterval) / 4,
                        (long) conf.getMaxUploadRate() * 1024 / (1000 / updateInterval) / 4
                );
    }

}

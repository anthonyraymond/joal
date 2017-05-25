package org.araymond.joal.core.ttorent.client.bandwidth;

import org.araymond.joal.core.config.AppConfiguration;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.*;

/**
 * Created by raymo on 14/05/2017.
 */
public class BandwidthDispatcherTest {

    @Test
    public void shouldNotBuildWithoutConfigProvider() {
        assertThatThrownBy(() -> new BandwidthDispatcher(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Cannot build without ConfigProvider.");
    }

    @Test
    public void shouldBuild() {
        final JoalConfigProvider configProvider = Mockito.mock(JoalConfigProvider.class);
        final AppConfiguration conf = Mockito.mock(AppConfiguration.class);
        Mockito.when(configProvider.get()).thenReturn(conf);
        Mockito.when(conf.getSimultaneousSeed()).thenReturn(3);

        try {
            final BandwidthDispatcher bandwidthDispatcher = new BandwidthDispatcher(configProvider);
        } catch (final Throwable throwable) {
            fail("should build", throwable);
        }
    }

    @Test
    public void shouldGenerateRandomizedSpeed() {
        final JoalConfigProvider configProvider = Mockito.mock(JoalConfigProvider.class);
        final AppConfiguration conf = Mockito.mock(AppConfiguration.class);
        Mockito.when(configProvider.get()).thenReturn(conf);
        Mockito.when(conf.getSimultaneousSeed()).thenReturn(3);
        Mockito.when(conf.getMinUploadRate()).thenReturn(180);
        Mockito.when(conf.getMaxUploadRate()).thenReturn(190);

        final BandwidthDispatcher bandwidthDispatcher = new BandwidthDispatcher(configProvider);

        for (int i = 0; i < 20; ++i) {
            assertThat(bandwidthDispatcher.generateRandomizedSpeedInBytes())
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
        Mockito.when(conf.getSimultaneousSeed()).thenReturn(3);
        Mockito.when(conf.getMinUploadRate()).thenReturn(0);
        Mockito.when(conf.getMaxUploadRate()).thenReturn(1);

        final BandwidthDispatcher bandwidthDispatcher = new BandwidthDispatcher(configProvider);

        for (int i = 0; i < 500; ++i) {
            assertThat(bandwidthDispatcher.generateRandomizedSpeedInBytes())
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
        Mockito.when(conf.getSimultaneousSeed()).thenReturn(3);
        Mockito.when(conf.getMinUploadRate()).thenReturn(0);
        Mockito.when(conf.getMaxUploadRate()).thenReturn(0);

        final BandwidthDispatcher bandwidthDispatcher = new BandwidthDispatcher(configProvider);

        for (int i = 0; i < 20; ++i) {
            assertThat(bandwidthDispatcher.generateRandomizedSpeedInBytes()).isEqualTo(0);
        }
    }

    @Test
    public void shouldIncrementTorrentStat() throws InterruptedException {
        final JoalConfigProvider configProvider = Mockito.mock(JoalConfigProvider.class);
        final AppConfiguration conf = Mockito.mock(AppConfiguration.class);
        Mockito.when(configProvider.get()).thenReturn(conf);
        Mockito.when(conf.getSimultaneousSeed()).thenReturn(3);
        Mockito.when(conf.getMinUploadRate()).thenReturn(180);
        Mockito.when(conf.getMaxUploadRate()).thenReturn(190);

        final int updateInterval = 4;
        final BandwidthDispatcher bandwidthDispatcher = new BandwidthDispatcher(configProvider, updateInterval);
        final TorrentWithStats torrent = new TorrentWithStats(Mockito.mock(MockedTorrent.class));
        bandwidthDispatcher.registerTorrent(torrent);

        bandwidthDispatcher.start();
        Thread.sleep(5);
        bandwidthDispatcher.stop();

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
        Mockito.when(conf.getSimultaneousSeed()).thenReturn(3);
        Mockito.when(conf.getMinUploadRate()).thenReturn(160);
        Mockito.when(conf.getMaxUploadRate()).thenReturn(180);

        final int updateInterval = 4;
        final BandwidthDispatcher bandwidthDispatcher = new BandwidthDispatcher(configProvider, updateInterval);
        final TorrentWithStats torrent = new TorrentWithStats(Mockito.mock(MockedTorrent.class));
        final TorrentWithStats torrent2 = new TorrentWithStats(Mockito.mock(MockedTorrent.class));
        final TorrentWithStats torrent3 = new TorrentWithStats(Mockito.mock(MockedTorrent.class));
        final TorrentWithStats torrent4 = new TorrentWithStats(Mockito.mock(MockedTorrent.class));
        bandwidthDispatcher.registerTorrent(torrent);
        bandwidthDispatcher.registerTorrent(torrent2);
        bandwidthDispatcher.registerTorrent(torrent3);
        bandwidthDispatcher.registerTorrent(torrent4);

        bandwidthDispatcher.start();
        Thread.sleep(5);
        bandwidthDispatcher.stop();

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

    @Test
    public void shouldNotIncrementUnregisteredTorrent() throws InterruptedException {
        final JoalConfigProvider configProvider = Mockito.mock(JoalConfigProvider.class);
        final AppConfiguration conf = Mockito.mock(AppConfiguration.class);
        Mockito.when(configProvider.get()).thenReturn(conf);
        Mockito.when(conf.getSimultaneousSeed()).thenReturn(3);
        Mockito.when(conf.getMinUploadRate()).thenReturn(180);
        Mockito.when(conf.getMaxUploadRate()).thenReturn(190);

        final int updateInterval = 4;
        final BandwidthDispatcher bandwidthDispatcher = new BandwidthDispatcher(configProvider, updateInterval);
        final TorrentWithStats torrent = new TorrentWithStats(Mockito.mock(MockedTorrent.class));
        bandwidthDispatcher.registerTorrent(torrent);

        bandwidthDispatcher.start();
        Thread.sleep(5);

        assertThat(torrent.getUploaded())
                .isBetween(
                        (long) conf.getMinUploadRate() * 1024 / (1000 / updateInterval),
                        (long) conf.getMaxUploadRate() * 1024 / (1000 / updateInterval)
                );

        bandwidthDispatcher.unRegisterTorrent(torrent);
        Thread.sleep(5);
        bandwidthDispatcher.stop();

        assertThat(torrent.getUploaded())
                .isBetween(
                        (long) conf.getMinUploadRate() * 1024 / (1000 / updateInterval),
                        (long) conf.getMaxUploadRate() * 1024 / (1000 / updateInterval)
                );
    }

}

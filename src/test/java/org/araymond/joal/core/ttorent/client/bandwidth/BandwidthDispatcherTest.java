package org.araymond.joal.core.ttorent.client.bandwidth;

import org.araymond.joal.core.config.AppConfiguration;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.assertj.core.data.Percentage;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

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
            new BandwidthDispatcher(configProvider);
        } catch (final Throwable throwable) {
            fail("should build", throwable);
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
        bandwidthDispatcher.onAnnouncerStart(null, torrent);

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
        bandwidthDispatcher.onAnnouncerStart(null, torrent);
        bandwidthDispatcher.onAnnouncerStart(null, torrent2);
        bandwidthDispatcher.onAnnouncerStart(null, torrent3);
        bandwidthDispatcher.onAnnouncerStart(null, torrent4);

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
        bandwidthDispatcher.onAnnouncerStart(null, torrent);

        bandwidthDispatcher.start();
        Thread.sleep(5);

        assertThat(torrent.getUploaded())
                .isBetween(
                        (long) conf.getMinUploadRate() * 1024 / (1000 / updateInterval),
                        (long) conf.getMaxUploadRate() * 1024 / (1000 / updateInterval)
                );

        bandwidthDispatcher.onAnnouncerStop(null, torrent);
        Thread.sleep(5);
        bandwidthDispatcher.stop();

        assertThat(torrent.getUploaded())
                .isBetween(
                        (long) conf.getMinUploadRate() * 1024 / (1000 / updateInterval),
                        (long) conf.getMaxUploadRate() * 1024 / (1000 / updateInterval)
                );
    }

    @Test
    public void shouldProvideLargeStandardDeviationOverTime() throws InterruptedException {
        // If all values are provided by a simple rand, after a short time the average value converge to a average value
        //  and it result in upload graph being a straight line

        final JoalConfigProvider configProvider = Mockito.mock(JoalConfigProvider.class);
        final AppConfiguration conf = Mockito.mock(AppConfiguration.class);
        Mockito.when(configProvider.get()).thenReturn(conf);
        Mockito.when(conf.getSimultaneousSeed()).thenReturn(3);
        Mockito.when(conf.getMinUploadRate()).thenReturn(50);
        Mockito.when(conf.getMaxUploadRate()).thenReturn(130);

        final int updateInterval = 1;
        final int numberOfTorrents = 50;
        final Collection<TorrentWithStatsCountdown> torrents = new ArrayList<>(numberOfTorrents);
        IntStream.range(0, numberOfTorrents)
                .parallel()
                .mapToObj(i -> {
                    final BandwidthDispatcher bandwidthDispatcher = new BandwidthDispatcher(configProvider, updateInterval);
                    final TorrentWithStatsCountdown torrent = new TorrentWithStatsCountdown(bandwidthDispatcher);
                    bandwidthDispatcher.onAnnouncerStart(null, torrent);
                    torrents.add(torrent);
                    bandwidthDispatcher.start();
                    return torrent;
                })
                .forEach(torrent -> {
                    try {
                        torrent.await();
                    } catch (final InterruptedException ignored) {
                        fail("not supposed to happen");
                    }
                });

        final double expectedAverage = (conf.getMinUploadRate() + conf.getMaxUploadRate()) / 2;
        final double realAverage = torrents.stream()
                .mapToDouble(TorrentWithStatsCountdown::getAverage)
                .average()
                .orElseThrow(() -> new IllegalStateException("Impossible to reach this case."));
        assertThat(realAverage)
                .as("Average speed should be close median between min and max speed.")
                .isCloseTo(expectedAverage, Percentage.withPercentage(10));

        final double expectedMinimumStandardDeviation = (conf.getMaxUploadRate() - conf.getMinUploadRate()) / 2 / 2;
        final double realStandardDeviation = torrents.stream()
                .mapToDouble(TorrentWithStatsCountdown::getStandardDeviation)
                .average()
                .orElseThrow(() -> new IllegalStateException("Impossible to reach this case."));
        assertThat(realStandardDeviation)
                .as("deviation should be greater than a fourth the distance between min and max speed.")
                .isGreaterThan(expectedMinimumStandardDeviation);
    }

    private static final class TorrentWithStatsCountdown extends TorrentWithStats {
        private final CountDownLatch countDown = new CountDownLatch(50);
        private final BandwidthDispatcher dispatcher;
        private final List<Long> values;

        private TorrentWithStatsCountdown(final BandwidthDispatcher dispatcher) {
            super(Mockito.mock(MockedTorrent.class));
            this.dispatcher = dispatcher;
            values = new ArrayList<>();
        }

        @Override
        void addUploaded(final Long uploaded) {
            super.addUploaded(uploaded);
            this.values.add(uploaded);
            this.countDown.countDown();
            dispatcher.onAnnounceRequesting(null, this);
            if (this.countDown.getCount() == 0) {
                this.dispatcher.stop();
            }
        }

        void await() throws InterruptedException {
            this.countDown.await();
        }

        double getAverage() {
            return values.stream()
                    .mapToDouble(Long::doubleValue)
                    .average()
                    .orElseThrow(() -> new IllegalStateException("Cannot happen, but we need to remove intellij warning."));
        }

        double getStandardDeviation() {
            final double average = this.getAverage();
            return Math.sqrt(values.stream()
                    .mapToDouble(Long::doubleValue)
                    .map((a) -> Math.pow(a - average, 2))
                    .sum() / values.size());
        }
    }

}

package org.araymond.joal.core.ttorent.client.bandwidth;

import org.araymond.joal.core.config.AppConfiguration;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.ttorent.client.announce.Announcer;
import org.assertj.core.data.Percentage;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

/**
 * Created by raymo on 14/05/2017.
 */
public class BandwidthDispatcherTest {

    private JoalConfigProvider prepareMockedConfProvider(final Long minRate, final Long maxRate) {
        final JoalConfigProvider configProvider = Mockito.mock(JoalConfigProvider.class);
        final AppConfiguration conf = Mockito.mock(AppConfiguration.class);
        Mockito.when(configProvider.get()).thenReturn(conf);
        Mockito.when(conf.getSimultaneousSeed()).thenReturn(3);
        Mockito.when(conf.getMinUploadRate()).thenReturn(minRate);
        Mockito.when(conf.getMaxUploadRate()).thenReturn(maxRate);

        return configProvider;
    }

    @Test
    public void shouldNotBuildWithoutConfigProvider() {
        assertThatThrownBy(() -> new BandwidthDispatcher(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Cannot build without ConfigProvider.");
    }

    @Test
    public void shouldBuild() {
        final JoalConfigProvider configProvider = prepareMockedConfProvider(130L, 180L);

        try {
            new BandwidthDispatcher(configProvider);
        } catch (final Throwable throwable) {
            fail("should build", throwable);
        }
    }

    @Test
    public void shouldRefreshTorrentCurrentSpeedWhenOnAnnouncerHasAnnouncedSuccessIsCalled() {
        final JoalConfigProvider configProvider = prepareMockedConfProvider(130L, 180L);

        final BandwidthDispatcher bandwidthDispatcher = Mockito.spy(new BandwidthDispatcher(configProvider, 1));
        final TorrentWithStats torrentWithStats = Mockito.mock(TorrentWithStats.class);
        final Announcer announcer = Mockito.mock(Announcer.class);
        Mockito.when(announcer.getSeedingTorrent()).thenReturn(torrentWithStats);

        bandwidthDispatcher.onAnnounceSuccess(announcer);
        Mockito.verify(torrentWithStats, Mockito.times(1)).refreshRandomSpeedInBytes(Matchers.anyLong());
        bandwidthDispatcher.onAnnounceSuccess(announcer);
        Mockito.verify(torrentWithStats, Mockito.times(2)).refreshRandomSpeedInBytes(Matchers.anyLong());
        bandwidthDispatcher.onAnnounceSuccess(announcer);
        Mockito.verify(torrentWithStats, Mockito.times(3)).refreshRandomSpeedInBytes(Matchers.anyLong());
    }

    @Test
    public void shouldAddTorrentToListOnAnnounceStarts() {
        final JoalConfigProvider configProvider = prepareMockedConfProvider(130L, 180L);
        final BandwidthDispatcher bandwidthDispatcher = new BandwidthDispatcher(configProvider, 1);

        assertThat(bandwidthDispatcher.getTorrentCount()).isEqualTo(0);
        bandwidthDispatcher.onAnnouncerStart(Mockito.mock(Announcer.class));
        assertThat(bandwidthDispatcher.getTorrentCount()).isEqualTo(1);
    }

    @Test
    public void shouldRemoveTorrentToListOnAnnounceStarts() {
        final JoalConfigProvider configProvider = prepareMockedConfProvider(130L, 180L);
        final BandwidthDispatcher bandwidthDispatcher = new BandwidthDispatcher(configProvider, 1);

        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final Announcer announcer = Mockito.mock(Announcer.class);
        Mockito.when(announcer.getSeedingTorrent()).thenReturn(torrent);
        bandwidthDispatcher.onAnnouncerStart(announcer);
        bandwidthDispatcher.onAnnouncerStop(announcer);
        assertThat(bandwidthDispatcher.getTorrentCount()).isEqualTo(0);
    }

    @Test
    public void shouldIncrementTorrentStat() throws InterruptedException {
        final JoalConfigProvider configProvider = prepareMockedConfProvider(130L, 180L);

        final int updateInterval = 4;
        final BandwidthDispatcher bandwidthDispatcher = new BandwidthDispatcher(configProvider, updateInterval);
        final CountDownTorrentWithStats torrent = new CountDownTorrentWithStats(Mockito.mock(MockedTorrent.class), new CountDownLatch(1));
        torrent.setLeechers(10);
        torrent.setSeeders(10);
        final Announcer announcer = Mockito.mock(Announcer.class);
        Mockito.when(announcer.getSeedingTorrent()).thenReturn(torrent);

        bandwidthDispatcher.onAnnouncerStart(announcer);
        bandwidthDispatcher.onAnnounceSuccess(announcer);

        bandwidthDispatcher.start();
        torrent.getCountDownLatch().await(20, TimeUnit.MILLISECONDS);
        bandwidthDispatcher.stop();

        assertThat(torrent.getUploaded())
                .isBetween(
                        configProvider.get().getMinUploadRate() * 1000 / (1000 / updateInterval),
                        configProvider.get().getMaxUploadRate() * 1000 / (1000 / updateInterval)
                );
    }

    @Test
    public void shouldIncrementTorrentStatAndSplitSpeed() throws InterruptedException {
        final JoalConfigProvider configProvider = prepareMockedConfProvider(130L, 180L);

        final int updateInterval = 4;
        final BandwidthDispatcher bandwidthDispatcher = new BandwidthDispatcher(configProvider, updateInterval);
        final Collection<CountDownTorrentWithStats> torrents = new ArrayList<>();
        for (int i = 0; i < 4; ++i) {
            final CountDownTorrentWithStats torrent = new CountDownTorrentWithStats(Mockito.mock(MockedTorrent.class), new CountDownLatch(1));
            torrent.setLeechers(10);
            torrent.setSeeders(10);
            final Announcer announcer = Mockito.mock(Announcer.class);
            Mockito.when(announcer.getSeedingTorrent()).thenReturn(torrent);
            torrents.add(torrent);
            bandwidthDispatcher.onAnnouncerStart(announcer);
            bandwidthDispatcher.onAnnounceSuccess(announcer);
        }

        bandwidthDispatcher.start();
        torrents.stream()
                .map(CountDownTorrentWithStats::getCountDownLatch)
                .forEach(cdl -> {
                    try {
                        cdl.await(20, TimeUnit.MILLISECONDS);
                    } catch (final InterruptedException e) {
                        throw new IllegalStateException("Fuck has happened while awaiting CountDownLatch");
                    }
                });
        bandwidthDispatcher.stop();

        final AppConfiguration conf = configProvider.get();
        for (final TorrentWithStats t : torrents) {
            assertThat(t.getUploaded())
                    .isBetween(
                            conf.getMinUploadRate() * 1000 / (1000 / updateInterval) / 4,
                            conf.getMaxUploadRate() * 1000 / (1000 / updateInterval) / 4
                    );
        }
    }

    @Test
    public void shouldNotIncrementUnregisteredTorrent() throws InterruptedException {
        final JoalConfigProvider configProvider = prepareMockedConfProvider(130L, 180L);

        final int updateInterval = 4;
        final BandwidthDispatcher bandwidthDispatcher = new BandwidthDispatcher(configProvider, updateInterval);
        final CountDownTorrentWithStats torrent = new CountDownTorrentWithStats(Mockito.mock(MockedTorrent.class), new CountDownLatch(1));
        torrent.setLeechers(10);
        torrent.setSeeders(10);
        final Announcer announcer = Mockito.mock(Announcer.class);
        Mockito.when(announcer.getSeedingTorrent()).thenReturn(torrent);

        bandwidthDispatcher.onAnnouncerStart(announcer);
        bandwidthDispatcher.onAnnounceSuccess(announcer);
        bandwidthDispatcher.onAnnouncerStop(announcer);

        bandwidthDispatcher.start();
        torrent.getCountDownLatch().await(20, TimeUnit.MILLISECONDS);
        bandwidthDispatcher.stop();

        assertThat(torrent.getUploaded())
                .isEqualTo(0);
    }

    @Test
    public void shouldProvideLargeStandardDeviationOverTime() throws InterruptedException {
        // If all values are provided by a simple rand, after a short time the average value converge to a average value
        //  and it result in upload graph being a straight line



        final JoalConfigProvider configProvider = prepareMockedConfProvider(50L, 130L);

        final int updateInterval = 1;
        final int numberOfTorrents = 50;
        final Collection<TorrentWithStatsHistoryAware> torrents = new ArrayList<>(numberOfTorrents);
        IntStream.range(0, numberOfTorrents)
                .parallel()
                .forEach(i -> {
                    final BandwidthDispatcher bandwidthDispatcher = new BandwidthDispatcher(configProvider, updateInterval);
                    final TorrentWithStatsHistoryAware torrent = new TorrentWithStatsHistoryAware();
                    torrent.setLeechers(10);
                    torrent.setSeeders(10);
                    final Announcer announcer = Mockito.mock(Announcer.class);
                    Mockito.when(announcer.getSeedingTorrent()).thenReturn(torrent);
                    bandwidthDispatcher.onAnnouncerStart(announcer);
                    torrents.add(torrent);
                    for (; i < 50; ++i) {
                        bandwidthDispatcher.onAnnounceSuccess(announcer);
                    }
                });

        final double expectedAverage = (configProvider.get().getMinUploadRate() + configProvider.get().getMaxUploadRate()) / 2.0;
        final double realAverage = torrents.stream()
                .mapToDouble(TorrentWithStatsHistoryAware::getAverage)
                .average()
                .orElseThrow(() -> new IllegalStateException("Impossible to reach this case."));
        assertThat(realAverage)
                .as("Average speed should be close median between min and max speed.")
                .isCloseTo(expectedAverage, Percentage.withPercentage(10));

        final double expectedMinimumStandardDeviation = (configProvider.get().getMaxUploadRate() - configProvider.get().getMinUploadRate()) / 2.0 / 2.0;
        final double realStandardDeviation = torrents.stream()
                .mapToDouble(TorrentWithStatsHistoryAware::getStandardDeviation)
                .average()
                .orElseThrow(() -> new IllegalStateException("Impossible to reach this case."));
        assertThat(realStandardDeviation)
                .as("deviation should be greater than a fourth the distance between min and max speed.")
                .isGreaterThan(expectedMinimumStandardDeviation);
    }

    @Test
    public void shouldNotAddSpeedIfTorrentHasNoLeechers() throws InterruptedException {
        final JoalConfigProvider configProvider = prepareMockedConfProvider(130L, 180L);

        final int updateInterval = 2;
        final BandwidthDispatcher bandwidthDispatcher = new BandwidthDispatcher(configProvider, updateInterval);
        final CountDownTorrentWithStats torrent = new CountDownTorrentWithStats(Mockito.mock(MockedTorrent.class), new CountDownLatch(1));
        torrent.setLeechers(0);
        torrent.setSeeders(100);
        final Announcer announcer = Mockito.mock(Announcer.class);
        Mockito.when(announcer.getSeedingTorrent()).thenReturn(torrent);
        bandwidthDispatcher.onAnnouncerStart(announcer);
        bandwidthDispatcher.onAnnounceSuccess(announcer);

        bandwidthDispatcher.start();
        torrent.getCountDownLatch().await(20, TimeUnit.MILLISECONDS);
        bandwidthDispatcher.stop();

        assertThat(torrent.getUploaded()).isEqualTo(0);
    }

    @Test
    public void shouldNotAddSpeedIfTorrentHasNoSeeders() throws InterruptedException {
        final JoalConfigProvider configProvider = prepareMockedConfProvider(130L, 180L);

        final int updateInterval = 2;
        final BandwidthDispatcher bandwidthDispatcher = new BandwidthDispatcher(configProvider, updateInterval);
        final CountDownTorrentWithStats torrent = new CountDownTorrentWithStats(Mockito.mock(MockedTorrent.class), new CountDownLatch(1));
        torrent.setLeechers(100);
        torrent.setSeeders(0);
        final Announcer announcer = Mockito.mock(Announcer.class);
        Mockito.when(announcer.getSeedingTorrent()).thenReturn(torrent);
        bandwidthDispatcher.onAnnouncerStart(announcer);
        bandwidthDispatcher.onAnnounceSuccess(announcer);

        bandwidthDispatcher.start();
        torrent.getCountDownLatch().await(20, TimeUnit.MILLISECONDS);
        bandwidthDispatcher.stop();

        assertThat(torrent.getUploaded()).isEqualTo(0);
    }

    @Test
    public void shouldDivideSpeedOnlyForTorrentThatHaveSpeedGreaterThan0() throws InterruptedException {
        final JoalConfigProvider configProvider = prepareMockedConfProvider(180L, 180L);

        final int updateInterval = 4;
        final BandwidthDispatcher bandwidthDispatcher = new BandwidthDispatcher(configProvider, updateInterval);
        final CountDownTorrentWithStats torrent = new CountDownTorrentWithStats(Mockito.mock(MockedTorrent.class), new CountDownLatch(1));
        torrent.setLeechers(100);
        torrent.setSeeders(100);
        final Announcer announcer = Mockito.mock(Announcer.class);
        Mockito.when(announcer.getSeedingTorrent()).thenReturn(torrent);
        bandwidthDispatcher.onAnnouncerStart(announcer);
        bandwidthDispatcher.onAnnounceSuccess(announcer);
        final CountDownTorrentWithStats torrent2 = new CountDownTorrentWithStats(Mockito.mock(MockedTorrent.class), new CountDownLatch(1));
        torrent2.setLeechers(0);
        torrent2.setSeeders(0);
        final Announcer announcer2 = Mockito.mock(Announcer.class);
        Mockito.when(announcer2.getSeedingTorrent()).thenReturn(torrent2);
        bandwidthDispatcher.onAnnouncerStart(announcer2);
        bandwidthDispatcher.onAnnounceSuccess(announcer2);

        bandwidthDispatcher.start();
        torrent.getCountDownLatch().await(20, TimeUnit.MILLISECONDS);
        torrent2.getCountDownLatch().await(20, TimeUnit.MILLISECONDS);
        bandwidthDispatcher.stop();

        final AppConfiguration conf = configProvider.get();
        assertThat(torrent.getUploaded()).isEqualTo(conf.getMinUploadRate() * 1000 / (1000 / updateInterval));
        assertThat(torrent2.getUploaded()).isEqualTo(0);
    }

    @Test
    public void shouldNotFailIfNoneOfTorrentsHaveLeechersNorSeeders() throws InterruptedException {
        // prevent crash: ArithmeticException: divide by zero
        final JoalConfigProvider configProvider = prepareMockedConfProvider(130L, 180L);

        final int updateInterval = 2;
        final BandwidthDispatcher bandwidthDispatcher = new BandwidthDispatcher(configProvider, updateInterval);
        final Collection<CountDownTorrentWithStats> torrents = new ArrayList<>();
        for (int i = 0; i < 4; ++i) {
            final CountDownTorrentWithStats torrent = new CountDownTorrentWithStats(Mockito.mock(MockedTorrent.class), new CountDownLatch(1));
            torrent.setLeechers(0);
            torrent.setSeeders(0);
            final Announcer announcer = Mockito.mock(Announcer.class);
            Mockito.when(announcer.getSeedingTorrent()).thenReturn(torrent);
            torrents.add(torrent);
            bandwidthDispatcher.onAnnouncerStart(announcer);
            bandwidthDispatcher.onAnnounceSuccess(announcer);
        }

        bandwidthDispatcher.start();
        torrents.stream()
                .map(CountDownTorrentWithStats::getCountDownLatch)
                .forEach(cdl -> {
                    try {
                        cdl.await(20, TimeUnit.MILLISECONDS);
                    } catch (final InterruptedException e) {
                        throw new IllegalStateException("Fuck has happened while awaiting CountDownLatch");
                    }
                });
        bandwidthDispatcher.stop();

        for (final TorrentWithStats t : torrents) {
            assertThat(t.getUploaded()).isEqualTo(0);
        }
    }

    private static final class TorrentWithStatsHistoryAware extends TorrentWithStats {
        private final List<Long> values;

        private TorrentWithStatsHistoryAware() {
            super(Mockito.mock(MockedTorrent.class));
            values = new ArrayList<>();
        }

        @Override
        void refreshRandomSpeedInBytes(final Long speedInBytes) {
            super.refreshRandomSpeedInBytes(speedInBytes / 1000);
            this.values.add(speedInBytes / 1000);
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

        @Override
        public boolean equals(final Object o) {
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }

    private static final class CountDownTorrentWithStats extends TorrentWithStats {
        private static CountDownLatch cdl;

        CountDownTorrentWithStats(final MockedTorrent torrent, final CountDownLatch countDownLatch) {
            super(torrent);
            cdl = countDownLatch;
        }

        @Override
        void addUploaded(final Long uploaded) {
            super.addUploaded(uploaded);
            cdl.countDown();
        }

        public CountDownLatch getCountDownLatch() {
            return cdl;
        }
    }

}

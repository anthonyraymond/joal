package org.araymond.joal.core.ttorent.client.bandwidth;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.config.AppConfiguration;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.assertj.core.data.Percentage;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.any;

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
    public void shouldRefreshTorrentCurrentSpeedWhenOnAnnouncerWillAnnounceIsCalled() {
        final JoalConfigProvider configProvider = prepareMockedConfProvider(130L, 180L);

        final BandwidthDispatcher bandwidthDispatcher = Mockito.spy(new BandwidthDispatcher(configProvider, 1));
        final TorrentWithStats torrentWithStats = Mockito.mock(TorrentWithStats.class);

        bandwidthDispatcher.onAnnouncerWillAnnounce(RequestEvent.COMPLETED, torrentWithStats);
        Mockito.verify(torrentWithStats, Mockito.times(1)).refreshRandomSpeedInBytes(Matchers.anyLong());
        bandwidthDispatcher.onAnnouncerWillAnnounce(RequestEvent.NONE, torrentWithStats);
        Mockito.verify(torrentWithStats, Mockito.times(2)).refreshRandomSpeedInBytes(Matchers.anyLong());
        bandwidthDispatcher.onAnnouncerWillAnnounce(RequestEvent.STARTED, torrentWithStats);
        Mockito.verify(torrentWithStats, Mockito.times(3)).refreshRandomSpeedInBytes(Matchers.anyLong());
    }

    @Test
    public void shouldNotRefreshTorrentCurrentSpeedWhenOnAnnouncerWillAnnounceIsCalledIfEventIsStopped() {
        final JoalConfigProvider configProvider = prepareMockedConfProvider(130L, 180L);

        final BandwidthDispatcher bandwidthDispatcher = Mockito.spy(new BandwidthDispatcher(configProvider, 1));
        final TorrentWithStats torrentWithStats = Mockito.mock(TorrentWithStats.class);

        bandwidthDispatcher.onAnnouncerWillAnnounce(RequestEvent.STOPPED, torrentWithStats);
        Mockito.verify(torrentWithStats, Mockito.never()).refreshRandomSpeedInBytes(Matchers.anyLong());
    }

    @Test
    public void shouldAddTorrentToListOnAnnounceStarts() {
        final JoalConfigProvider configProvider = prepareMockedConfProvider(130L, 180L);
        final BandwidthDispatcher bandwidthDispatcher = new BandwidthDispatcher(configProvider, 1);

        assertThat(bandwidthDispatcher.getTorrentCount()).isEqualTo(0);
        bandwidthDispatcher.onAnnouncerStart(null, Mockito.mock(TorrentWithStats.class));
        assertThat(bandwidthDispatcher.getTorrentCount()).isEqualTo(1);
    }

    @Test
    public void shouldRemoveTorrentToListOnAnnounceStarts() {
        final JoalConfigProvider configProvider = prepareMockedConfProvider(130L, 180L);
        final BandwidthDispatcher bandwidthDispatcher = new BandwidthDispatcher(configProvider, 1);

        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        bandwidthDispatcher.onAnnouncerStart(null, torrent);
        bandwidthDispatcher.onAnnouncerStop(null, torrent);
        assertThat(bandwidthDispatcher.getTorrentCount()).isEqualTo(0);
    }

    @Test
    public void shouldIncrementTorrentStat() throws InterruptedException {
        final JoalConfigProvider configProvider = prepareMockedConfProvider(130L, 180L);

        final int updateInterval = 4;
        final BandwidthDispatcher bandwidthDispatcher = new BandwidthDispatcher(configProvider, updateInterval);
        final TorrentWithStats torrent = new TorrentWithStats(Mockito.mock(MockedTorrent.class));

        bandwidthDispatcher.onAnnouncerStart(null, torrent);
        bandwidthDispatcher.onAnnouncerWillAnnounce(RequestEvent.NONE, torrent);

        bandwidthDispatcher.start();
        Thread.sleep(6);
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
        final Collection<TorrentWithStats> torrents = new ArrayList<>();
        for (int i = 0; i < 4; ++i) {
            final TorrentWithStats torrent = new TorrentWithStats(Mockito.mock(MockedTorrent.class));
            torrents.add(torrent);
            bandwidthDispatcher.onAnnouncerStart(null, torrent);
            bandwidthDispatcher.onAnnouncerWillAnnounce(RequestEvent.NONE, torrent);
        }

        bandwidthDispatcher.start();
        Thread.sleep(6);
        bandwidthDispatcher.stop();

        final AppConfiguration conf = configProvider.get();
        for (final TorrentWithStats t: torrents) {
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
        final TorrentWithStats torrent = new TorrentWithStats(Mockito.mock(MockedTorrent.class));

        bandwidthDispatcher.onAnnouncerStart(null, torrent);
        bandwidthDispatcher.onAnnouncerWillAnnounce(RequestEvent.NONE, torrent);

        bandwidthDispatcher.start();
        Thread.sleep(6);
        bandwidthDispatcher.onAnnouncerStop(null, torrent);

        assertThat(torrent.getUploaded())
                .isBetween(
                        configProvider.get().getMinUploadRate() * 1000 / (1000 / updateInterval),
                        configProvider.get().getMaxUploadRate() * 1000 / (1000 / updateInterval)
                );

        Thread.sleep(6);
        bandwidthDispatcher.stop();

        assertThat(torrent.getUploaded())
                .isBetween(
                        configProvider.get().getMinUploadRate() * 1000 / (1000 / updateInterval),
                        configProvider.get().getMaxUploadRate() * 1000 / (1000 / updateInterval)
                );
    }

    @Test
    public void shouldProvideLargeStandardDeviationOverTime() throws InterruptedException {
        // If all values are provided by a simple rand, after a short time the average value converge to a average value
        //  and it result in upload graph being a straight line

        final JoalConfigProvider configProvider = prepareMockedConfProvider(50L, 130L);

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

        final double expectedAverage = (configProvider.get().getMinUploadRate() + configProvider.get().getMaxUploadRate()) / 2.0;
        final double realAverage = torrents.stream()
                .mapToDouble(TorrentWithStatsCountdown::getAverage)
                .average()
                .orElseThrow(() -> new IllegalStateException("Impossible to reach this case."));
        assertThat(realAverage)
                .as("Average speed should be close median between min and max speed.")
                .isCloseTo(expectedAverage, Percentage.withPercentage(10));

        final double expectedMinimumStandardDeviation = (configProvider.get().getMaxUploadRate() - configProvider.get().getMinUploadRate()) / 2.0 / 2.0;
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
            dispatcher.onAnnouncerWillAnnounce(null, this);
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

        @Override
        public boolean equals(final Object o) {
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }

}

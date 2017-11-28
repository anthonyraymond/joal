package org.araymond.joal.core.bandwith;

import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.*;

public class NewBandwidthDispatcherTest {

    @Test
    public void shouldReturnZeroIfInfoHashIsNotRegistered() throws InterruptedException {
        final RandomSpeedProvider speedProvider = Mockito.mock(RandomSpeedProvider.class);
        Mockito.doReturn(1000000L).when(speedProvider).getInBytesPerSeconds();

        final NewBandwidthDispatcher bandwidthDispatcher = new NewBandwidthDispatcher(2, speedProvider);
        bandwidthDispatcher.start();
        Thread.sleep(10);
        final TorrentSeedStats seedStats = bandwidthDispatcher.getSeedStatForTorrent(new InfoHash(new byte[]{12}));

        bandwidthDispatcher.stop();

        assertThat(seedStats.getDownloaded()).isEqualTo(0);
        assertThat(seedStats.getUploaded()).isEqualTo(0);
        assertThat(seedStats.getLeft()).isEqualTo(0);
    }

    @Test
    public void shouldNotIncrementRegisteredTorrentsBeforePeersHaveBeenAdded() throws InterruptedException {
        final RandomSpeedProvider speedProvider = Mockito.mock(RandomSpeedProvider.class);
        Mockito.doReturn(1000000L).when(speedProvider).getInBytesPerSeconds();

        final InfoHash infoHash = new InfoHash(new byte[]{12});
        final NewBandwidthDispatcher bandwidthDispatcher = new NewBandwidthDispatcher(2, speedProvider);

        bandwidthDispatcher.registerTorrent(infoHash);
        bandwidthDispatcher.start();
        Thread.sleep(10);
        final TorrentSeedStats seedStats = bandwidthDispatcher.getSeedStatForTorrent(infoHash);
        bandwidthDispatcher.stop();


        assertThat(seedStats.getDownloaded()).isEqualTo(0);
        assertThat(seedStats.getUploaded()).isEqualTo(0);
        assertThat(seedStats.getLeft()).isEqualTo(0);
    }

    @Test
    public void shouldNotIncrementRegisteredTorrentsWithZeroSeeders() throws InterruptedException {
        final RandomSpeedProvider speedProvider = Mockito.mock(RandomSpeedProvider.class);
        Mockito.doReturn(1000000L).when(speedProvider).getInBytesPerSeconds();

        final InfoHash infoHash = new InfoHash(new byte[]{12});
        final NewBandwidthDispatcher bandwidthDispatcher = new NewBandwidthDispatcher(2, speedProvider);

        bandwidthDispatcher.registerTorrent(infoHash);
        bandwidthDispatcher.updateTorrentPeers(infoHash, 0, 100);
        bandwidthDispatcher.start();
        Thread.sleep(10);
        final TorrentSeedStats seedStats = bandwidthDispatcher.getSeedStatForTorrent(infoHash);
        bandwidthDispatcher.stop();


        assertThat(seedStats.getDownloaded()).isEqualTo(0);
        assertThat(seedStats.getUploaded()).isEqualTo(0);
        assertThat(seedStats.getLeft()).isEqualTo(0);
    }

    @Test
    public void shouldNotIncrementRegisteredTorrentsWithZeroLeechers() throws InterruptedException {
        final RandomSpeedProvider speedProvider = Mockito.mock(RandomSpeedProvider.class);
        Mockito.doReturn(1000000L).when(speedProvider).getInBytesPerSeconds();

        final InfoHash infoHash = new InfoHash(new byte[]{12});
        final NewBandwidthDispatcher bandwidthDispatcher = new NewBandwidthDispatcher(2, speedProvider);

        bandwidthDispatcher.registerTorrent(infoHash);
        bandwidthDispatcher.updateTorrentPeers(infoHash, 100, 0);
        bandwidthDispatcher.start();
        Thread.sleep(10);
        final TorrentSeedStats seedStats = bandwidthDispatcher.getSeedStatForTorrent(infoHash);
        bandwidthDispatcher.stop();


        assertThat(seedStats.getDownloaded()).isEqualTo(0);
        assertThat(seedStats.getUploaded()).isEqualTo(0);
        assertThat(seedStats.getLeft()).isEqualTo(0);
    }

    @Test
    public void shouldRemoveUnregisteredTorrent() throws InterruptedException {
        final RandomSpeedProvider speedProvider = Mockito.mock(RandomSpeedProvider.class);
        Mockito.doReturn(1000000L).when(speedProvider).getInBytesPerSeconds();

        final InfoHash infoHash = new InfoHash(new byte[]{12});
        final NewBandwidthDispatcher bandwidthDispatcher = new NewBandwidthDispatcher(2, speedProvider);

        bandwidthDispatcher.registerTorrent(infoHash);
        bandwidthDispatcher.updateTorrentPeers(infoHash, 10, 10);

        bandwidthDispatcher.start();
        Thread.sleep(20);

        final long uploadedBeforeUnregistering = bandwidthDispatcher.getSeedStatForTorrent(infoHash).getUploaded();
        assertThat(uploadedBeforeUnregistering).isGreaterThan(1);
        bandwidthDispatcher.unregisterTorrent(infoHash);

        assertThat(bandwidthDispatcher.getSeedStatForTorrent(infoHash).getUploaded()).isEqualTo(0);

        bandwidthDispatcher.stop();
    }

    @Test
    public void shouldIncrementRegisteredTorrent() throws InterruptedException {
        final RandomSpeedProvider speedProvider = Mockito.mock(RandomSpeedProvider.class);
        Mockito.doReturn(1000000L).when(speedProvider).getInBytesPerSeconds();

        final InfoHash infoHash = new InfoHash(new byte[]{12});
        final InfoHash infoHash2 = new InfoHash(new byte[]{100});
        final NewBandwidthDispatcher bandwidthDispatcher = new NewBandwidthDispatcher(2, speedProvider);

        bandwidthDispatcher.registerTorrent(infoHash);
        bandwidthDispatcher.updateTorrentPeers(infoHash, 10, 10);
        bandwidthDispatcher.registerTorrent(infoHash2);
        bandwidthDispatcher.updateTorrentPeers(infoHash2, 20, 30);
        bandwidthDispatcher.start();
        Thread.sleep(30);
        final TorrentSeedStats seedStats = bandwidthDispatcher.getSeedStatForTorrent(infoHash);
        final TorrentSeedStats seedStats2 = bandwidthDispatcher.getSeedStatForTorrent(infoHash2);
        bandwidthDispatcher.stop();


        assertThat(seedStats.getUploaded()).isGreaterThan(1);
        assertThat(seedStats2.getUploaded()).isGreaterThan(1);
    }

    @Test
    public void shouldBeSafeToUpdateTorrentSeedsStatsWhileRegisteringTorrents() throws ExecutionException, InterruptedException {
        final RandomSpeedProvider speedProvider = Mockito.mock(RandomSpeedProvider.class);
        Mockito.doReturn(1000000L).when(speedProvider).getInBytesPerSeconds();

        final NewBandwidthDispatcher bandwidthDispatcher = new NewBandwidthDispatcher(1, speedProvider);

        bandwidthDispatcher.start();

        final ExecutorService executorService = Executors.newFixedThreadPool(5);
        final List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 100; ++i) {
            final InfoHash infoHash = new InfoHash((i + "").getBytes());
            futures.add(executorService.submit(() -> {
                bandwidthDispatcher.registerTorrent(infoHash);
                bandwidthDispatcher.updateTorrentPeers(infoHash, 20, 50);
            }));
        }
        for (final Future<?> future : futures) {
            future.get();
        }

        futures.clear();

        for (int i = 0; i < 100; ++i) {
            final InfoHash infoHash = new InfoHash((i + "").getBytes());
            futures.add(executorService.submit(() -> {
                bandwidthDispatcher.unregisterTorrent(infoHash);
            }));
        }
        for (final Future<?> future : futures) {
            future.get();
        }

        bandwidthDispatcher.stop();
    }

}

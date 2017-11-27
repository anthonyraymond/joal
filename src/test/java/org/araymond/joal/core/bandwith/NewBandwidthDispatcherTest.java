package org.araymond.joal.core.bandwith;

import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.in;

public class NewBandwidthDispatcherTest {

    @Test
    public void shouldReturnZeroIfInfoHashIsNotRegistered() throws InterruptedException {
        final NewBandwidthDispatcher bandwidthDispatcher = new NewBandwidthDispatcher(2);
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
        final InfoHash infoHash = new InfoHash(new byte[]{12});
        final NewBandwidthDispatcher bandwidthDispatcher = new NewBandwidthDispatcher(2);

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
    public void shouldNotIncrementRegisteredTorrentsWithZeroPeers() throws InterruptedException {
        final InfoHash infoHash = new InfoHash(new byte[]{12});
        final NewBandwidthDispatcher bandwidthDispatcher = new NewBandwidthDispatcher(2);

        bandwidthDispatcher.registerTorrent(infoHash);
        bandwidthDispatcher.updateTorrentPeers(infoHash, 0, 0);
        bandwidthDispatcher.start();
        Thread.sleep(10);
        final TorrentSeedStats seedStats = bandwidthDispatcher.getSeedStatForTorrent(infoHash);
        bandwidthDispatcher.stop();


        assertThat(seedStats.getDownloaded()).isEqualTo(0);
        assertThat(seedStats.getUploaded()).isEqualTo(0);
        assertThat(seedStats.getLeft()).isEqualTo(0);
    }

    @Test
    public void shouldNotIncrementUnregisteredTorrent() throws InterruptedException {
        final InfoHash infoHash = new InfoHash(new byte[]{12});
        final NewBandwidthDispatcher bandwidthDispatcher = new NewBandwidthDispatcher(2);

        bandwidthDispatcher.registerTorrent(infoHash);
        bandwidthDispatcher.updateTorrentPeers(infoHash, 10, 10);

        bandwidthDispatcher.start();
        Thread.sleep(10);

        bandwidthDispatcher.unregisterTorrent(infoHash);
        final long uploadedBeforeUnregistering = bandwidthDispatcher.getSeedStatForTorrent(infoHash).getUploaded();
        assertThat(uploadedBeforeUnregistering).isGreaterThan(1);

        Thread.sleep(10);
        bandwidthDispatcher.stop();

        final TorrentSeedStats seedStats = bandwidthDispatcher.getSeedStatForTorrent(infoHash);
        assertThat(seedStats.getUploaded()).isEqualTo(uploadedBeforeUnregistering);
    }

    @Test
    public void shouldIncrementRegisteredTorrent() throws InterruptedException {
        final InfoHash infoHash = new InfoHash(new byte[]{12});
        final InfoHash infoHash2 = new InfoHash(new byte[]{100});
        final NewBandwidthDispatcher bandwidthDispatcher = new NewBandwidthDispatcher(2);

        bandwidthDispatcher.registerTorrent(infoHash);
        bandwidthDispatcher.updateTorrentPeers(infoHash, 10, 10);
        bandwidthDispatcher.updateTorrentPeers(infoHash, 20, 30);
        bandwidthDispatcher.start();
        Thread.sleep(10);
        final TorrentSeedStats seedStats = bandwidthDispatcher.getSeedStatForTorrent(infoHash);
        final TorrentSeedStats seedStats2 = bandwidthDispatcher.getSeedStatForTorrent(infoHash2);
        bandwidthDispatcher.stop();


        assertThat(seedStats.getUploaded()).isGreaterThan(1);
        assertThat(seedStats2.getUploaded()).isGreaterThan(1);
    }

    @Test
    public void shouldBeSafeToUpdateTorrentSeedsStatsWhileRegisteringTorrents() {
        fail("not implemented");
    }

    @Test
    public void shouldBeSafeToUpdateTorrentSeedsStatsWhileUnregisteringTorrents() {
        fail("not implemented");
    }
}

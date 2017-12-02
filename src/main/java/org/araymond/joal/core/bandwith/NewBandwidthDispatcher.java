package org.araymond.joal.core.bandwith;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.araymond.joal.core.bandwith.weight.PeersAwareWeightCalculator;
import org.araymond.joal.core.bandwith.weight.WeightHolder;
import org.araymond.joal.core.torrent.torrent.InfoHash;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class NewBandwidthDispatcher implements Runnable {

    private final ReentrantReadWriteLock lock;
    private final WeightHolder<InfoHash> weightHolder;
    private final RandomSpeedProvider randomSpeedProvider;
    private final Map<InfoHash, TorrentSeedStats> torrentsSeedStats;
    private final Map<InfoHash, Speed> speedMap;
    private SpeedChangedListener speedChangedListener;
    private final int threadPauseInterval;
    private int threadLoopCounter = 0;
    private volatile boolean stop = false;
    private Thread thread;

    public NewBandwidthDispatcher(final int threadPauseInterval, final RandomSpeedProvider randomSpeedProvider) {
        this.threadPauseInterval = threadPauseInterval;
        this.torrentsSeedStats = new HashMap<>();
        this.speedMap = new HashMap<>();
        this.lock = new ReentrantReadWriteLock();


        this.weightHolder = new WeightHolder<>(new PeersAwareWeightCalculator());
        this.randomSpeedProvider = randomSpeedProvider;
    }

    public void setSpeedListener(final SpeedChangedListener speedListener) {
        this.speedChangedListener = speedListener;
    }

    /*
     * This method does not benefit from the lock, because the value will never be accessed in a ambiguous way.
     * And even if i happens, we returns 0 by default.
     */
    public TorrentSeedStats getSeedStatForTorrent(final InfoHash infoHash) {
        return this.torrentsSeedStats.getOrDefault(infoHash, new TorrentSeedStats());
    }

    public void start() {
        this.stop = false;
        this.thread = new Thread(this);
        this.thread.setName("bandwidth-dispatcher");
        this.thread.start();
    }

    public void stop() {
        this.stop = true;
        this.thread.interrupt();
        try {
            this.thread.join();
        } catch (final InterruptedException ignored) {
        }
    }

    @Override
    public void run() {
        try {
            while (!this.stop) {
                Thread.sleep(this.threadPauseInterval);
                ++this.threadLoopCounter;
                // refresh bandwidth every 1200000 milliseconds (20 minutes)
                if (this.threadLoopCounter == 1200000 / this.threadPauseInterval) {
                    this.refreshCurrentBandwidth();
                }
                // TODO: refresh current speed every 20 minutes

                // This method as to run as fast as possible to avoid blocking other ones. Because we wan't this loop
                //  to be scheduled as precise as we can. Locking to much will delay the Thread.sleep and cause stats
                //  to be undervalued
                this.lock.readLock().lock();
                final Set<Map.Entry<InfoHash, TorrentSeedStats>> entrySet = Sets.newHashSet(this.torrentsSeedStats.entrySet());
                this.lock.readLock().unlock();

                for (final Map.Entry<InfoHash, TorrentSeedStats> entry : entrySet) {
                    final long speedInBytesPerSecond = this.speedMap.getOrDefault(entry.getKey(), new Speed(0)).getBytesPerSeconds();
                    // Divide by 1000 because of the thread pause interval being in milliseconds
                    entry.getValue().addUploaded(speedInBytesPerSecond / 1000 * this.threadPauseInterval);
                }
            }
        } catch (final InterruptedException ignore) {
        }
    }

    public void updateTorrentPeers(final InfoHash infoHash, final int seeders, final int leechers) {
        this.lock.writeLock().lock();
        try {
            this.weightHolder.addOrUpdate(infoHash, new Peers(seeders, leechers));
            this.recomputeSpeeds();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public void registerTorrent(final InfoHash infoHash) {
        this.lock.writeLock().lock();
        try {
            this.torrentsSeedStats.put(infoHash, new TorrentSeedStats());
            this.speedMap.put(infoHash, new Speed(0));
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public void unregisterTorrent(final InfoHash infoHash) {
        this.lock.writeLock().lock();
        try {
            this.weightHolder.remove(infoHash);
            this.torrentsSeedStats.remove(infoHash);
            this.speedMap.remove(infoHash);
            this.recomputeSpeeds();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    private void refreshCurrentBandwidth() {
        this.lock.writeLock().lock();
        try {
            this.randomSpeedProvider.refresh();
            this.recomputeSpeeds();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    private void recomputeSpeeds() {
        for (final InfoHash infohash : this.torrentsSeedStats.keySet()) {
            final double percentOfSpeedAssigned = this.weightHolder.getTotalWeight() == 0.0
                    ? 0.0
                    : this.weightHolder.getWeightFor(infohash) / this.weightHolder.getTotalWeight();

            this.speedMap.compute(infohash, (hash, speed) -> {
                if (speed == null) {
                    return new Speed(0);
                }
                speed.setBytesPerSeconds((long) (this.randomSpeedProvider.getInBytesPerSeconds() * percentOfSpeedAssigned));
                return speed;
            });
        }
        if (speedChangedListener != null) {
            speedChangedListener.speedsHasChanged(Maps.newHashMap(this.speedMap));
        }
    }

}

package org.araymond.joal.core.ttorent.client.bandwidth;

import com.google.common.base.Preconditions;
import com.turn.ttorrent.common.protocol.TrackerMessage;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.araymond.joal.core.ttorent.client.announce.Announcer;
import org.araymond.joal.core.ttorent.client.announce.AnnouncerEventListener;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by raymo on 14/05/2017.
 */
public class BandwidthDispatcher implements AnnouncerEventListener, Runnable {

    private final JoalConfigProvider configProvider;
    /**
     * Update interval have to be a low value, because when a torrent is added, the Thread.pause may end and split value
     * among all torrent and add a non reasonable value to the freshly added torrent
     */
    // TODO : add a decorator around TorrentWithStat and store the datetime when it was added, so we can know if we should add uploaded or not
    private final Integer updateInterval;
    private final Map<TorrentWithStats, TorrentWithStatsWrapper> torrents;
    private final ReentrantReadWriteLock lock;

    private Thread thread;
    private boolean stop;

    public BandwidthDispatcher(final JoalConfigProvider configProvider) {
        this(configProvider, 1000);
    }

    BandwidthDispatcher(final JoalConfigProvider configProvider, final Integer updateInterval) {
        Preconditions.checkNotNull(configProvider, "Cannot build without ConfigProvider.");
        this.configProvider = configProvider;
        this.updateInterval = updateInterval;
        this.torrents = new HashMap<>(configProvider.get().getSimultaneousSeed());
        lock = new ReentrantReadWriteLock();
    }


    @Override
    public void onAnnounceRequesting(final TrackerMessage.AnnounceRequestMessage.RequestEvent event, final TorrentWithStats torrent) {
        if (event == TrackerMessage.AnnounceRequestMessage.RequestEvent.NONE) {
            this.torrents.get(torrent).refreshRandomSpeed();
        }
    }

    @Override
    public void onNoMoreLeecherForTorrent(final Announcer announcer, final TorrentWithStats torrent) {

    }

    @Override
    public void onAnnouncerStart(final Announcer announcer, final TorrentWithStats torrent) {
        this.lock.writeLock().lock();
        try {
            this.torrents.put(torrent, new TorrentWithStatsWrapper(torrent, configProvider));
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public void onAnnouncerStop(final Announcer announcer, final TorrentWithStats torrent) {
        this.lock.writeLock().lock();
        try {
            this.torrents.remove(torrent);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public void start() {
        this.stop = false;
        if (this.thread == null || !this.thread.isAlive()) {
            this.thread = new Thread(this);
            this.thread.setName("bandwidth-manager");
            this.thread.start();
        }
    }

    public void stop() {
        this.stop = true;
        if (this.thread != null) {
            this.thread.interrupt();

            try {
                this.thread.join();
            } catch (final InterruptedException ignored) {
            }
            this.thread = null;

            this.lock.writeLock().lock();
            try {
                this.torrents.clear();
            } finally {
                this.lock.writeLock().unlock();
            }
        }
    }

    @Override
    public void run() {
        try {
            while (!this.stop) {
                Thread.sleep(updateInterval);

                lock.readLock().lock();

                final int torrentCount = this.torrents.size();
                for (final TorrentWithStatsWrapper torrentWrapper: this.torrents.values()) {
                    final long uploadRateInBytesForTorrent = torrentWrapper.getRandomSpeedInBytes() / torrentCount;
                    final long uploadRateInKiloBytesForTorrent = uploadRateInBytesForTorrent / 1024;

                    torrentWrapper.getTorrent().addUploaded(uploadRateInKiloBytesForTorrent * updateInterval);
                }
                lock.readLock().unlock();
            }
        } catch (final InterruptedException ignored) {
        } finally {
            if (this.lock.getReadLockCount() > 0) {
                lock.readLock().unlock();
            }
        }
    }


    static class TorrentWithStatsWrapper {
        private final TorrentWithStats torrent;
        private final JoalConfigProvider configProvider;
        private Long randomSpeedInBytes;
        private final Random rand;

        TorrentWithStatsWrapper(final TorrentWithStats torrent, final JoalConfigProvider configProvider) {
            this.torrent = torrent;
            this.configProvider = configProvider;
            rand = new Random();
            this.refreshRandomSpeed();
        }

        TorrentWithStats getTorrent() {
            return torrent;
        }

        Long getRandomSpeedInBytes() {
            return randomSpeedInBytes;
        }

        void refreshRandomSpeed() {
            // TODO : implement config with Long instead of INT (and ensure it does not add 'L' when serialized
            final long minUploadRateInBytes = (long) configProvider.get().getMinUploadRate() * 1024;
            final long maxUploadRateInBytes = (long) configProvider.get().getMaxUploadRate() * 1024;
            // TODO : add some randomness to ensure values wont be 250000 or 450000
            this.randomSpeedInBytes = minUploadRateInBytes + (long) (this.rand.nextDouble() * (maxUploadRateInBytes - minUploadRateInBytes));
        }

    }

}

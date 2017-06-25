package org.araymond.joal.core.ttorent.client.bandwidth;

import com.google.common.base.Preconditions;
import com.turn.ttorrent.common.protocol.TrackerMessage;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.araymond.joal.core.ttorent.client.announce.Announcer;
import org.araymond.joal.core.ttorent.client.announce.AnnouncerEventListener;
import org.araymond.joal.core.utils.RandomGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by raymo on 14/05/2017.
 */
public class BandwidthDispatcher implements AnnouncerEventListener, Runnable {

    private final JoalConfigProvider configProvider;
    private final RandomGenerator randomGenerator;
    /**
     * Update interval have to be a low value, because when a torrent is added, the Thread.pause may end and split value
     * among all torrent and add a non reasonable value to the freshly added torrent
     */
    // TODO : add a decorator around TorrentWithStat and store the datetime when it was added, so we can know if we should add uploaded or not
    private final Integer updateInterval;
    private final List<TorrentWithStats> torrents;
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
        this.torrents = new ArrayList<>(configProvider.get().getSimultaneousSeed());
        lock = new ReentrantReadWriteLock();
        this.randomGenerator = new RandomGenerator();
    }

    @Override
    public void onAnnouncerWillAnnounce(final TrackerMessage.AnnounceRequestMessage.RequestEvent event, final TorrentWithStats torrent) {
        if (event != TrackerMessage.AnnounceRequestMessage.RequestEvent.STOPPED) {
            final Long minUploadRateInBytes = configProvider.get().getMinUploadRate() * 1024L;
            final Long maxUploadRateInBytes = configProvider.get().getMaxUploadRate() * 1024L;

            final Long randomSpeedInBytes = randomGenerator.nextLong(minUploadRateInBytes, maxUploadRateInBytes);

            torrent.refreshRandomSpeedInBytes(randomSpeedInBytes);
        }
    }

    @Override
    public void onAnnounceSuccess(final TorrentWithStats torrent, final int interval, final int seeders, final int leechers) {
    }

    @Override
    public void onAnnounceFail(final TrackerMessage.AnnounceRequestMessage.RequestEvent event, final TorrentWithStats torrent, final String error) {
    }

    @Override
    public void onNoMoreLeecherForTorrent(final Announcer announcer, final TorrentWithStats torrent) {
    }

    @Override
    public void onAnnouncerStart(final Announcer announcer, final TorrentWithStats torrent) {
        this.lock.writeLock().lock();
        try {
            this.torrents.add(torrent);
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

    public int getTorrentCount() {
        return this.torrents.size();
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
                for (final TorrentWithStats torrent: this.torrents) {
                    final long uploadRateInBytesForTorrent = torrent.getCurrentRandomSpeedInBytes() / torrentCount;

                    torrent.addUploaded(uploadRateInBytesForTorrent * updateInterval / 1000);
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

}

package org.araymond.joal.core.torrent;

import com.google.common.base.Preconditions;
import org.araymond.joal.core.config.JoalConfigProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by raymo on 14/05/2017.
 */
public class BandwidthManager implements Runnable {

    private final JoalConfigProvider configProvider;
    /**
     * Update nterval have to be a low value, because when a torrent is added, the Thread.pause may end and split value
     * among all torrent and add a non reasonable value to the freshly added torrent
     */
    // TODO : add a decorator around TorrentWithStat and store the datetime when it was added, so we can know if we should add uploaded or not
    private final Integer updateInterval;
    private final List<TorrentWithStats> torrents;
    private final ReentrantReadWriteLock lock;
    private final Random rand;

    private Thread thread;
    private boolean stop;

    public BandwidthManager(final JoalConfigProvider configProvider) {
        this(configProvider, 1000);
    }

    public BandwidthManager(final JoalConfigProvider configProvider, final Integer updateInterval) {
        Preconditions.checkNotNull(configProvider, "Cannot build without ConfigProvider.");
        this.configProvider = configProvider;
        this.updateInterval = updateInterval;
        // TODO : list size = configProvider.getMaxTorrentToSeedSimultaneously
        this.torrents = new ArrayList<>();
        lock = new ReentrantReadWriteLock();
        rand = new Random();
    }

    public void registerTorrent(final TorrentWithStats torrent) {
        this.lock.writeLock().lock();
        this.torrents.add(torrent);
        this.lock.writeLock().unlock();
    }

    public void unRegisterTorrent(final TorrentWithStats torrent) {
        this.lock.writeLock().lock();
        this.torrents.remove(torrent);
        this.lock.writeLock().unlock();
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
            this.torrents.clear();
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public void run() {
        try {
            while (!this.stop) {
                Thread.sleep(updateInterval);

                lock.readLock().lock();

                final int torrentCount = this.torrents.size();
                for (final TorrentWithStats torrent : this.torrents) {
                    final long uploadRateInBytes = generateRandomizedSpeedInBytes();

                    torrent.addUploaded(uploadRateInBytes * updateInterval / 1000 / torrentCount);
                }
            }
        } catch (final InterruptedException ignored) {
        } finally {
            lock.readLock().unlock();
        }
    }

    long generateRandomizedSpeedInBytes() {
        // TODO : implement config with Long instead of INT (and ensure it does not add 'L' when serialized
        final long minUploadRate = (long) configProvider.get().getMinUploadRate() * 1024;
        final long maxUploadRate = (long) configProvider.get().getMaxUploadRate() * 1024;
        return minUploadRate + (long) (this.rand.nextDouble() * (maxUploadRate - minUploadRate));
    }

}

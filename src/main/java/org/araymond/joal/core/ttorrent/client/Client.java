package org.araymond.joal.core.ttorrent.client;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.araymond.joal.core.exception.NoMoreTorrentsFileAvailableException;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.torrent.watcher.TorrentFileChangeAware;
import org.araymond.joal.core.torrent.watcher.TorrentFileProvider;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceDataAccessor;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceRequest;
import org.araymond.joal.core.ttorrent.client.announcer.request.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnouncerExecutor;
import org.araymond.joal.core.ttorrent.client.announcer.response.AnnounceResponseCallback;
import org.slf4j.Logger;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.slf4j.LoggerFactory.getLogger;

public class Client implements TorrentFileChangeAware {
    private static final Logger logger = getLogger(Client.class);

    private final JoalConfigProvider configProvider;
    private final TorrentFileProvider torrentFileProvider;
    private final AnnouncerExecutor announcerExecutor;
    private final List<MockedTorrent> currentlySeedingTorrents;
    private final DelayQueue<AnnounceRequest> delayQueue;
    private final AnnounceResponseCallback announceResponseCallback;
    private final AnnounceDataAccessor announceDataAccessor;
    private final ReentrantReadWriteLock lock;
    private Thread thread;
    private volatile boolean stop = false;


    public Client(final JoalConfigProvider configProvider, final TorrentFileProvider torrentFileProvider, final AnnouncerExecutor announcerExecutor, final DelayQueue<AnnounceRequest> delayQueue, final AnnounceResponseCallback announceResponseCallback, AnnounceDataAccessor announceDataAccessor) {
        this.configProvider = configProvider;
        this.torrentFileProvider = torrentFileProvider;
        this.announcerExecutor = announcerExecutor;
        this.delayQueue = delayQueue;
        this.announceResponseCallback = announceResponseCallback;
        this.announceDataAccessor = announceDataAccessor;
        this.currentlySeedingTorrents = new ArrayList<>();
        this.lock = new ReentrantReadWriteLock();
    }

    public void start() {
        this.stop = false;

        this.thread = new Thread(() -> {
            while (!this.stop) {
                final List<AnnounceRequest> availables = this.delayQueue.getAvailables();
                availables.forEach(req -> {
                    this.announcerExecutor.execute(req, this.announceResponseCallback);
                    try {
                        this.lock.writeLock().lock();
                        this.currentlySeedingTorrents.add(req.getAnnouncer().getTorrent());
                    } finally {
                        this.lock.writeLock().unlock();
                    }
                });

                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException ignored) {
                }
            }
        });
        for (int i = 0; i < this.configProvider.get().getSimultaneousSeed(); i++) {
            final MockedTorrent torrent;
            try {
                this.lock.writeLock().lock();
                torrent = this.torrentFileProvider.getTorrentNotIn(this.currentlySeedingTorrents);
                this.currentlySeedingTorrents.add(torrent);
            } catch (final NoMoreTorrentsFileAvailableException ignored) {
                break;
            } finally {
                this.lock.writeLock().unlock();
            }
            final Announcer announcer = new Announcer(torrent, this.announceDataAccessor);
            this.delayQueue.addOrReplace(AnnounceRequest.createStart(announcer), 0, ChronoUnit.SECONDS);
        }

        this.thread.setName("client-orchestrator-thread");
        this.thread.start();
    }

    public void stop() {
        this.stop = true;
        this.thread.interrupt();
        try {
            this.thread.join();
        } catch (final InterruptedException ignored) {
        }
        this.delayQueue.drainAll().stream()
                .filter(req -> req.getEvent() != RequestEvent.STARTED)
                .map(AnnounceRequest::getAnnouncer)
                .map(AnnounceRequest::createStop)
                .forEach(req -> this.announcerExecutor.execute(req, this.announceResponseCallback));

        this.announcerExecutor.awaitForRunningTasks();
    }

    public void onTorrentHasStopped(final MockedTorrent torrent) {
        try {
            this.lock.writeLock().lock();
            this.currentlySeedingTorrents.remove(torrent);
        } finally {
            this.lock.writeLock().unlock();
        }
        if (this.stop) {
            return; // TODO: double check that
        }
        // TODO: add a new torrent
    }

    @Override
    public void onTorrentFileAdded(final MockedTorrent torrent) {

    }

    @Override
    public void onTorrentFileRemoved(final MockedTorrent torrent) {

    }
}

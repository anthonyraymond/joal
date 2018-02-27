package org.araymond.joal.core.ttorrent.client;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.araymond.joal.core.exception.NoMoreTorrentsFileAvailableException;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.torrent.watcher.TorrentFileChangeAware;
import org.araymond.joal.core.torrent.watcher.TorrentFileProvider;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.AnnouncerFacade;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceDataAccessor;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceRequest;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnouncerExecutor;
import org.araymond.joal.core.ttorrent.client.announcer.response.AnnounceResponseCallback;
import org.slf4j.Logger;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class Client implements TorrentFileChangeAware, ClientFacade {
    private static final Logger logger = getLogger(Client.class);

    private final JoalConfigProvider configProvider;
    private final TorrentFileProvider torrentFileProvider;
    private final AnnouncerExecutor announcerExecutor;
    private final List<Announcer> currentlySeedingAnnouncer;
    private final DelayQueue<AnnounceRequest> delayQueue;
    private final AnnounceResponseCallback announceResponseCallback;
    private final AnnounceDataAccessor announceDataAccessor;
    private final ReentrantReadWriteLock lock;
    private Thread thread;
    private volatile boolean stop = false;

    Client(final JoalConfigProvider configProvider, final TorrentFileProvider torrentFileProvider, final AnnouncerExecutor announcerExecutor, final DelayQueue<AnnounceRequest> delayQueue, final AnnounceResponseCallback announceResponseCallback, final AnnounceDataAccessor announceDataAccessor) {
        Preconditions.checkNotNull(configProvider, "JoalConfigProvider must not be null");
        Preconditions.checkNotNull(torrentFileProvider, "TorrentFileProvider must not be null");
        Preconditions.checkNotNull(delayQueue, "DelayQueue must not be null");
        Preconditions.checkNotNull(announceResponseCallback, "AnnounceResponseCallback must not be null");
        Preconditions.checkNotNull(announceDataAccessor, "AnnounceDataAccessor must not be null");
        this.configProvider = configProvider;
        this.torrentFileProvider = torrentFileProvider;
        this.announcerExecutor = announcerExecutor;
        this.delayQueue = delayQueue;
        this.announceResponseCallback = announceResponseCallback;
        this.announceDataAccessor = announceDataAccessor;
        this.currentlySeedingAnnouncer = new ArrayList<>();
        this.lock = new ReentrantReadWriteLock();
    }

    @Override
    public void start() {
        this.stop = false;

        this.thread = new Thread(() -> {
            while (!this.stop) {
                final List<AnnounceRequest> availables = this.delayQueue.getAvailables();
                availables.forEach(req -> {
                    this.announcerExecutor.execute(req, this.announceResponseCallback);
                    try {
                        this.lock.writeLock().lock();
                        this.currentlySeedingAnnouncer.removeIf(an -> an.equals(req.getAnnouncer())); // remove the last recorded event
                        this.currentlySeedingAnnouncer.add(req.getAnnouncer());
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
            try {
                this.lock.writeLock().lock();

                this.addTorrent();
            } catch (final NoMoreTorrentsFileAvailableException ignored) {
                break;
            } finally {
                this.lock.writeLock().unlock();
            }

        }

        this.thread.setName("client-orchestrator-thread");

        this.thread.start();
        this.torrentFileProvider.registerListener(this);
    }

    private void addTorrent() throws NoMoreTorrentsFileAvailableException {
        final MockedTorrent torrent = this.torrentFileProvider.getTorrentNotIn(
                this.currentlySeedingAnnouncer.stream()
                        .map(Announcer::getTorrent)
                        .collect(Collectors.toList())
        );
        final Announcer announcer = new Announcer(torrent, this.announceDataAccessor);
        this.currentlySeedingAnnouncer.add(announcer);
        this.delayQueue.addOrReplace(AnnounceRequest.createStart(announcer), 0, ChronoUnit.SECONDS);
    }

    @Override
    public void stop() {
        this.stop = true;
        this.torrentFileProvider.unRegisterListener(this);
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

    public void onTooManyFailedInARaw(final Announcer announcer) {
        try {
            this.lock.writeLock().lock();
            this.currentlySeedingAnnouncer.remove(announcer);
            this.torrentFileProvider.moveToArchiveFolder(announcer.getTorrentInfoHash());
            if (this.stop) {
                return;
            }

            this.addTorrent();
        } catch (final NoMoreTorrentsFileAvailableException ignored) {
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public void onNoMorePeers(final InfoHash infoHash) {
        if (!this.configProvider.get().shouldKeepTorrentWithZeroLeechers()) {
            this.torrentFileProvider.moveToArchiveFolder(infoHash);
        }
    }

    public void onTorrentHasStopped(final Announcer stoppedAnnouncer) {
        try {
            this.lock.writeLock().lock();
            if (this.stop) {
                return;
            }

            this.addTorrent();
        } catch (final NoMoreTorrentsFileAvailableException ignored) {
        } finally {
            this.currentlySeedingAnnouncer.remove(stoppedAnnouncer);
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public void onTorrentFileAdded(final MockedTorrent torrent) {
        if (this.stop) {
            return;
        }
        try {
            this.lock.writeLock().lock();
            if (this.currentlySeedingAnnouncer.size() >= this.configProvider.get().getSimultaneousSeed()) {
                return;
            }
            final Announcer announcer = new Announcer(torrent, this.announceDataAccessor);
            this.currentlySeedingAnnouncer.add(announcer);
            this.delayQueue.addOrReplace(AnnounceRequest.createStart(announcer), 1, ChronoUnit.SECONDS);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public void onTorrentFileRemoved(final MockedTorrent torrent) {
        try {
            this.lock.writeLock().lock();
            this.currentlySeedingAnnouncer.stream()
                    .filter(announcer -> announcer.getTorrentInfoHash().equals(torrent.getTorrentInfoHash()))
                    .findFirst()
                    .ifPresent(announcer ->
                            this.delayQueue.addOrReplace(AnnounceRequest.createStop(announcer), 1, ChronoUnit.SECONDS)
                    );
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public List<AnnouncerFacade> getCurrentlySeedingAnnouncer() {
        try {
            this.lock.readLock().lock();
            return Lists.newArrayList(this.currentlySeedingAnnouncer);
        } finally {
            this.lock.readLock().unlock();
        }
    }
}

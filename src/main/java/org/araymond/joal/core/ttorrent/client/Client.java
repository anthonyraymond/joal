package org.araymond.joal.core.ttorrent.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.config.AppConfiguration;
import org.araymond.joal.core.events.torrent.files.TorrentFileAddedEvent;
import org.araymond.joal.core.events.torrent.files.TorrentFileDeletedEvent;
import org.araymond.joal.core.exception.NoMoreTorrentsFileAvailableException;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.torrent.watcher.TorrentFileChangeAware;
import org.araymond.joal.core.torrent.watcher.TorrentFileProvider;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.AnnouncerFacade;
import org.araymond.joal.core.ttorrent.client.announcer.AnnouncerFactory;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceRequest;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnouncerExecutor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;

public class Client implements TorrentFileChangeAware, ClientFacade {
    private final AppConfiguration appConfiguration;
    private final TorrentFileProvider torrentFileProvider;
    private final ApplicationEventPublisher eventPublisher;
    private AnnouncerExecutor announcerExecutor;
    private final List<Announcer> currentlySeedingAnnouncers;
    private final DelayQueue<AnnounceRequest> delayQueue;
    private final AnnouncerFactory announcerFactory;
    private final ReentrantReadWriteLock lock;
    private Thread thread;
    private volatile boolean stop = true;

    Client(final AppConfiguration appConfiguration, final TorrentFileProvider torrentFileProvider, final AnnouncerExecutor announcerExecutor,
           final DelayQueue<AnnounceRequest> delayQueue, final AnnouncerFactory announcerFactory, final ApplicationEventPublisher eventPublisher) {
        Preconditions.checkNotNull(appConfiguration, "AppConfiguration must not be null");
        Preconditions.checkNotNull(torrentFileProvider, "TorrentFileProvider must not be null");
        Preconditions.checkNotNull(delayQueue, "DelayQueue must not be null");
        Preconditions.checkNotNull(announcerFactory, "AnnouncerFactory must not be null");
        this.eventPublisher = eventPublisher;
        this.appConfiguration = appConfiguration;
        this.torrentFileProvider = torrentFileProvider;
        this.announcerExecutor = announcerExecutor;
        this.delayQueue = delayQueue;
        this.announcerFactory = announcerFactory;
        this.currentlySeedingAnnouncers = new ArrayList<>();
        this.lock = new ReentrantReadWriteLock();
    }

    @VisibleForTesting
    void setAnnouncerExecutor(final AnnouncerExecutor announcerExecutor) {
        this.announcerExecutor = announcerExecutor;
    }

    @Override
    public void start() {
        this.stop = false;

        this.thread = new Thread(() -> {
            while (!this.stop) {
                this.delayQueue.getAvailables().forEach(req -> {
                    this.announcerExecutor.execute(req);
                    try {
                        this.lock.writeLock().lock();
                        // TODO: do we need removeIf()? why not use remove(req.getAnnouncer())? because list may contain more instances of same announcer?
                        this.currentlySeedingAnnouncers.removeIf(a -> a.equals(req.getAnnouncer()));  // remove the last recorded events
                        this.currentlySeedingAnnouncers.add(req.getAnnouncer());
                    } finally {
                        this.lock.writeLock().unlock();
                    }
                });

                try {
                    MILLISECONDS.sleep(1000); // TODO: move to config
                } catch (final InterruptedException ignored) {
                }
            }
        });

        for (int i = 0; i < this.appConfiguration.getSimultaneousSeed(); i++) {
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
                this.currentlySeedingAnnouncers.stream()
                        .map(Announcer::getTorrentInfoHash)
                        .collect(toList())
        );
        final Announcer announcer = this.announcerFactory.create(torrent);
        this.currentlySeedingAnnouncers.add(announcer);
        this.delayQueue.addOrReplace(AnnounceRequest.createStart(announcer), 0, ChronoUnit.SECONDS);
    }

    @Override
    public void stop() {
        try {
            this.lock.writeLock().lock();
            this.stop = true;
            this.torrentFileProvider.unRegisterListener(this);
            if (this.thread != null) {
                this.thread.interrupt();
                try {
                    this.thread.join();
                } catch (final InterruptedException ignored) {
                }
                this.thread = null;
            }
            this.delayQueue.drainAll().stream()
                    .filter(req -> req.getEvent() != RequestEvent.STARTED)
                    .map(AnnounceRequest::getAnnouncer)
                    .map(AnnounceRequest::createStop)
                    .forEach(this.announcerExecutor::execute);

            this.announcerExecutor.awaitForRunningTasks();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public void onTooManyFailedInARaw(final Announcer announcer) {
        if (this.stop) {
            this.currentlySeedingAnnouncers.remove(announcer);
            return;
        }

        try {
            this.lock.writeLock().lock();
            this.currentlySeedingAnnouncers.remove(announcer); // Remove from announcers list asap, otherwise the deletion will trigger an announce stop event.
            this.torrentFileProvider.moveToArchiveFolder(announcer.getTorrentInfoHash());
            this.addTorrent();
        } catch (final NoMoreTorrentsFileAvailableException ignored) {
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public void onNoMorePeers(final InfoHash infoHash) {
        if (!this.appConfiguration.isKeepTorrentWithZeroLeechers()) {
            this.torrentFileProvider.moveToArchiveFolder(infoHash);
        }
    }

    public void onTorrentHasStopped(final Announcer stoppedAnnouncer) {
        try {
            this.lock.writeLock().lock();
            if (!this.stop) {
                this.addTorrent();
            }
        } catch (final NoMoreTorrentsFileAvailableException ignored) {
        } finally {
            this.currentlySeedingAnnouncers.remove(stoppedAnnouncer);
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public void onTorrentFileAdded(final MockedTorrent torrent) {
        this.eventPublisher.publishEvent(new TorrentFileAddedEvent(torrent));
        if (this.stop) {
            return;
        }
        try {
            this.lock.writeLock().lock();
            if (this.currentlySeedingAnnouncers.size() >= this.appConfiguration.getSimultaneousSeed()) {
                return;
            }
            final Announcer announcer = this.announcerFactory.create(torrent);
            this.currentlySeedingAnnouncers.add(announcer);
            this.delayQueue.addOrReplace(AnnounceRequest.createStart(announcer), 1, ChronoUnit.SECONDS);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public void onTorrentFileRemoved(final MockedTorrent torrent) {
        this.eventPublisher.publishEvent(new TorrentFileDeletedEvent(torrent));
        try {
            this.lock.writeLock().lock();
            this.currentlySeedingAnnouncers.stream()
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
    public List<AnnouncerFacade> getCurrentlySeedingAnnouncers() {
        Lock lock = this.lock.readLock();
        try {
            lock.lock();
            return new ArrayList<>(this.currentlySeedingAnnouncers);
        } finally {
            lock.unlock();
        }
    }
}

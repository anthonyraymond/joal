package org.araymond.joal.core.ttorrent.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
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
import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class Client implements TorrentFileChangeAware, ClientFacade {
    private static final Logger logger = getLogger(Client.class);

    private final AppConfiguration appConfiguration;
    private final TorrentFileProvider torrentFileProvider;
    private final ApplicationEventPublisher eventPublisher;
    private AnnouncerExecutor announcerExecutor;
    private final List<Announcer> currentlySeedingAnnouncer;
    private final DelayQueue<AnnounceRequest> delayQueue;
    private final AnnouncerFactory announcerFactory;
    private final ReentrantReadWriteLock lock;
    private Thread thread;
    private volatile boolean stop = true;

    Client(final AppConfiguration appConfiguration, final TorrentFileProvider torrentFileProvider, final AnnouncerExecutor announcerExecutor, final DelayQueue<AnnounceRequest> delayQueue, final AnnouncerFactory announcerFactory, final ApplicationEventPublisher eventPublisher) {
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
        this.currentlySeedingAnnouncer = new ArrayList<>();
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
                for (final AnnounceRequest req : this.delayQueue.getAvailables()) {
                    this.announcerExecutor.execute(req);
                    try {
                        this.lock.writeLock().lock();
                        this.currentlySeedingAnnouncer.removeIf(an -> an.equals(req.getAnnouncer())); // remove the last recorded event
                        this.currentlySeedingAnnouncer.add(req.getAnnouncer());
                    } finally {
                        this.lock.writeLock().unlock();
                    }
                }

                try {
                    Thread.sleep(1000);
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
                this.currentlySeedingAnnouncer.stream()
                        .map(Announcer::getTorrentInfoHash)
                        .collect(Collectors.toList())
        );
        final Announcer announcer = this.announcerFactory.create(torrent);
        this.currentlySeedingAnnouncer.add(announcer);
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
            this.currentlySeedingAnnouncer.remove(announcer);
            return;
        }

        try {
            this.lock.writeLock().lock();
            this.currentlySeedingAnnouncer.remove(announcer); // Remove from announcers list asap, otherwise the deletion will trigger a announce stop event.
            this.torrentFileProvider.moveToArchiveFolder(announcer.getTorrentInfoHash());
            this.addTorrent();
        } catch (final NoMoreTorrentsFileAvailableException ignored) {
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public void onNoMorePeers(final InfoHash infoHash) {
        if (!this.appConfiguration.shouldKeepTorrentWithZeroLeechers()) {
            this.torrentFileProvider.moveToArchiveFolder(infoHash);
        }
    }

    public void onTorrentHasStopped(final Announcer stoppedAnnouncer) {
        if (this.stop) {
            this.currentlySeedingAnnouncer.remove(stoppedAnnouncer);
            return;
        }
        try {
            this.lock.writeLock().lock();

            this.addTorrent();
        } catch (final NoMoreTorrentsFileAvailableException ignored) {
        } finally {
            this.currentlySeedingAnnouncer.remove(stoppedAnnouncer);
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
            if (this.currentlySeedingAnnouncer.size() >= this.appConfiguration.getSimultaneousSeed()) {
                return;
            }
            final Announcer announcer = this.announcerFactory.create(torrent);
            this.currentlySeedingAnnouncer.add(announcer);
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

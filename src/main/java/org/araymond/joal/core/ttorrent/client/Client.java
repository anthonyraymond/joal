package org.araymond.joal.core.ttorrent.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import lombok.extern.slf4j.Slf4j;
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
import static java.util.stream.Collectors.toSet;

/**
 * This class is a torrent client agnostic representation. It
 * <ul>
 *     <li>keeps track of & manages all the queued {@link AnnounceRequest}s via {@link DelayQueue}</li>
 *     <li>spawns a thread periodically going through the {@code delayQueue}, and generating
 *     tracker announcements off it</li>
 *     <li>implements {@link TorrentFileChangeAware} to react to torrent file changes in filesystem</li>
 * </ul>
 */
@Slf4j
public class Client implements TorrentFileChangeAware, ClientFacade {
    private final AppConfiguration appConfig;
    private final TorrentFileProvider torrentFileProvider;
    private final ApplicationEventPublisher eventPublisher;
    private AnnouncerExecutor announcerExecutor;
    private final DelayQueue<AnnounceRequest> delayQueue;
    private final AnnouncerFactory announcerFactory;
    private final List<Announcer> currentlySeedingAnnouncers = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private Thread thread;
    private volatile boolean stop = true;

    Client(final AppConfiguration appConfig, final TorrentFileProvider torrentFileProvider, final AnnouncerExecutor announcerExecutor,
           final DelayQueue<AnnounceRequest> delayQueue, final AnnouncerFactory announcerFactory, final ApplicationEventPublisher eventPublisher) {
        Preconditions.checkNotNull(appConfig, "AppConfiguration must not be null");
        Preconditions.checkNotNull(torrentFileProvider, "TorrentFileProvider must not be null");
        Preconditions.checkNotNull(delayQueue, "DelayQueue must not be null");
        Preconditions.checkNotNull(announcerFactory, "AnnouncerFactory must not be null");
        this.eventPublisher = eventPublisher;
        this.appConfig = appConfig;
        this.torrentFileProvider = torrentFileProvider;
        this.announcerExecutor = announcerExecutor;
        this.delayQueue = delayQueue;
        this.announcerFactory = announcerFactory;
    }

    @VisibleForTesting
    void setAnnouncerExecutor(final AnnouncerExecutor announcerExecutor) {
        this.announcerExecutor = announcerExecutor;
    }

    @Override
    public void start() {
        this.stop = false;

        // TODO: use @Scheduled or something similar instead of looping manually over X period
        this.thread = new Thread(() -> {
            while (!this.stop) {
                this.delayQueue.getAvailables().forEach(req -> {
                    this.announcerExecutor.execute(req);
                    try {
                        this.lock.writeLock().lock();
                        this.currentlySeedingAnnouncers.remove(req.getAnnouncer());
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

        // start off by populating our state with the max concurrent torrents:
        Lock lock = this.lock.writeLock();
        for (int i = 0; i < this.appConfig.getSimultaneousSeed(); i++) {
            try {
                lock.lock();
                this.addTorrentFromDirectory();
            } catch (final NoMoreTorrentsFileAvailableException ignored) {
                break;
            } finally {
                lock.unlock();
            }
        }

        this.thread.setName("client-orchestrator-thread");
        this.thread.start();

        this.torrentFileProvider.registerListener(this);
    }

    /**
     * Polls a new torrent file from the directory that's not currently being tracked.
     */
    private void addTorrentFromDirectory() throws NoMoreTorrentsFileAvailableException {
        final MockedTorrent torrent = this.torrentFileProvider.getTorrentNotIn(
                this.currentlySeedingAnnouncers.stream()
                        .map(Announcer::getTorrentInfoHash)
                        .collect(toSet())
        );

        addTorrent(torrent);
    }

    private void addTorrent(MockedTorrent torrent) {
        final Announcer announcer = this.announcerFactory.create(torrent);
        this.currentlySeedingAnnouncers.add(announcer);
        this.delayQueue.addOrReplace(AnnounceRequest.createStart(announcer), 0, ChronoUnit.SECONDS);
    }

    @Override
    public void stop() {
        Lock lock = this.lock.writeLock();
        try {
            lock.lock();
            this.stop = true;
            this.torrentFileProvider.unRegisterListener(this);
            if (this.thread != null) {
                this.thread.interrupt();
                try {
                    this.thread.join();
                } catch (final InterruptedException ignored) {
                } finally {
                    this.thread = null;
                }
            }
            this.delayQueue.drainAll().stream()
                    .filter(req -> req.getEvent() != RequestEvent.STARTED)  // no need to generate 'stopped' request if the 'started' req was still waiting in queue
                    .map(AnnounceRequest::toStop)
                    .forEach(this.announcerExecutor::execute);

            this.announcerExecutor.awaitForRunningTasks();
        } finally {
            lock.unlock();
        }
    }

    public void onTooManyFailedInARow(final Announcer announcer) {
        if (this.stop) {
            this.currentlySeedingAnnouncers.remove(announcer);
            return;
        }

        Lock lock = this.lock.writeLock();
        try {
            lock.lock();
            this.currentlySeedingAnnouncers.remove(announcer);  // Remove from announcers list asap, otherwise the deletion will trigger an announce stop event.
            this.torrentFileProvider.moveToArchiveFolder(announcer.getTorrentInfoHash());
            this.addTorrentFromDirectory();
        } catch (final NoMoreTorrentsFileAvailableException ignored) {
        } finally {
            lock.unlock();
        }
    }

    public void onNoMorePeers(final InfoHash infoHash) {
        if (!this.appConfig.isKeepTorrentWithZeroLeechers()) {
            this.torrentFileProvider.moveToArchiveFolder(infoHash);
        }
    }

    public void onUploadRatioLimitReached(final InfoHash infoHash) {
        log.info("Deleting torrent [{}] since ratio has been met", infoHash);
        this.torrentFileProvider.moveToArchiveFolder(infoHash);
    }

    public void onTorrentHasStopped(final Announcer stoppedAnnouncer) {
        if (this.stop) {
            this.currentlySeedingAnnouncers.remove(stoppedAnnouncer);
            return;
        }

        Lock lock = this.lock.writeLock();
        try {
            lock.lock();
            this.addTorrentFromDirectory();
        } catch (final NoMoreTorrentsFileAvailableException ignored) {
        } finally {
            this.currentlySeedingAnnouncers.remove(stoppedAnnouncer);
            lock.unlock();
        }
    }

    @Override
    public void onTorrentFileAdded(final MockedTorrent torrent) {
        this.eventPublisher.publishEvent(new TorrentFileAddedEvent(torrent));

        if (!this.stop && this.currentlySeedingAnnouncers.size() < this.appConfig.getSimultaneousSeed()) {
            Lock lock = this.lock.writeLock();
            try {
                lock.lock();
                addTorrent(torrent);
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public void onTorrentFileRemoved(final MockedTorrent torrent) {
        this.eventPublisher.publishEvent(new TorrentFileDeletedEvent(torrent));
        Lock lock = this.lock.writeLock();
        try {
            lock.lock();
            this.currentlySeedingAnnouncers.stream()
                    .filter(announcer -> announcer.getTorrentInfoHash().equals(torrent.getTorrentInfoHash()))
                    .findAny()
                    .ifPresent(announcer ->
                            this.delayQueue.addOrReplace(
                                    AnnounceRequest.createStop(announcer), 1, ChronoUnit.SECONDS
                            )
                    );
        } finally {
            lock.unlock();
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

package com.araymond.joalcore.core.sharing.domain;

import com.araymond.joalcore.annotations.ddd.AggregateRoot;
import com.araymond.joalcore.core.infohash.domain.InfoHash;
import com.araymond.joalcore.core.sharing.domain.events.DoneDownloadingEvent;
import com.araymond.joalcore.core.sharing.domain.events.TorrentPausedEvent;
import com.araymond.joalcore.core.sharing.domain.events.TorrentStartedDownloadingEvent;
import com.araymond.joalcore.core.sharing.domain.events.TorrentStartedSeedingEvent;
import com.araymond.joalcore.core.sharing.domain.exception.IllegalActionForTorrentState;
import com.araymond.joalcore.events.DomainEvent;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@AggregateRoot
public class SharedTorrent {
    private final UUID id;
    private final InfoHash infoHash;
    private SharingStatus status;
    private Contributions contributions;

    public SharedTorrent(InfoHash infoHash, Contribution overallContribution, Left left) {
        this(UUID.randomUUID(), infoHash, SharingStatus.Paused, new Contributions(overallContribution, left));
    }

    public SharedTorrent(UUID id, InfoHash infoHash, SharingStatus status, Contributions contributions) {
        this.id = id;
        this.infoHash = infoHash;
        this.status = status;
        this.contributions = contributions;
    }

    public List<DomainEvent> add(DownloadAmount download) {
        if (!isDownloading()) {
            throw new IllegalActionForTorrentState("can not add download to a %s torrent".formatted(status));
        }
        this.contributions = this.contributions.add(download);

        if (isDownloading() && contributions.isFullyDownloaded()) {
            return List.of(new DoneDownloadingEvent());
        }
        return Collections.emptyList();
    }

    public void add(UploadAmount upload) {
        if (isPaused()) {
            throw new IllegalActionForTorrentState("can not add download to a %s torrent".formatted(status));
        }
        this.contributions.add(upload);
    }

    public boolean isDownloading() {
        return status == SharingStatus.Downloading;
    }

    public boolean isPaused() {
        return status == SharingStatus.Paused;
    }

    public boolean isSeeding() {
        return status == SharingStatus.Seeding;
    }

    public List<DomainEvent> pause() {
        status = status.pause();
        return List.of(new TorrentPausedEvent());
    }

    public List<DomainEvent> resume() {
        if (contributions.isFullyDownloaded()) {
            return seed();
        }
        return download();
    }

    public List<DomainEvent> download() {
        status = status.download();
        return List.of(new TorrentStartedDownloadingEvent());
    }

    public List<DomainEvent> seed() {
        status = status.seed();
        return List.of(new TorrentStartedSeedingEvent());
    }
}

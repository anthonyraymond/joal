package com.araymond.joalcore.core.sharing.domain;

import com.araymond.joalcore.annotations.ddd.AggregateRoot;
import com.araymond.joalcore.core.infohash.domain.InfoHash;
import com.araymond.joalcore.core.sharing.domain.events.*;
import com.araymond.joalcore.core.sharing.domain.exceptions.IllegalActionForTorrentState;
import com.araymond.joalcore.core.sharing.domain.services.PeerElection;
import com.araymond.joalcore.events.DomainEvent;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@AggregateRoot
public class SharedTorrent {
    private final SharedTorrentId id;
    private final InfoHash infoHash;
    private SharingStatus status;
    private Contributions contributions;
    private Swarm swarm;

    public SharedTorrent(InfoHash infoHash, Contribution overallContribution, Left left) {
        this(SharedTorrentId.random(), infoHash, SharingStatus.Paused, new Contributions(overallContribution, left), Swarm.EMPTY);
    }

    public SharedTorrent(SharedTorrentId id, InfoHash infoHash, SharingStatus status, Contributions contributions, Swarm swarm) {
        this.id = id;
        this.infoHash = infoHash;
        this.status = status;
        this.contributions = contributions;
        this.swarm = swarm;
    }

/* TODO
    public void create(TorrentMetadata metadata) {
        factory.build();
    }*/

    public List<DomainEvent> add(DownloadAmount download) {
        if (!isDownloading()) {
            throw new IllegalActionForTorrentState("can not add download to a %s torrent".formatted(status));
        }
        this.contributions = this.contributions.add(download);

        if (isDownloading() && contributions.isFullyDownloaded()) {
            var events = this.seed();
            return Stream.concat(
                    Stream.of(new DoneDownloadingEvent()),
                    events.stream()
            ).toList();
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

    public List<DomainEvent> registerPeers(Swarm.TrackerUniqueIdentifier identifier, Peers peers, PeerElection peerElection) {
        swarm = swarm.with(identifier, peers);

        return this.swarm.representativePeers(peerElection)
                .map(p -> (DomainEvent) new TorrentPeersChangedEvent(id, p))
                .stream().toList();
    }
}

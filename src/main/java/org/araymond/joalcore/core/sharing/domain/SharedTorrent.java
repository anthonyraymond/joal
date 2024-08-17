package org.araymond.joalcore.core.sharing.domain;

import org.araymond.joalcore.annotations.ddd.AggregateRoot;
import org.araymond.joalcore.core.metadata.domain.InfoHash;
import org.araymond.joalcore.core.sharing.domain.events.DoneDownloadingEvent;
import org.araymond.joalcore.core.sharing.domain.events.TorrentPausedEvent;
import org.araymond.joalcore.core.sharing.domain.events.TorrentPeersChangedEvent;
import org.araymond.joalcore.core.sharing.domain.events.TorrentStartedSharingEvent;
import org.araymond.joalcore.core.sharing.domain.exceptions.IllegalActionForTorrentState;
import org.araymond.joalcore.core.sharing.domain.exceptions.InvalidContributionException;
import org.araymond.joalcore.core.sharing.domain.services.PeerElection;
import org.araymond.joalcore.events.DomainEvent;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

    public SharedTorrentId id() {
        return id;
    }

    public InfoHash infoHash() {
        return infoHash;
    }

    public List<DomainEvent> add(DownloadAmount download) {
        if (!isSharing()) {
            throw new IllegalActionForTorrentState("can not add download to a %s torrent".formatted(status));
        }
        if (contributions.isFullyDownloaded()) {
            throw new InvalidContributionException("can not add download to a torrent fully downloaded");
        }

        this.contributions = this.contributions.add(download);

        if (contributions.isFullyDownloaded()) {
            return List.of(new DoneDownloadingEvent());
        }
        return Collections.emptyList();
    }

    public void add(UploadAmount upload) {
        if (isPaused()) {
            throw new IllegalActionForTorrentState("can not add download to a %s torrent".formatted(status));
        }
        this.contributions = this.contributions.add(upload);
    }

    public boolean isSharing() {
        return status == SharingStatus.Sharing;
    }

    public boolean isPaused() {
        return status == SharingStatus.Paused;
    }

    public List<DomainEvent> pause() {
        status = status.pause();
        return List.of(new TorrentPausedEvent());
    }

    public List<DomainEvent> share() {
        status = status.share();

        return List.of(new TorrentStartedSharingEvent(contributions.isFullyDownloaded()));
    }

    public List<DomainEvent> registerPeers(Swarm.TrackerUniqueIdentifier identifier, Peers peers, PeerElection peerElection) {
        swarm = swarm.with(identifier, peers);

        return this.swarm.representativePeers(peerElection)
                .map(p -> (DomainEvent) new TorrentPeersChangedEvent(id, p))
                .stream().toList();
    }

    public Contribution overallContributions() {
        return contributions.overall();
    }

    public boolean isFullyDownloaded() {
        return contributions.isFullyDownloaded();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SharedTorrent that = (SharedTorrent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}

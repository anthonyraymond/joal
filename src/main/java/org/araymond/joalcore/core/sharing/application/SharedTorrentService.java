package org.araymond.joalcore.core.sharing.application;

import org.araymond.joalcore.core.metadata.domain.InfoHash;
import org.araymond.joalcore.core.metadata.domain.TorrentMetadata;
import org.araymond.joalcore.core.sharing.application.exceptions.UnknownSharedTorrentException;
import org.araymond.joalcore.core.sharing.domain.*;
import org.araymond.joalcore.core.sharing.domain.events.TorrentCreatedEvent;
import org.araymond.joalcore.events.DomainEvent;
import org.araymond.joalcore.events.DomainEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SharedTorrentService {
    private final SharedTorrentRepository torrents;
    private final DomainEventPublisher publisher;
    private final OverallContributions overallContributions;
    private final SharedTorrentConfig config;

    @Autowired
    public SharedTorrentService(SharedTorrentRepository torrents, DomainEventPublisher publisher, OverallContributions overallContributions, SharedTorrentConfig config) {
        this.torrents = torrents;
        this.publisher = publisher;
        this.overallContributions = overallContributions;
        this.config = config;
    }
    
    public void create(TorrentMetadata metadata) {
        var events = new ArrayList<DomainEvent>();

        var overallContributions = this.overallContributions.load(metadata.infoHash())
                .orElseGet(() -> {
                    Contribution overall = Contribution.ZERO;
                    if (config.skipDownload().get()) {
                        // return a fully Downloaded contribution when the torrent is not yet known and skip download is true
                        overall = new Contribution(new DownloadAmount(metadata.size().bytes()), new UploadAmount(0));
                    }
                    this.overallContributions.save(metadata.infoHash(), overall);

                    return overall;
                });

        var left = new Left(Math.max(metadata.size().bytes() - overallContributions.downloaded().bytes(), 0));

        var torrent = new SharedTorrent(metadata.infoHash(), overallContributions, left);
        events.add(new TorrentCreatedEvent(torrent.id()));

        var e = torrent.share();
        events.addAll(e);

        torrents.save(torrent);

        publish(events);
    }

    public void registerPeers(InfoHash infoHash, Swarm.TrackerUniqueIdentifier identifier, Peers peers) {
        var torrent = torrents.findByTorrentInfoHash(infoHash).orElseThrow(() -> new UnknownSharedTorrentException("No torrent found for %s".formatted(infoHash)));

        var events = torrent.registerPeers(identifier, peers, config.peersElection().get());
        torrents.save(torrent);

        publisher.publish(events);
    }

    public void pause(SharedTorrentId id) {
        var torrent = torrents.findById(id).orElseThrow(() -> new UnknownSharedTorrentException("No torrent found for %s".formatted(id)));

        var events = torrent.pause();
        torrents.save(torrent);

        publish(events);
    }

    public void share(SharedTorrentId id) {
        var torrent = torrents.findById(id).orElseThrow(() -> new UnknownSharedTorrentException("No torrent found for %s".formatted(id)));

        var events = torrent.share();
        torrents.save(torrent);

        publish(events);
    }

    public void addUpload(SharedTorrentId id, UploadAmount upload) {
        var torrent = torrents.findById(id).orElseThrow(() -> new UnknownSharedTorrentException("No torrent found for %s".formatted(id)));

        torrent.add(upload);
        torrents.save(torrent);
        overallContributions.save(torrent.infoHash(), torrent.overallContributions());
    }

    public void addDownload(SharedTorrentId id, DownloadAmount download) {
        var torrent = torrents.findById(id).orElseThrow(() -> new UnknownSharedTorrentException("No torrent found for %s".formatted(id)));

        var events = torrent.add(download);
        torrents.save(torrent);

        publish(events);
        overallContributions.save(torrent.infoHash(), torrent.overallContributions());
    }

    private void publish(List<DomainEvent> events) {
        publisher.publish(events);
    }

}

package com.araymond.joalcore.core.sharing.application;

import com.araymond.joalcore.core.infohash.domain.InfoHash;
import com.araymond.joalcore.core.sharing.application.exceptions.UnknownSharedTorrentException;
import com.araymond.joalcore.core.sharing.domain.*;
import com.araymond.joalcore.core.sharing.domain.services.PeerElection;
import com.araymond.joalcore.events.DomainEvent;
import com.araymond.joalcore.events.DomainEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

@Component
public class SharedTorrentService {
    private final SharedTorrentRepository torrents;
    private final DomainEventPublisher publisher;
    private final Supplier<PeerElection> electionSupplier;

    @Autowired
    public SharedTorrentService(SharedTorrentRepository torrents, DomainEventPublisher publisher, Supplier<PeerElection> electionSupplier) {
        this.torrents = torrents;
        this.publisher = publisher;
        this.electionSupplier = electionSupplier;
    }


    public void registerPeers(InfoHash infoHash, Swarm.TrackerUniqueIdentifier identifier, Peers peers) {
        var torrent = torrents.findByTorrentInfoHash(infoHash).orElseThrow(() -> new UnknownSharedTorrentException("No torrent found for %s".formatted(infoHash)));

        var events = torrent.registerPeers(identifier, peers, electionSupplier.get());
        torrents.save(torrent);

        publisher.publish(events);
    }

    public void pause(SharedTorrentId id) {
        var torrent = torrents.findById(id).orElseThrow(() -> new UnknownSharedTorrentException("No torrent found for %s".formatted(id)));

        var events = torrent.pause();
        torrents.save(torrent);

        publish(events);
    }

    public void resume(SharedTorrentId id) {
        var torrent = torrents.findById(id).orElseThrow(() -> new UnknownSharedTorrentException("No torrent found for %s".formatted(id)));

        var events = torrent.resume();
        torrents.save(torrent);

        publish(events);
    }

    public void download(SharedTorrentId id) {
        var torrent = torrents.findById(id).orElseThrow(() -> new UnknownSharedTorrentException("No torrent found for %s".formatted(id)));

        var events = torrent.download();
        torrents.save(torrent);

        publish(events);
    }

    public void seed(SharedTorrentId id) {
        var torrent = torrents.findById(id).orElseThrow(() -> new UnknownSharedTorrentException("No torrent found for %s".formatted(id)));

        var events = torrent.seed();
        torrents.save(torrent);

        publish(events);
    }

    public void addUpload(SharedTorrentId id, UploadAmount upload) {
        var torrent = torrents.findById(id).orElseThrow(() -> new UnknownSharedTorrentException("No torrent found for %s".formatted(id)));

        torrent.add(upload);
        torrents.save(torrent);
    }

    public void addDownload(SharedTorrentId id, DownloadAmount download) {
        var torrent = torrents.findById(id).orElseThrow(() -> new UnknownSharedTorrentException("No torrent found for %s".formatted(id)));

        var events = torrent.add(download);
        torrents.save(torrent);

        publish(events);
    }

    private void publish(List<DomainEvent> events) {
        publisher.publish(events);
    }
}
